#include "info.h"
#include <sstream>
#include <iomanip>
#include <stdexcept>
#include <fstream>
#include <iostream>
#include <unistd.h>
#include <ifaddrs.h>
#include <net/if.h>
#include <arpa/inet.h>
#include <sys/ioctl.h>
#include <iostream>
#include <vector>
#include <sys/socket.h>
#include <netinet/in.h>
#include <net/if_arp.h>
#include <cstring>
#include <curl/curl.h>
#include <pthread.h>
#include <cmath>
/**
 * 保留两位小数
 * @brief formatDouble
 * @param source
 * @return
 */
double formatDouble(double source){
    return (int) (100.0 * source + 0.5) / 100.0;
}
std::string readConfigFile(const std::string &path) {
    // 打开文件
    std::ifstream file(path);
    if (!file.is_open()) {
        throw std::runtime_error("Failed to open file: " + path);
    }

    try {
        // 使用 stringstream 一次性读取文件的全部内容
        std::stringstream buffer;
        buffer << file.rdbuf();

        // 关闭文件
        file.close();

        // 返回读取到的内容
        return buffer.str();
    } catch (const std::exception &e) {
        // 关闭文件（如果未关闭）
        if (file.is_open()) {
            file.close();
        }
        throw std::runtime_error("Error reading file: " + path + ". Details: " + std::string(e.what()));
    }
}

/**
 * 接收到服务端返回值
 *
 * @brief Info::onTextMessageReceived
 * @param message
 */
void onTextMessageReceived(const std::string &message) {
    printf(">>> %s\n", message.c_str());
}


/**
 * 重新开启websocket连接
 * @param url
 * @return
 */
easywsclient::WebSocket::pointer Info::openWs(std::string &url) {
    while (true) {
        // 需要解决ws 不为空，但是连接失败问题
        if (ws == nullptr) {
            std::cout << "初始化连接" << std::endl;
            ws = easywsclient::WebSocket::from_url(url);

        } else if (
                ws->getReadyState() == easywsclient::WebSocket::CLOSED
                ) {
            sleep(1);
            std::cout << " 连接断开，等待重连   "
                      << std::endl;


            delete ws;
            ws = nullptr;

        } else {
            std::cout << "连接成功" << std::endl;
            break;
        }
        sleep(2);

    }

}


Info::~Info() {
    // 先关闭旧连接
    if (ws) {
        std::cout<<"销毁ws链接"<<std::endl;
        ws->close();
        delete ws;
        ws = nullptr;
    }

}


void Info::initConfig() {
    std::string configPath = "config.json";

    int ret = access(configPath.c_str(), F_OK);
    if (ret != 0) {
        std::cout << "文件不存在" << std::endl;
        return;
    }


    std::string jsonStr = readConfigFile(configPath);
    std::cout << jsonStr << std::endl;
    Json::Value obj = reflect_json::strToJson(jsonStr);

    if (!(obj.isMember("server")
          &&
          obj.isMember("port")
          &&
          obj.isMember("token")
    )) {

        std::cout << "文件格式异常" << std::endl;
        return;
    }

    char buffer[2048];  // 你可以根据实际情况调整缓冲区大小

    // 使用 sprintf 格式化字符串
    sprintf(buffer, "ws://%s:%d/?token=%s&type=dev&endpointName=%s",
            obj["server"].asString().c_str(),
            obj["port"].asInt(),
            obj["token"].asString().c_str(),
            localmachineName().c_str()
            );


    this->urlStr = std::string(buffer);
    std::cout << "url = " << this->urlStr << std::endl;


}

/**
 * 更新需要频繁更新的数据
 */
void Info::refreshDataFast() {


    // //cpuInfo 的封装
    cpuInfo();
    // std::cout.noquote()<<reflect_json::serialize(info->d.cpuInfo);


    //std::cout.noquote()<<reflect_json::serialize(info->d.osInfo);



    //频繁更新的
    // //memInfo
    memInfo();
    // std::cout.noquote()<<reflect_json::serialize(info->d.memInfo);


    // driverInfo
    driveInfo();
    // std::cout.noquote()<<reflect_json::serialize(info->d.driveInfo);

    //netstatInfo
    netstatInfo();
    //std::cout.noquote()<<reflect_json::serialize(info->d.netstatInfo);

    //netInterface
    netInterface();
    // std::cout.noquote()<<reflect_json::serialize(info->d.netInterface);



    ws->send(reflect_json::serialize(d));
    if (firstInit) {
        firstInit = false;
    }
}

/**
 * 快刷新并发送数据
 * @param ppp
 * @return
 */
void *refreshAndSend(void *ppp) {

    Info *const that = static_cast<Info *const>(ppp);

    std::cout << " loop start " << std::endl;
    while (true) {
        if (that->ws->getReadyState() != easywsclient::WebSocket::CLOSED) {
            std::cout << " send " << std::endl;
            //先发一次，然后等服务端返回数据
            that->refreshDataFast();
            std::cout << " poll " << std::endl;
            that->ws->poll();
            std::cout << " dispatch " << std::endl;
            that->ws->dispatch([](const std::string &message) {
                onTextMessageReceived(message);
            });

        } else {
            that->openWs(that->urlStr);
        }
        sleep(1);
    }
}

/**
 * 慢刷新，一小时一次
 * @param ppp
 * @return
 */
void *slowFresh(void *ppp) {

    Info *const that = static_cast<Info *>(ppp);

    std::cout << " loop start " << std::endl;
    while (true) {
        //一小时更新一次ip地址
        that->ipInfo();
        that->osInfo();
        std::cout << "更新ip os 信息" << reflect_json::serialize(that->d.ipInfo);

        sleep(3600);
    }
}

/**
 * 一秒运行一次的线程
 */
void threadStart(void *(*run)(void *), void *arg) {

    pthread_t thread;
    int r = pthread_create(&thread, NULL, run, arg);
    if (r != 0) {
        printf("线程创建失败:%s \n", strerror(r));
        exit(-1);
    }

    int d = pthread_detach(thread);
    if (d != 0) {
        printf("pthread_detach失败:%s \n", strerror(r));
        exit(-1);
    }

}


Info::Info() {

    this->initConfig();
    if (this->urlStr.empty()) {
        std::cout << "初始化失败";
        exit(0);
    }
    //初始化时刷新的
    ipInfo();
    osInfo();

    //打开链接
    openWs(this->urlStr);

    //启动一秒执行一次的线程
    threadStart(refreshAndSend, this);
    //启动一小时更新一次的
    //threadStart(slowFresh, this);

    slowFresh(this);
}

// 回调函数：用于将服务器响应的数据写入字符串
size_t WriteCallback(void *contents, size_t size, size_t nmemb, void *userp) {
    std::string *response = static_cast<std::string *>(userp);
    size_t totalSize = size * nmemb;
    response->append(static_cast<char *>(contents), totalSize);
    return totalSize;
}

void Info::ipInfo() {

    // 初始化 CURL
    CURL *curl;
    CURLcode res;
    std::string responseString;

    // 创建 CURL 句柄
    curl = curl_easy_init();
    if (curl) {
        // 设置请求 URL
        curl_easy_setopt(curl, CURLOPT_URL, "https://api.qjqq.cn/api/Local");

        // 启用 SSL/TLS 验证（可选）
        // 如果需要禁用证书验证（仅用于测试环境），可以设置为 false
        curl_easy_setopt(curl, CURLOPT_SSL_VERIFYPEER, 1L);  // 启用证书验证
        curl_easy_setopt(curl, CURLOPT_SSL_VERIFYHOST, 2L);  // 启用主机名验证

        // 设置回调函数，用于处理服务器响应
        curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, WriteCallback);
        curl_easy_setopt(curl, CURLOPT_WRITEDATA, &responseString);

        // 执行请求
        res = curl_easy_perform(curl);

        // 检查请求是否成功
        if (res != CURLE_OK) {
            std::cerr << "curl_easy_perform() failed: " << curl_easy_strerror(res) << std::endl;
        } else {
            // 获取 HTTP 响应状态码
            long httpCode = 0;
            curl_easy_getinfo(curl, CURLINFO_RESPONSE_CODE, &httpCode);

            if (httpCode == 200) {
                std::cout << "HTTPS GET request successful!" << std::endl;

                // 解析 JSON 响应
                Json::Value root;
                Json::CharReaderBuilder reader;
                std::string errs;

                std::istringstream s(responseString);
                if (Json::parseFromStream(reader, s, &root, &errs)) {
                    // 访问 JSON 数据
//                    std::cout << "Title: " << root["data"] << std::endl;
//                    std::cout << "Title: " << root["time"] << std::endl;
                    d.ipInfo = root["data"];
                    d.ipInfo["time"] = root["time"];
                } else {
                    std::cerr << "Failed to parse JSON: " << errs << std::endl;
                }
            } else {
                std::cerr << "Request failed with HTTP status code: " << httpCode << std::endl;
            }
        }

        // 清理 CURL 资源
        curl_easy_cleanup(curl);
    }
}

/*
 *
 * 计算机名
 */
const std::string Info::localmachineName() {
    char hostname[256];
    gethostname(hostname, sizeof(hostname));
    return std::string(hostname);
}

// 将 in_addr 或 in6_addr 转换为字符串
std::string inet_ntop_wrapper(const struct sockaddr *sa, char *str, size_t len) {
    if (sa->sa_family == AF_INET) {
        return inet_ntop(AF_INET, &(((struct sockaddr_in *) sa)->sin_addr), str, len);
    } else if (sa->sa_family == AF_INET6) {
        return inet_ntop(AF_INET6, &(((struct sockaddr_in6 *) sa)->sin6_addr), str, len);
    }
    return "";
}

// 获取网络接口信息
// 获取网络接口信息
void getNetInterfaces(std::map<std::string, std::vector<NetInterfaceInfo>> &map) {
    struct ifaddrs *ifaddr, *ifa;

    // 调用getifaddrs函数获取网络接口列表
    if (getifaddrs(&ifaddr) == -1) {
        perror("getifaddrs");
        return;
    }

    // 遍历所有网络接口
    for (ifa = ifaddr; ifa != nullptr; ifa = ifa->ifa_next) {
        if (ifa->ifa_addr == nullptr) continue;

        NetInterfaceInfo info;
        std::vector<NetInterfaceInfo> interfaces;
        std::string ifaceName = ifa->ifa_name;

        // 处理IPv4地址
        if (ifa->ifa_addr->sa_family == AF_INET) {
            info.family = "IPv4";

            // IP地址
            char addr[INET_ADDRSTRLEN];
            inet_ntop(AF_INET, &((struct sockaddr_in *) ifa->ifa_addr)->sin_addr, addr, sizeof(addr));
            info.address = addr;

            // 子网掩码
            if (ifa->ifa_netmask) {
                char mask[INET_ADDRSTRLEN];
                inet_ntop(AF_INET, &((struct sockaddr_in *) ifa->ifa_netmask)->sin_addr, mask, sizeof(mask));
                info.netmask = mask;
            } else {
                info.netmask = "N/A";
            }

            // 广播地址
            if (ifa->ifa_ifu.ifu_broadaddr) {
                char broad[INET_ADDRSTRLEN];
                inet_ntop(AF_INET, &((struct sockaddr_in *) ifa->ifa_ifu.ifu_broadaddr)->sin_addr, broad,
                          sizeof(broad));
                info.boardcast = broad;
            } else {
                info.boardcast = "N/A";
            }
        }
            // 处理IPv6地址
        else if (ifa->ifa_addr->sa_family == AF_INET6) {
            info.family = "IPv6";

            // IP地址
            char addr[INET6_ADDRSTRLEN];
            inet_ntop(AF_INET6, &((struct sockaddr_in6 *) ifa->ifa_addr)->sin6_addr, addr, sizeof(addr));
            info.address = addr;

            // 子网掩码（IPv6不常用，可选）
            info.netmask = "N/A";
            info.boardcast = "N/A";
        } else {
            continue;
        }

        // 判断是否为内部接口（如 lo）
        if (strcmp(ifa->ifa_name, "lo") == 0) {
            info.internal = true;
        }

        // 获取 MAC 地址 (仅在IPv4时获取)
        if (ifa->ifa_addr->sa_family == AF_INET) {
            int sockfd = socket(AF_INET, SOCK_DGRAM, 0);
            if (sockfd != -1) {
                struct ifreq ifr;
                memset(&ifr, 0, sizeof(ifr));
                strncpy(ifr.ifr_name, ifa->ifa_name, IFNAMSIZ - 1);
                if (ioctl(sockfd, SIOCGIFHWADDR, &ifr) == 0) {
                    std::ostringstream macStream;
                    for (int j = 0; j < 6; ++j) {
                        macStream << std::hex << (int) ((unsigned char *) ifr.ifr_hwaddr.sa_data)[j];
                        if (j != 5) macStream << ":";
                    }
                    info.mac = macStream.str();
                } else {
                    info.mac = "N/A";
                }
                close(sockfd);
            }
        }

        // 添加到 map
        map[ifaceName].push_back(info);
    }

    freeifaddrs(ifaddr);
}
/*
 * 本机局域网ip
 */
void Info::netInterface() {
    d.netInterface = {};
    getNetInterfaces(d.netInterface);

}



