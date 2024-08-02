#include"info.h"

#ifdef Q_OS_WIN




#define GB (1024.0 * 1024.0 * 1024.0)
#define MB (1024.0 * 1024.0 )
#include "iphlpapi.h"
#include<QThread>
#include<QEventLoop>
#pragma comment(lib, "IPHLPAPI.lib")
#pragma comment(lib,"pdh")
#pragma execution_character_set("utf-8")

/**
 * cpu信息
 * @brief Info::cpuType
 * @return
 */
const QString cpuType()
{
    QSettings *CPU = new QSettings("HKEY_LOCAL_MACHINE\\HARDWARE\\DESCRIPTION\\System\\CentralProcessor\\0",QSettings::NativeFormat);
    QString  m_cpuDescribe = CPU->value("ProcessorNameString").toString();
    delete CPU;

    return m_cpuDescribe;
}

/**
 *
 * 获取操作系统信息
 * @brief osVersion
 * @return
 */
const QString osVersion()
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

unsigned short getCpuCount(){

    SYSTEM_INFO sysInfo;
    GetSystemInfo( &sysInfo );
    return static_cast<unsigned short>(sysInfo.dwNumberOfProcessors);

}
double measureCpuUsage()
{
    static system_time_file_t begin_time{}, end_time{};
    GetSystemTimes(&end_time.idle_time, &end_time.kernel_time, &end_time.user_time);
    auto system_time = end_time.to_uint64_t() - begin_time.to_uint64_t();
    double cpu_usage_percent = 1.0 * (system_time.kernel_time + system_time.user_time - system_time.idle_time) / (system_time.kernel_time + system_time.user_time) * 100;

    //更新下上次使用率
    GetSystemTimes(&begin_time.idle_time, &begin_time.kernel_time, &begin_time.user_time);

    return cpu_usage_percent;
}


void Info::netstatInfo(){

    PMIB_IFTABLE pTable = nullptr;
    DWORD dword = 0;
    ULONG retCode = GetIfTable(pTable, &dword, true);
    if(retCode == ERROR_NOT_SUPPORTED)
        return;
    if(retCode == ERROR_INSUFFICIENT_BUFFER)
        pTable = (PMIB_IFTABLE)new BYTE[65535];
    //上传速度
    DWORD dwIn = 0;
    //下载速度
    DWORD dwOut = 0;
    //上传最后字节
    static DWORD dwLastIn = 0;
        //下载最后字节
    static DWORD dwLastOut = 0;

    GetIfTable(pTable, &dword, true);
    DWORD dwInOc = 0;
    DWORD dwOutOc = 0;
    for(UINT i = 0; i < pTable->dwNumEntries; i++)
    {
        MIB_IFROW row = pTable->table[i];
        dwInOc += row.dwInOctets;
        dwOutOc += row.dwOutOctets;
    }
    dwIn = dwInOc - dwLastIn;
    dwOut = dwOutOc - dwLastOut;

    if(dwLastIn <= 0)
        dwIn = 0;
    else
        dwIn = dwIn / 1024;

    if(dwLastOut <= 0)
        dwOut = 0;
    else
        dwOut = dwOut / 1024;

    dwLastIn = dwInOc;
    dwLastOut = dwOutOc;

    double in = static_cast<double>(dwIn) /1024/8;
    double out = static_cast<double>(dwOut) /1024/8;
    //qDebug()<<"in="<<in<<",out="<<out;


    d.netstatInfo["total"] = {
        formatDouble(out),formatDouble(in)
    };

    delete [] pTable;


}
void Info::driveInfo(){
    QFileInfoList list = QDir::drives();
    double total = 0.0;
    double free = 0.0;
    foreach (QFileInfo dir, list)
    {
        QString dirName = dir.absolutePath();
        dirName.remove("/");
        // qDebug()<<dirName;
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
    d.memInfo.usedMemMb =   formatDouble( d.memInfo.totalMemMb -  d.memInfo.freeMemMb);
    d.memInfo.usedMemPercentage =  formatDouble(d.memInfo.usedMemMb/ d.memInfo.totalMemMb *100);
    d.memInfo.freeMemPercentage =  formatDouble(d.memInfo.freeMemMb/ d.memInfo.totalMemMb *100);
}

double getStartTime(){
    DWORD iRunTime = GetTickCount();
    const int Num1 = 1000;
    time_t nowTime;
    time(&nowTime);
    time_t systemUpTime = nowTime - (iRunTime / Num1);
    return (nowTime - systemUpTime)/60.0/60;

}
void Info::osInfo(){

    d.osInfo.type = "Windows";
    d.osInfo.platform ="win";
    d.osInfo.release = osVersion();
    d.osInfo.hostname = localmachineName();
    d.osInfo.arch =  QSysInfo::currentCpuArchitecture();
    d.osInfo.uptime = formatDouble(getStartTime());
}

void Info::cpuInfo(){
    d.cpuInfo.cpuUsage =   QString::number(measureCpuUsage(), 'f', 2).toDouble();
    d.cpuInfo.cpuModel = cpuType();
    d.cpuInfo.cpuCount = getCpuCount();
}






#endif


