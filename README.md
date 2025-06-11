# starsnode

## 简介

[starsnode]是一个系统状态监控软件。

展示的信息包括：cpu、内存、硬盘、网速、网卡等信息。

### 优点

#### 高性能

rust编写的原生客户端，只占用%0.几的cpu，最低3.9M的内存(不开启ip信息的情况，开启ipinfo 需要使用openssl会带来一定的内存开销)
(c++原生客户端和qt客户端不再推荐)

#### 轻量级

软件小，依赖少，编译十分简单，同时提供编译好的客户端

#### 跨平台

nodejs客户端几乎适配所有主流系统，win、linux、macos、android。
如要求性能，也可使用rust客户端，同样支持跨平台

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

统一推荐rust客户端。有良好的跨平台支持

~~win平台，使用qt客户端。~~ 

~~linux平台，如果内核版本比较新，比如ubuntu22以上，建议使用qt客户端。~~ 

~~如果是历史平台，比如centos系列，建议使用linux客户端，缺点是稳定性可能稍差。~~ 

~~macos，理论上也可以使用linux客户端，但是编译是个问题~~ 


## 性能展示

### rust客户端




#### win10 8c 16g x64
win10实测，软件大小只有3.67M, 只占用6m左右的内存，百分之0.3的cpu。
<p align="center">
    <img src="./img/rust_win10_8c_16g_x64.png" >
</p>


#### debian 2c 4g x64
开启ip信息后，占11M左右，0.3%cpu

<p align="center">
    <img src="./img/rust_debian_2c_4g_x64.png" >
</p>

#### armbian 4c 4g x64

开启ip信息后，占7M左右，0.3%cpu

<p align="center">
    <img src="./img/rust_armbian_4c_4g_x64.png" >
</p>
## 部署方式




### 配置
服务端的配置文件在 `server/config.js`，只需要设置port和token即可。




nodejs客户端的配置文件，在其目录下的config.js 中，需要设置服务端地址(serverHost)，端口(port)，token。
endpointName可省略，这是用于给客户端命名的。



rust配置文件，在各自目录下的 config.json 文件中，需要设置服务端地址(server)，端口(port)，token。
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

### Rust客户端

#### win环境
我已经提供了打包后的exe文件，直接到release下载即可，双击启动即可

#### 其他平台
- 安装rust

- 执行 `cargo build --release` ，然后执行生成的可执行文件
注意，国内依赖下载可能比较慢，可以配置一下国内镜像来加速下载






### qt客户端
不再推荐使用，如有需要，参考 other-client.md
### linux客户端
不再推荐使用，如有需要，参考 other-client.md




