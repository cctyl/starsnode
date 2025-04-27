const getOsData = require('./utils/os-data')
const ws = require("ws");
const url = require('url');
const path = require('path');
const config = require(path.join(process.cwd(), './config.js'));
const func = require('./utils/functions');
var server = new ws.Server({
    host: "0.0.0.0",//绑定所有网卡
    port: config.port
});
/**
 * 上传信息的设备
 * @type {{}}
 */
const devMap = {};
/**
 * 获取信息的设备
 * @type {{}}
 */
const viewMap = {};

/**
 * 设备上次传输数据的时间
 *
 */
const lastDataTimeMap = {};

/**
 * os 数据map
 * @type {{}}
 */
const devDataMap = {};
let ipInfo = {};


// 监听接入进来的客户端事件
function websocket_add_listener(socket, request) {

    const query = url.parse(request.url, true).query;
    const token = query.token;
    const type = query.type;
    const endpointName = query.endpointName;

    if (!token || !type || !endpointName) {
        console.log("参数异常")
        socket.terminate();
    }
    if (token !== config.token) {
        console.log(endpointName + "鉴权失败:" + token);
        socket.terminate();
    } else {
        console.log(endpointName + "鉴权通过：" + token);
    }

    let targetMap = null;
    switch (type) {
        case 'dev':
            targetMap = devMap;
            break;
        case 'view':
            targetMap = viewMap;
            break;
        default:
            console.log(`非法类型${type}，断开连接`);
            socket.terminate();
    }
    targetMap[endpointName] = socket;
    lastDataTimeMap[endpointName] = Math.floor(Date.now() / 1000);
    func.sendAlert(`[starnode] ${endpointName} 上线了`);
    // close事件
    socket.on("close", function () {
        console.log(`client:${endpointName} close`);
        func.sendAlert(`[starnode] ${endpointName} close 掉线`);
        //置空socket
        destory(endpointName);
    });

    // error事件
    socket.on("error", function (err) {
        console.log(`client:${endpointName} error`);
        func.sendAlert(`[starnode] ${endpointName} error 掉线`);
        //置空socket
        destory(endpointName);
    });


    /**
     * 处理响应
     */
    socket.on("message", function (data) {
        //console.log(`message from ${endpointName}`);
        if (!devMap[endpointName] ){
            console.log(`${endpointName} 掉线后仍在发送数据 `)
            socket.close(1000, '服务端主动断开连接');
            return;
        }
        firstConn = false;
        lastDataTimeMap[endpointName] = Math.floor(Date.now() / 1000);
        if (type === 'dev') {
            try {
                handleMessage(socket, endpointName, JSON.parse(data.toString()));
            } catch (e) {
                console.error(e)
            }
        }
    });


}

/**
 * 销毁连接
 * @param endpointName
 */
function destory(endpointName) {

    delete devMap[endpointName];
    delete viewMap[endpointName];
    delete devDataMap[endpointName];
    delete lastDataTimeMap[endpointName];
}

/**
 * 处理数据
 * @param socket
 * @param endpointName
 * @param data Object
 */
function handleMessage(socket, endpointName, data) {
    devDataMap[endpointName] = data;
}


server.on("connection", websocket_add_listener);
server.on("error", (err) => {
    console.error(err)
});

/**
 * 心跳包
 */
setInterval(args => {

    let socketArr = Object.values(devMap)
    for (let item of socketArr) {
        item.send(JSON.stringify({}))
    }

}, 1000);

/**
 * 超时检测
 */
setInterval(args => {
    const now = Math.floor(Date.now() / 1000);
    for (const endpointName in lastDataTimeMap) {
        let time = now - lastDataTimeMap[endpointName];
        if (time >10 ){
            console.log(`${endpointName}检查心跳后掉线`);
            func.sendAlert(`[starnode] ${endpointName}  检查心跳后掉线，超时${time}秒`);
            destory(endpointName);
        }
    }
},5000);

/**
 * 发送数据给观察的设备
 */
setInterval(async args => {
    let osDataArr = Object.values(devDataMap);
    let osData = await getOsData();
    Object.assign(osData, {ipInfo})
    osDataArr.push(osData);
    for (let item of Object.values(viewMap)) {
        item.send(JSON.stringify(osDataArr));
    }
}, 1000);
/**
 * 更新ip
 */
setInterval(async args => {
    func.getIpInfo().then(value => {
        ipInfo = value;
    });
}, 30 * 60 * 1000);

console.log(`监听:${config.port}`);
func.sendAlert(`[starnode] 服务端启动`)
//初始化时更新数据
func.getIpInfo().then(value => {
    ipInfo = value;
});


