# starsnode

## 简介

[starsnode]是一个系统状态监控软件。

展示的信息包括：cpu、内存、硬盘、网速、网卡等信息。

### 优点

#### 高性能

c++编写的原生客户端和qt客户端

#### 轻量级

软件小，依赖少，编译十分简单，同时提供编译好的客户端

#### 跨平台

nodejs客户端几乎适配所有主流系统，win、linux、macos。
如要求性能，也可使用qt客户端，同样支持跨平台

本库的js服务端参考了该项目：https://github.com/chaos-zhu/easynode，感谢原作者。

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

win和mac平台，使用qt客户端。

linux平台，如果内核版本比较新，比如ubuntu22以上，建议使用qt客户端。
如果是历史平台，比如centos系列，建议使用linux客户端，缺点是稳定性可能稍差。

## 性能展示

### qt客户端

win10以及win11实测，只占用2m左右的内存，百分之0.几的cpu

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


待完成。。。
