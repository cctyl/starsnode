#ifndef INFO_H
#define INFO_H
#include "devinfo.h"
#include<string>
#include "easywsclient.hpp"
#define GB (1024.0 * 1024.0 * 1024.0)
#define MB (1024.0 * 1024.0 )
class Info {


public:
    Info();

    ~Info();

    void initConfig();

    DevInfo d;

    // 通用实现

    void netInterface();

    void ipInfo();

    void refreshDataFast();

    easywsclient::WebSocket::pointer openWs(std::string &url);

    /*
     * 获取计算机名称
     */
    const std::string localmachineName();

    // 不同操作系统分别实现
    void memInfo();

    void cpuInfo();

    void driveInfo();

    void netstatInfo();

    void osInfo();
    bool firstInit = true;
    easywsclient::WebSocket::pointer ws = nullptr;
    std::string urlStr;
};

double formatDouble(double source);

#endif // INFO_H
