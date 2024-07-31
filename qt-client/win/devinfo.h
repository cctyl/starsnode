#ifndef DEVINFO_H
#define DEVINFO_H

#include<QString>
#include<QMap>
#include<QJsonObject>
#include"qtjson.hpp"

class CpuInfo{

public:

    double cpuUsage = 0;
    unsigned short cpuCount = 0;
    QString cpuModel;

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

    QString address;
    QString netmask;
    QString family;
    QString mac;
    bool internal;
    REFLECT(address,netmask,family,mac,internal)
};

/**
 * 操作系统相关信息
 * @brief The OsInfo class
 */
class OsInfo{
public:

    QString type = "Windows";//系统类型，Windows 或 Linux
    QString platform = "win";
    QString release;//系统版本
    QString hostname;//机器名
    QString arch;//cpu架构
    QString   uptime;//开机时间


REFLECT(release,hostname,arch,uptime)
};



class DevInfo
{
public:
    DevInfo();
    CpuInfo cpuInfo;
    MemInfo memInfo;
    DriverInfo driveInfo;
    std::map<QString,NetstatInfo> netstatInfo;
    std::map<QString,QJsonArray> netInterface;
    OsInfo osInfo;
    short openedCount = 0;
    QJsonObject ipInfo;

    REFLECT(cpuInfo,memInfo,driveInfo,netstatInfo,netInterface,osInfo,openedCount,ipInfo)
};

#endif // DEVINFO_H
