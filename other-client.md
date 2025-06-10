
# 其他客户端的安装配置
QT客户端和C++Linux客户端已经被rust客户端统一代替，不推荐使用。这里提供之前的打包方式

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




## 安装

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
