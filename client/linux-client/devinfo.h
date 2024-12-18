#ifndef DEVINFO_H
#define DEVINFO_H

#include<string>
#include<map>
#include "reflect.hpp"
#include "reflect_json.hpp"
class CpuInfo{

public:

    double cpuUsage = 0;
    unsigned short cpuCount = 0;
    std::string cpuModel;

    REFLECT(cpuUsage,cpuCount,cpuModel)
};

class MemInfo{


public:
    double totalMemMb;
    double usedMemMb;
    double freeMemMb;
    double usedMemPercentage;
    double freeMemPercentage;

    REFLECT(totalMemMb,usedMemMb,freeMemMb,usedMemPercentage,freeMemPercentage)
};
class DriverInfo{

public:
    double totalGb;
    double usedGb;
    double   freeGb ;
    double usedPercentage;
    double freePercentage;
    REFLECT(totalGb,usedGb,freeGb,usedPercentage,freePercentage)
};

/**
 * 网速相关信息
 * @brief The NetstatInfo class
 */
class NetstatInfo{
public:
    double inputMb;
    double outputMb;
    REFLECT(inputMb,outputMb)
};

/**
 * 网卡信息
 *
 * @brief The NetInterfaceInfo class
 */
class NetInterfaceInfo{
public:

    std::string address;
    std::string netmask;
    std::string family;
    std::string mac;
    std::string boardcast;
    bool internal;
    REFLECT(address,netmask,family,mac,internal,boardcast)



};

/**
 * 操作系统相关信息
 * @brief The OsInfo class
 */
class OsInfo{
public:

    std::string type = "Windows";//系统类型，Windows 或 Linux
    std::string platform = "win";
    std::string release;//系统版本
    std::string hostname;//机器名
    std::string arch;//cpu架构
    double   uptime;//开机时间


REFLECT(type,platform,release,hostname,arch,uptime)
};



class DevInfo
{
public:
    DevInfo();
    CpuInfo cpuInfo;
    MemInfo memInfo;
    DriverInfo driveInfo;
    std::map<std::string,NetstatInfo> netstatInfo;
    std::map<std::string,std::vector<NetInterfaceInfo>> netInterface;
    OsInfo osInfo;
    short openedCount = 0;
    Json::Value ipInfo;

    REFLECT(cpuInfo,memInfo,driveInfo,netstatInfo,netInterface,osInfo,openedCount,ipInfo)
};

#endif // DEVINFO_H
