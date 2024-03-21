const wsModule = require("ws");
const path = require('path');
const config = require(path.join(process.cwd(), './config.js'));
const getOsData = require('./utils/os-data');
const func = require('./utils/functions');
let receiveMsg = false;

//获得设备名 更新
const endpointName = config.endpointName ? config.endpointName : func.guid();

let ws;
let lock=false;
/**
 * 创建websocket 客户端
 */
function createWebSocketClient(){
    if (ws){
        ws.close();
    }
    ws = new wsModule.WebSocket(`ws://${config.serverHost}:${config.port}?token=${config.token}&type=dev&endpointName=${endpointName}`);
    ws.on('error', args => {

        console.log("error 解锁");

        //解除锁定
        lock = false;

    });
    ws.on('open', function open() {
        receiveMsg = true;
        //解除锁定
        lock = false;
        console.log("连接成功 解锁")
    });
    ws.on('message', function message(data) {
        receiveMsg = true;
        console.log('received: %s', data);
    });
    ws.on("close",args => {
        console.log("close 解锁");
        //解除锁定
        lock = false;
    })

}
createWebSocketClient();


//2秒后没有收到服务端消息，就认为掉线，重新连接
let timeOut = setInterval(()=>{
    console.log("开始一轮检查");
    if (receiveMsg===true){
        //改为false，等待下一轮接收服务端消息后重置为true
        receiveMsg = false;
    }else {
        //如果已经上锁（没有多线程问题）
        if (lock===true){
            console.log("已锁定...");
            return;
        }
        //上锁
        lock = true;
        console.log("上锁");

        //如果是false，说明2秒没收到服务端响应了，该重连了
        console.log("断开重连");
        try {
            createWebSocketClient();
        } catch (e) {
            console.error(e);
        }
    }
},3000);




let ipInfo = {};
/**
 * 更新ip
 */
setInterval(async args => {
    try {
        func.getIpInfo().then(value => {
            ipInfo = value;
        });
    } catch (e) {
        console.error(e);
    }
}, 30 * 60 * 1000);

/**
 * 1秒上传一次数据
 */
setInterval(async args => {
    try {
        if (lock) {
            //lock=true时，说明还没有解锁，还在尝试连接，不应该发送数据
            return;
        }
        let osData = await getOsData();
        Object.assign(osData, {ipInfo});
        ws.send(JSON.stringify(osData));
    } catch (e) {
        console.error(e);
    }
}, 1000);


//初始化时更新数据
func.getIpInfo().then(value => {
    ipInfo = value;
});
