#include "info.h"
#include<iostream>
#define GB (1024.0 * 1024.0 * 1024.0)
#define MB (1024.0 * 1024.0 )



struct system_time_t
{
    uint64_t idle_time, kernel_time, user_time;
    system_time_t operator-(system_time_t other) {
        return {
            .idle_time = idle_time - other.idle_time,
            .kernel_time = kernel_time - other.kernel_time,
            .user_time = user_time - other.user_time,
        };
    }
};

struct system_time_file_t
{
    FILETIME idle_time, kernel_time, user_time;
    system_time_t to_uint64_t() {
        system_time_t time{};
        time.idle_time = ((uint64_t)idle_time.dwHighDateTime << 32) | idle_time.dwLowDateTime;
        time.kernel_time = ((uint64_t)kernel_time.dwHighDateTime << 32) | kernel_time.dwLowDateTime;
        time.user_time = ((uint64_t)user_time.dwHighDateTime << 32) | user_time.dwLowDateTime;
        return time;
    }
};
/**
 * 保留两位小数
 * @brief formatDouble
 * @param source
 * @return
 */
double formatDouble(double source){
    return QString::number(source, 'f', 2).toDouble();
}

unsigned short Info::getCpuCount(){

    SYSTEM_INFO sysInfo;
    GetSystemInfo( &sysInfo );
    return static_cast<unsigned short>(sysInfo.dwNumberOfProcessors);

}
double Info::measureCpuUsage()
{
    static system_time_file_t begin_time{}, end_time{};
    GetSystemTimes(&end_time.idle_time, &end_time.kernel_time, &end_time.user_time);
    auto system_time = end_time.to_uint64_t() - begin_time.to_uint64_t();
    double cpu_usage_percent = 1.0 * (system_time.kernel_time + system_time.user_time - system_time.idle_time) / (system_time.kernel_time + system_time.user_time) * 100;

    //更新下上次使用率
    GetSystemTimes(&begin_time.idle_time, &begin_time.kernel_time, &begin_time.user_time);

    return cpu_usage_percent;
}

Info::Info() {


    // QTimer * timer = new QTimer(this);

    // connect(
    //     timer,
    //     &QTimer::timeout,
    //     [&]{
    //         measure_cpu_usage();
    //         ramInfo();
    //     }
    //     );

    // timer->start(1000);//间隔的秒数
}


void Info::cpuInfo(){

    d.cpuInfo.cpuUsage =   QString::number(measureCpuUsage(), 'f', 2).toDouble();
    d.cpuInfo.cpuModel = cpuType();
    d.cpuInfo.cpuCount = getCpuCount();
}


void Info::driveInfo(){
    QFileInfoList list = QDir::drives();
    double total = 0.0;
    double free = 0.0;
    foreach (QFileInfo dir, list)
    {
        QString dirName = dir.absolutePath();
        dirName.remove("/");
        qDebug()<<dirName;
        LPCWSTR lpcwstrDriver = (LPCWSTR)dirName.utf16();
        ULARGE_INTEGER liFreeBytesAvailable, liTotalBytes, liTotalFreeBytes;

        if(GetDiskFreeSpaceEx(lpcwstrDriver, &liFreeBytesAvailable, &liTotalBytes, &liTotalFreeBytes) )
        {
            free+= QString::number((double) liTotalFreeBytes.QuadPart / GB, 'f', 1).toDouble();
            total+= QString::number((double) liTotalBytes.QuadPart /  GB, 'f', 1).toDouble();
        }
    }

    d.driveInfo.usedGb =  formatDouble(total - free);
    d.driveInfo.totalGb = formatDouble(total);
    d.driveInfo.freeGb = formatDouble(free);

    d.driveInfo.usedPercentage = formatDouble(d.driveInfo.usedGb/ d.driveInfo.totalGb *100 );
    d.driveInfo.freePercentage = formatDouble( d.driveInfo.freeGb / d.driveInfo.totalGb *100);

}


void Info::memInfo()
{
    MEMORYSTATUSEX statex;
    statex.dwLength = sizeof (statex);
    GlobalMemoryStatusEx(&statex);

    d.memInfo.totalMemMb =formatDouble( statex.ullTotalPhys  * 1.0 / MB);
    d.memInfo.freeMemMb = formatDouble(statex.ullAvailPhys * 1.0 / MB);
    d.memInfo.usedMemMb =    d.memInfo.totalMemMb -  d.memInfo.freeMemMb;
    d.memInfo.usedMemPercentage =  formatDouble(d.memInfo.usedMemMb/ d.memInfo.totalMemMb *100);
    d.memInfo.freeMemPercentage =  formatDouble(d.memInfo.freeMemMb/ d.memInfo.totalMemMb *100);
}

/*
 *
 * 计算机名
 */
const QString Info::localmachineName()
{
    QString machineName = QHostInfo::localHostName();
    return machineName;
}

/*
 * 本机局域网ip
 */
const  QList<QString> Info::ip()
{
    QString ip="";

    QList<QString> ipList;
    QList<QNetworkInterface> interFaceList = QNetworkInterface::allInterfaces();
    for(int i=0; i< interFaceList.size(); i++)
    {
        QNetworkInterface f = interFaceList.at(i);
        if(f.flags().testFlag(QNetworkInterface::IsRunning))
        {

            qDebug()<< "===========QNetworkInterface============";
            qDebug()<< f.hardwareAddress();
            qDebug()<< f.humanReadableName();
            qDebug()<< f.name();
            qDebug()<< f.index();

            QList<QNetworkAddressEntry> entryList = f.addressEntries();
            foreach(QNetworkAddressEntry entry, entryList)
            {


                if(entry.ip() != QHostAddress::LocalHost )
                {
                    qDebug()<< "===========QNetworkInterface  QNetworkAddressEntry============";

                    qDebug()<< entry.ip();
                    qDebug()<< entry.ip().protocol();
                    qDebug()<< entry.netmask().toString();
                    qDebug()<< entry.broadcast().toString();

                    if(QAbstractSocket::IPv4Protocol == entry.ip().protocol()){
                        qDebug()<<"ipv4";


                    }else{
                        qDebug()<<"ipv6";

                    }

                    ip = entry.ip().toString();



                    ipList.push_back(ip);
                }
            }
        }
    }

    return ipList;
}




/**
 * mac 地址
 * @brief Info::mac
 * @return
 */
const QString Info::mac()
{
    QString strMac;

    QList<QNetworkInterface> netList = QNetworkInterface::allInterfaces();
    foreach(QNetworkInterface item, netList)
    {
        if((QNetworkInterface::IsUp & item.flags()) && (QNetworkInterface::IsRunning & item.flags()))
        {
            if(strMac.isEmpty() || strMac < item.hardwareAddress())
            {
                strMac = item.hardwareAddress();
            }
        }
    }
    return strMac;
}


/**
 * cpu信息
 * @brief Info::cpuType
 * @return
 */
const QString Info::cpuType()
{
    QSettings *CPU = new QSettings("HKEY_LOCAL_MACHINE\\HARDWARE\\DESCRIPTION\\System\\CentralProcessor\\0",QSettings::NativeFormat);
    QString  m_cpuDescribe = CPU->value("ProcessorNameString").toString();
    delete CPU;

    return m_cpuDescribe;
}





/**
 *
 * 获取操作系统信息
 * @brief Info::osVersion
 * @return
 */
const QString Info::osVersion()
{
    typedef BOOL (WINAPI *LPFN_ISWOW64PROCESS) (HANDLE, PBOOL);
    LPFN_ISWOW64PROCESS fnIsWow64Process;
    BOOL bIsWow64 = FALSE;
    fnIsWow64Process = (LPFN_ISWOW64PROCESS)GetProcAddress( GetModuleHandle(TEXT("kernel32")),"IsWow64Process");
    if (NULL != fnIsWow64Process)
    {
        fnIsWow64Process(GetCurrentProcess(),&bIsWow64);
    }
    QString sysBit = "unknown";
    if(bIsWow64)
        sysBit = "64位";
    else
        sysBit = "32位";

    QString m_osDescirbe = QSysInfo::prettyProductName() + " " + sysBit;
    return m_osDescirbe;
}



