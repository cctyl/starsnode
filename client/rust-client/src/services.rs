use crate::{
    modles::*,
    utils::{byte_to_gb, byte_to_mb, round_f32},
};
use chrono::Local;
use network_interface::NetworkInterface;
use network_interface::NetworkInterfaceConfig;
use serde::de::value;
use serde_json::Value;
use std::{
    collections::HashMap,
    error::Error,
    fs::{self, File},
    io::{BufRead, BufReader},
    net::TcpStream,
    thread,
    time::Duration,
};
use sysinfo::{Disks, Networks, System};
use tungstenite::{Message, WebSocket, connect, stream::MaybeTlsStream};
fn read_config(path: String) -> Result<Config, Box<dyn Error>> {
    match fs::read_to_string(&path) {
        Ok(s) => match serde_json::from_str(&s) {
            Ok(config) => Ok(config),
            Err(e) => {
                eprintln!("解析配置文件失败");
                Err(Box::new(e))
            }
        },
        Err(s) => {
            eprintln!("读取配置文件:{path}失败！");
            Err(Box::new(s))
        }
    }
}

/// 填充存储信息
fn drive_info(d: &mut DevInfo) {
    let disks = Disks::new_with_refreshed_list();

    // 使用 HashSet 记录已处理的设备
    let mut processed_devices = std::collections::HashSet::new();

    let (total_gb, free_gb, used_gb) = disks
        .iter()
        .filter(|disk| {
            if let Some(name) = disk.name().to_str() {
                return !name.contains("overlay");
            } else {
                return false;
            }

            // 只处理每个物理设备一次
            //如果元素存在则insert返回false
            processed_devices.insert(disk.name().to_owned())
        })
        .map(|disk| {
            let total = round_f32(byte_to_gb(disk.total_space() as f32));
            let free = round_f32(byte_to_gb(disk.available_space() as f32));
            let used = (total - free).round();

            // println!("{disk:#?} total={total}, free={free}, used={used}");

            (total, free, used)
        })
        .fold((0.0, 0.0, 0.0), |acc, x| {
            (acc.0 + x.0, acc.1 + x.1, acc.2 + x.2)
        });

    let free_percentage = free_gb / total_gb * 100.0;
    let used_percentage = used_gb / total_gb * 100.0;

    d.drive_info = Some(DriveInfo {
        free_gb: round_f32(free_gb),
        total_gb: round_f32(total_gb),
        used_gb: round_f32(used_gb),
        used_percentage: round_f32(used_percentage),
        free_percentage: round_f32(free_percentage),
    });
}

/// 填充cpu信息到DevInfo中
fn cpu_info(d: &mut DevInfo, sys: &mut System) {
    sys.refresh_cpu_usage();
    let cpus = sys.cpus();
    let cpu_count = cpus.len() as u16;
    let cpu_model = cpus[0].brand().to_string();
    let total_usage: f32 = sys.global_cpu_usage();

    let c = CpuInfo {
        cpu_count,
        cpu_model: cpu_model,
        cpu_usage: round_f32(total_usage),
    };

    d.cpu_info = Some(c);
}
/// 填充内存信息
fn mem_info(d: &mut DevInfo, sys: &mut System) {
    sys.refresh_memory();
    let total_mem_mb = round_f32(byte_to_mb(sys.total_memory() as f32));
    let used_mem_mb = round_f32(byte_to_mb(sys.used_memory() as f32));
    let free_mem_mb = total_mem_mb - used_mem_mb;

    let used_mem_percentage = round_f32((used_mem_mb / total_mem_mb) * 100.0);
    let free_mem_percentage = round_f32(100.0 - used_mem_percentage);

    d.mem_info = Some(MemInfo {
        free_mem_mb,
        free_mem_percentage,
        total_mem_mb,
        used_mem_mb,
        used_mem_percentage,
    });
}

const IPV4_STR: &str = "IPv4";
const IPV6_STR: &str = "IPv6";
/// 网卡信息
fn net_interface(d: &mut DevInfo) {
    let mut map: HashMap<String, Vec<NetworkInterfaceInfo>> = HashMap::new();

    let network_interfaces = NetworkInterface::show().unwrap();
    for itf in network_interfaces.into_iter() {
        let mut net_arr = Vec::new();
        let name = itf.name;

        let mac = itf.mac_addr;
        let mac = mac;

        for addr in itf.addr {
            let mut item = NetworkInterfaceInfo::default();

            match addr {
                network_interface::Addr::V4(v4_if_addr) => {
                    item.family = IPV4_STR.to_string();
                    item.address = v4_if_addr.ip.to_string();
                    if let Some(netmask) = v4_if_addr.netmask {
                        item.netmask = netmask.to_string();
                    }
                    if let Some(broadcast) = v4_if_addr.broadcast {
                        item.boardcast = broadcast.to_string();
                    }
                }
                network_interface::Addr::V6(v6_if_addr) => {
                    item.family = IPV6_STR.to_string();
                    item.address = v6_if_addr.ip.to_string();
                    if let Some(netmask) = v6_if_addr.netmask {
                        item.netmask = netmask.to_string();
                    }

                    if let Some(broadcast) = v6_if_addr.broadcast {
                        item.boardcast = broadcast.to_string();
                    }
                }
            }

            item.mac = mac.clone();
            net_arr.push(item);
        }

        map.insert(name.clone(), net_arr);
    }

    d.net_interface = Some(map);
}

///网速信息
fn netstat_info(d: &mut DevInfo, networks: &mut Networks) {
    thread::sleep(Duration::from_secs(1));
    networks.refresh(true);

    let mut total_input_mb: f32 = 0.0;
    let mut total_output_mb: f32 = 0.0;

    for (interface_name, data) in networks.iter() {
        if data.mac_address().to_string().ne("00:00:00:00:00:00") {
            let input_mb = round_f32(byte_to_mb(data.received() as f32));
            let output_mb = round_f32(byte_to_mb(data.transmitted() as f32));
            d.netstat_info.insert(
                interface_name.clone(),
                NetstatInfo {
                    input_mb,
                    output_mb,
                },
            );
            total_input_mb += input_mb;
            total_output_mb += output_mb;
        }
    }

    d.netstat_info.insert(
        "total".to_string(),
        NetstatInfo {
            input_mb: total_input_mb,
            output_mb: total_output_mb,
        },
    );
}

#[cfg(target_os = "windows")]
pub fn get_logged_in_user_count() -> usize {
    use std::ptr;
    use windows::Win32::System::RemoteDesktop::{
        WTS_CURRENT_SERVER_HANDLE, WTS_SESSION_INFOW, WTSActive, WTSEnumerateSessionsW,
        WTSFreeMemory,
    };
    let mut p_session_info: *mut WTS_SESSION_INFOW = ptr::null_mut();
    let mut count: u32 = 0;
    let mut users = 0;
    unsafe {
        if WTSEnumerateSessionsW(
            WTS_CURRENT_SERVER_HANDLE,
            0,
            1,
            &mut p_session_info,
            &mut count,
        )
        .is_ok()
        {
            let sessions = std::slice::from_raw_parts(p_session_info, count as usize);
            for s in sessions {
                if s.State == WTSActive {
                    users += 1;
                }
            }
            WTSFreeMemory(p_session_info as _);
        }
    }
    users
}

#[cfg(target_os = "linux")]
pub fn get_logged_in_user_count() -> usize {
    let file = File::open("/proc/net/tcp").unwrap();
    let reader = BufReader::new(file);
    let mut count = 0;

    for line in reader.lines().skip(1) {
        // 跳过标题行
        let line = line.unwrap();
        let fields: Vec<&str> = line.split_whitespace().collect();
        // 检查状态码是否为 ESTABLISHED (01) 且本地端口为 22 (0016)
        if fields.get(3) == Some(&"01") && fields.get(1).map(|s| s.contains(":0016")) == Some(true)
        {
            count += 1;
        }
    }
    count
}

//其他系统默认0
#[cfg(not(any(target_os = "linux", target_os = "windows")))]
pub fn get_logged_in_user_count() -> usize {
    0
}

pub fn os_info(d: &mut DevInfo) {
    if sysinfo::IS_SUPPORTED_SYSTEM {
        let uptime = System::uptime();
        let arch = System::cpu_arch();
        let host_name = System::host_name();
        let platform = std::env::consts::OS.to_string();
        let type_ = System::distribution_id();
        let release = match (System::kernel_version(), System::long_os_version()) {
            (Some(release), Some(os)) => Some(format!(" {os} {release}")),
            _ => None,
        };

        let os = OsInfo {
            arch,
            hostname: host_name,
            platform,
            release,
            r#type: type_,
            uptime,
        };

        d.os_info = Some(os);
    } else {
        println!("This OS isn't supported (yet?).");
    }
}

/*
    启动
*/
pub fn run(path: String) -> Result<(), Box<dyn Error>> {
    let c = read_config(path)?;

    //获取url
    let url = get_url(&c);

    //获取连接
    let mut socket = open_conn(&url)?;
    let mut dev = DevInfo::default();
    dev.netstat_info = HashMap::new();
    let mut sys = System::new_all();
    let mut networks = Networks::new_with_refreshed_list();

    // 记录上次更新时间
    let mut last_1_update = None;
    let mut last_10_update = None;
    let mut last_60_update = None;
    //循环发送
    loop {
        let now = Local::now();

        //填充cpu数据
        cpu_info(&mut dev, &mut sys);

        //内存
        mem_info(&mut dev, &mut sys);

        //网速信息
        netstat_info(&mut dev, &mut networks);

        //1分钟执行一次
        if last_1_update.is_none()
            || (now
                .signed_duration_since(last_1_update.unwrap())
                .num_minutes()
                >= 1)
        {
            //登陆用户数
            dev.opened_count = get_logged_in_user_count() as u16;
            last_1_update = Some(now);
        }

        //10分钟执行一次
        if last_10_update.is_none()
            || (now
                .signed_duration_since(last_10_update.unwrap())
                .num_minutes()
                >= 10)
        {
            //填充存储数据
            drive_info(&mut dev);

            net_interface(&mut dev);

            last_10_update = Some(now);
            println!("10分钟执行了");
        }

        //60分钟执行一次
        if last_60_update.is_none()
            || (now
                .signed_duration_since(last_60_update.unwrap())
                .num_minutes()
                >= 60)
        {
            os_info(&mut dev);

            get_ip_info(&mut dev);

            last_60_update = Some(now);
            println!("60分钟执行了");
        }

        let jsonstr = serde_json::to_string(&dev).unwrap();

        // println!("{jsonstr}");

        match socket.send(Message::Text(jsonstr.into())) {
            Ok(_) => match socket.read() {
                Ok(_msg) => (),
                Err(e) => {
                    eprintln!("接收消息失败！原因：{:#?}", e);
                    match socket.close(None) {
                        Ok(_) => (),
                        Err(_) => println!("关闭失败！忽略此错误"),
                    }
                    socket = open_conn(&url)?;
                }
            },
            Err(e) => {
                eprintln!("发送失败！原因：{:#?}", e);
                match socket.close(None) {
                    Ok(_) => (),
                    Err(_) => println!("关闭失败！忽略此错误"),
                }
                socket = open_conn(&url)?;
            }
        }

        std::thread::sleep(Duration::from_secs(1));
    }
    Ok(())
}

pub fn open_conn(url: &str) -> Result<WebSocket<MaybeTlsStream<TcpStream>>, Box<dyn Error>> {
    loop {
        match connect(url) {
            Ok((socket, resp)) => {
                println!("连接成功！");
                return Ok(socket);
            }
            Err(e) => {
                eprintln!("连接失败：{:#?},3秒后重试", e);
            }
        }

        thread::sleep(Duration::from_secs(1));
    }
}

pub fn get_url(c: &Config) -> String {
    let device_name = System::host_name().unwrap_or("nonamepc".to_string());
    format!(
        "ws://{}:{}/?token={}&type=dev&endpointName={}",
        c.server, c.port, c.token, device_name
    )
}

// #[test]
// pub fn get_ip_info() {
pub fn get_ip_info(d: &mut DevInfo) {

    match  minreq::get("http://ip-api.com/json?lang=zh-CN") .send(){
        Ok(resp) => {
            let text = resp.as_str();
            match text {
                Ok(body) => {
                    let Ok(json_value) = serde_json::from_str::<Value>(&body) else {
                        eprintln!("json解析失败！");

                        return;
                    };
                    d.ip_info = Some(json_value);
                }
                Err(e) => println!("请求错误：{e:#?}"),
            }
        }
        Err(e) => println!("请求错误：{e:#?}"),
    }
}
