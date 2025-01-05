# starsnode

## 简介

[starsnode]是一个系统状态监控软件。

展示的信息包括：cpu、内存、硬盘、网速、网卡等信息。

### 优点

#### 高性能

c++编写的原生客户端和qt客户端，只占用%0.几的cpu，最低3.9M的内存

#### 轻量级

软件小，依赖少，编译十分简单，同时提供编译好的客户端

#### 跨平台

nodejs客户端几乎适配所有主流系统，win、linux、macos、android。
如要求性能，也可使用qt客户端，同样支持跨平台

本库的js服务端参考了该项目：https://github.com/chaos-zhu/easynode   感谢原作者。

## 效果图

<p align="center">
    <img src="./img/a.png" >
</p>

<p align="center">
    <img src="./img/b.png" >
</p>

## 平台构成

由1个服务端+n个客户端+web展示页面组成。三个部分每部分都可以单独部署，通过websocket进行数据传输。

各客户端首先将信息通过ws上报给服务端，服务端进行汇总。用户可以通过web页面查看到汇总后数据。

## 客户端选择

### 如果你不考虑性能问题

选择js客户端，兼容性好，bug少，稳定性强。

### 如果对性能敏感

win平台，使用qt客户端。

linux平台，如果内核版本比较新，比如ubuntu22以上，建议使用qt客户端。
如果是历史平台，比如centos系列，建议使用linux客户端，缺点是稳定性可能稍差。

macos，理论上也可以使用linux客户端，但是编译是个问题


## 性能展示

### qt客户端

win10以及win11实测，只占用5m左右的内存，百分之0.几的cpu

#### win10 4c 12g x64
<p align="center">
    <img src="./img/win10 4c 12g x64.png" >
</p>

#### win10 8c 16g x64
<p align="center">
    <img src="./img/win10 8c 16g x64.png" >
</p>


### linux客户端

在ubuntu和armbian均进行过测试，约占用14m左右的内存，百分之0.几的cpu。

如果你不需要主机所在地的ip信息，甚至只占用3.9M。

#### armbian 4c 4g arm64
<p align="center">
    <img src="./img/armbian 4c 4g x64.png" >
</p>

#### ubuntu18 2c 4g x64
<p align="center">
    <img src="./img/ubuntu18 2c 4g x64.png" >
</p>

#### ubuntu22 2c 1g x64
<p align="center">
    <img src="./img/ubuntu22 2c 1g x64.png" >
</p>



## 部署方式




### 配置
服务端的配置文件在 `server/config.js`，只需要设置port和token即可。

nodejs客户端的配置文件，在其目录下的config.js 中，需要设置服务端地址(serverHost)，端口(port)，token。
endpointName可省略，这是用于给客户端命名的。

qt和c++客户端配置文件，在各自目录下的 config.json 文件中，需要设置服务端地址(server)，端口(port)，token。
使用时，只需要把这个config.json，放在可执行文件的旁边即可。

web端的配置写到了 simple-list.html中的625行：
```
const ws = new WebSocket("ws://服务端地址:服务器端口?token=你的token&type=view&endpointName=web");
```

### web端

你可以选择部署到nginx中，也可以直接双击simple-list.html 在本地打开，如果你能直接访问服务端的话

### 服务端


```
# 需要安装 nodejs16 及以上
# 下载release文件中的server并解压
cd server
npm install
#直接启动
node app/server.js

#或使用pm2启动，需要提前安装pm2，win平台似乎是不支持pm2的
#pm2 start app/server.js --name starsnode

```

### js客户端

```
# 需要安装 nodejs16 及以上
# 下载release文件中js-client并解压
cd js-client
npm install
#直接启动
node app/client.js

#或使用pm2启动，需要提前安装pm2，win平台似乎是不支持pm2的
#pm2 start app/client.js --name starsnode

```


### qt客户端

#### win平台
下载release文件，直接双击exe启动即可，启动后窗口会关闭，并在后台运行


#### linux平台
下载release文件(如果我打包了的话)
```
	chmod 755 ./qt-websocket
	./qt-websocket
```


### linux客户端

#### 使用编译好的
直接下载解压并执行即可（如果我提供了的话）
	
#### 本地编译
灰常简单。

##### 环境要求
```
	gcc >= 5.4
	c++ >= 14
	cmake >= 3.0
```

```
# 安装常见开发环境
sudo apt-get install build-essential libgl1-mesa-dev

# 安装cmake
sudo apt install cmake

# 安装libcurl
sudo apt-get install libcurl4-openssl-dev

# 安装openssl开发工具
sudo apt-get install libssl-dev
 
# 下载源码并解压
git clone https://github.com/cctyl/starsnode.git
cd starsnode/client/linux-client

#开始编译
mkdir build
cd build
cmake ..
make

#执行
./qt-websocket


```




