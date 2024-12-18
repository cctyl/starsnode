#include"info.h"

//TODO 记得换
#ifdef Q_OS_LINUX
// #ifdef Q_OS_WIN
#include<string.h>
#include <sys/statvfs.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>
#include <linux/hdreg.h>
#include <unistd.h>
#include <netdb.h>
#include <ifaddrs.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <sys/ioctl.h>
#include <linux/if.h>
#include <sys/utsname.h>
#include <time.h>
#include <sys/sysinfo.h>

typedef struct MEMPACKED
{
    char name1[20];
    unsigned long MemTotal;
    char name2[20];
    unsigned long MemFree;
    char name3[20];
    unsigned long Buffers;
    char name4[20];
    unsigned long Cached;

}MEM_OCCUPY;

typedef struct os_line_data
{
    char * val;
    int    len;
} os_line_data;

static char * osGetline(char *sin, os_line_data * line, char delim)
{
    char *out = sin;
    if (*out == '\0') return NULL;
    line->val = out;
    while (*out && (*out != delim)) { out++; }
    line->len = out - line->val;
    if (*out && (*out == delim)) { out++; }
    if (*out == '\0') return NULL;
    return out;
}
void parserEnvInfo(char * buffer,int size ,MEM_OCCUPY * lpMemory)
{
    int    state = 0;
    char * p     = buffer;
    while (p)
    {
        os_line_data       line = { 0 };
        p = osGetline(p, &line, ':');
        if (p == NULL || line.len <= 0) continue;

        if (line.len == 8&& strncmp(line.val, "MemTotal", 8) == 0)
        {
            char *point = strtok(p," ");
            memcpy(lpMemory->name1,"MemTotal",8);
            lpMemory->MemTotal = atol(point);

        }
        else if(line.len == 7&& strncmp(line.val, "MemFree", 7) == 0)
        {
            char *point = strtok(p," ");
            memcpy(lpMemory->name2,"MemFree",7);
            lpMemory->MemFree = atol(point);
        }
        else if(line.len == 7&& strncmp(line.val, "Buffers", 7) == 0)
        {
            char *point = strtok(p," ");
            memcpy(lpMemory->name3,"Buffers",7);
            lpMemory->Buffers = atol(point);
        }
        else if(line.len == 6&& strncmp(line.val, "Cached", 6) == 0)
        {
            char *point = strtok(p," ");
            memcpy(lpMemory->name4,"Cached",6);
            lpMemory->Cached = atol(point);
        }

    }
}

void  getProcmeminfo(MEM_OCCUPY * lpMemory)
{
    FILE *fd;
    char buff[128]={0};
    fd = fopen("/proc/meminfo", "r");
    if(fd==NULL) return ;
    fgets(buff, sizeof(buff), fd);
    // printf("buf=%s\n",buff);
    parserEnvInfo(buff,sizeof(buff),lpMemory);

    fgets(buff, sizeof(buff), fd);
    // printf("buf=%s\n",buff);
    parserEnvInfo(buff,sizeof(buff),lpMemory);

    fgets(buff, sizeof(buff), fd);
    // printf("buf=%s\n",buff);
    parserEnvInfo(buff,sizeof(buff),lpMemory);

    fgets(buff, sizeof(buff), fd);
    // printf("buf=%s\n",buff);
    parserEnvInfo(buff,sizeof(buff),lpMemory);

    fclose(fd);

}

/**
 * 获取内存信息
 *
 * @brief Info::memInfo
 */

void Info::memInfo() {
    MEM_OCCUPY mem;
    memset(&mem,0,sizeof(mem));
    getProcmeminfo(&mem);
    d.memInfo.totalMemMb =formatDouble( mem.MemTotal/1024 );
    d.memInfo.freeMemMb = formatDouble((mem.MemFree+mem.Buffers+mem.Cached)/1024 );
    d.memInfo.usedMemMb =   formatDouble( d.memInfo.totalMemMb -  d.memInfo.freeMemMb);
    d.memInfo.usedMemPercentage =  formatDouble(d.memInfo.usedMemMb/ d.memInfo.totalMemMb *100);
    d.memInfo.freeMemPercentage =  formatDouble(d.memInfo.freeMemMb/ d.memInfo.totalMemMb *100);

}


/**判断str1是否以str2开头
 * 如果是返回1
 * 不是返回0
 * 出错返回-1
 * */
int is_begin_with(const char * str1,char *str2)
{
    if(str1 == NULL || str2 == NULL)
        return -1;
    int len1 = strlen(str1);
    int len2 = strlen(str2);
    if((len1 < len2) ||  (len1 == 0 || len2 == 0))
        return -1;
    char *p = str2;
    int i = 0;
    while(*p != '\0')
    {
        if(*p != str1[i])
            return 0;
        p++;
        i++;
    }
    return 1;
}


void getCpuCountAndName(CpuInfo & c)
{
    FILE *fp = fopen("/proc/cpuinfo", "r");
    if(NULL == fp)
        printf("failed to open cpuinfo\n");
    char szTest[1000] = {0};

    char modelName[256] = {0};
    char cpuCoreNum[64] = {0};

    while(!feof(fp))
    {

        memset(szTest, 0, sizeof(szTest));
        fgets(szTest, sizeof(szTest) - 1, fp); // leave out \n
        if(is_begin_with(szTest,"model name")  && strlen(modelName) <1){

            sscanf(szTest, "%*[^:]: %[^\n]", modelName);
            //printf("b1=%s,\n",modelName);
            //printf("%s", szTest);
            c.cpuModel =  QString::fromUtf8(modelName);
        }


        if(is_begin_with(szTest,"cpu cores")  && strlen(cpuCoreNum) <1){

            sscanf(szTest, "%*[^:]: %[^\n]", cpuCoreNum);
            //printf("b1=%d,\n",atoi(cpuCoreNum));
            c.cpuCount = atoi(cpuCoreNum);
            //printf("%s", szTest);
        }


        if(strlen(cpuCoreNum) >0  && strlen(modelName) >0){
            break;
        }

    }
    fclose(fp);
}


// 定义结构体来存储 CPU 统计信息
typedef struct {
    unsigned long long user;
    unsigned long long nice;
    unsigned long long system;
    unsigned long long idle;
    unsigned long long iowait;
    unsigned long long irq;
    unsigned long long softirq;
    unsigned long long steal;
    unsigned long long guest;
} CpuStat;

// 读取 /proc/stat 文件并解析 CPU 统计信息
int read_cpu_stat(CpuStat *stat) {
    FILE *fp = fopen("/proc/stat", "r");
    if (!fp) {
        perror("fopen");
        return -1;
    }

    char line[512];
    if (fgets(line, sizeof(line), fp) == NULL) {
        perror("fgets");
        fclose(fp);
        return -1;
    }

    // 解析 "cpu" 行
    int ret = sscanf(line, "cpu %llu %llu %llu %llu %llu %llu %llu %llu %llu",
                     &stat->user, &stat->nice, &stat->system, &stat->idle,
                     &stat->iowait, &stat->irq, &stat->softirq, &stat->steal, &stat->guest);

    fclose(fp);

    if (ret != 9) {
        fprintf(stderr, "Failed to parse /proc/stat\n");
        return -1;
    }

    return 0;
}

// 计算 CPU 使用率
double calculate_cpu_usage(CpuStat *prev, CpuStat *curr) {
    unsigned long long total_prev = prev->user + prev->nice + prev->system + prev->idle +
                                    prev->iowait + prev->irq + prev->softirq + prev->steal + prev->guest;
    unsigned long long total_curr = curr->user + curr->nice + curr->system + curr->idle +
                                    curr->iowait + curr->irq + curr->softirq + curr->steal + curr->guest;
    unsigned long long idle_prev = prev->idle + prev->iowait;
    unsigned long long idle_curr = curr->idle + curr->iowait;
    unsigned long long total_diff = total_curr - total_prev;
    unsigned long long idle_diff = idle_curr - idle_prev;
    // 计算 CPU 使用率
    double cpu_usage = (double)(total_diff - idle_diff) / (double)total_diff * 100.0;
    return cpu_usage;
}
static CpuStat prev_stat, curr_stat;


/**
 *
 * 由于需要上一次的频率才能计算出使用率，所以这里必须间隔一秒执行一次
 * @brief refreshCpuUsage
 * @param c
 */
void refreshCpuUsage(CpuInfo & c){

    // 第二次读取 CPU 统计信息
    if (read_cpu_stat(&curr_stat) != 0) {
        fprintf(stderr, "Failed to read updated CPU stats\n");
        return ;
    }
    // 计算 CPU 使用率
    double cpu_usage = calculate_cpu_usage(&prev_stat, &curr_stat);

    //printf("CPU Usage: %.2f%%\n", cpu_usage);
    prev_stat = curr_stat;
    c.cpuUsage = formatDouble(cpu_usage);
}


void Info::cpuInfo(){
    if(firstInit){
        getCpuCountAndName(d.cpuInfo);
    }
    refreshCpuUsage(d.cpuInfo);
}



// 获取指定路径的磁盘空间信息
void get_disk_space(DriverInfo & d) {

    const char *path = "/";
    struct statvfs buf;

    // 调用 statvfs 获取文件系统的统计信息
    if (statvfs(path, &buf) == -1) {
        perror("statvfs");
        return;
    }

    // 计算总空间（字节）
    unsigned long long total_space = (unsigned long long)buf.f_blocks * buf.f_frsize;

    // 计算可用空间（字节）
    unsigned long long available_space = (unsigned long long)buf.f_bavail * buf.f_frsize;

    double total =  (double)total_space / GB;
    double free = (double)available_space / GB;
    // 打印结果
    // printf("Disk space for %s:\n", path);
    // printf("Total space: ");
    // printf("%.2f GB \n",total);


    // printf("Available space: ");
    // printf("%.2f GB \n", free);



    d.usedGb =  formatDouble(total - free);
    d.totalGb = formatDouble(total);
    d.freeGb = formatDouble(free);

    d.usedPercentage = formatDouble(d.usedGb/ d.totalGb *100 );
    d.freePercentage = formatDouble( d.freeGb / d.totalGb *100);
}



void Info::driveInfo(){

    get_disk_space(d.driveInfo);
}


time_t getBootTime() {
    FILE *fp = fopen("/proc/uptime", "r");
    if (!fp) {
        perror("fopen /proc/uptime");
        return -1;
    }

    double uptime_seconds;
    fscanf(fp, "%lf", &uptime_seconds);
    fclose(fp);

    return uptime_seconds;
}

void Info::osInfo(){
    struct utsname buf;

    // 调用 uname 系统调用
    if (uname(&buf) == -1) {
        perror("uname");
        return;
    }



    d.osInfo.type = QString::fromUtf8(buf.sysname);
    d.osInfo.platform ="linux";
    d.osInfo.release =   QString::fromUtf8(buf.release).append(" ")
                           .append(QString::fromUtf8(buf.version));

    d.osInfo.hostname = localmachineName();
    d.osInfo.arch =  QSysInfo::currentCpuArchitecture();
    d.osInfo.uptime = formatDouble(getBootTime());

}




#define PROC_NET_DEV "/proc/net/dev"
#define BUFFER_SIZE 512
//最大网卡数量
#define INTERFACE_NUM 20
//间隔时间
#define ELAPSED_TIME 1

// 结构体用于存储每个网卡的统计数据
typedef struct {
    char name[16];  // 网卡名称
    unsigned long long rx_bytes;  // 接收的字节数
    unsigned long long tx_bytes;  // 发送的字节数
    int flag;//是否初始化
} NetStats;

// 读取 /proc/net/dev 文件并解析数据
void read_net_stats(NetStats *stats, int max_interfaces) {
    FILE *fp = fopen(PROC_NET_DEV, "r");
    if (!fp) {
        perror("fopen /proc/net/dev");
        return;
    }

    char line[BUFFER_SIZE];
    int count = 0;

    // 跳过前两行（标题行）
    fgets(line, sizeof(line), fp);
    fgets(line, sizeof(line), fp);

    int lineno = 1;
    while (fgets(line, sizeof(line), fp) && count < max_interfaces) {
        //printf("第%d行\n",lineno++);
        // 解析网卡名称和统计数据
        char iface_name[16];
        unsigned long long rx_bytes, rx_packets, rx_errs, rx_drop, rx_fifo, rx_frame, rx_compressed, rx_multicast;
        unsigned long long tx_bytes, tx_packets, tx_errs, tx_drop, tx_fifo, tx_colls, tx_carrier, tx_compressed;

        sscanf(line, "%15s %llu %llu %llu %llu %llu %llu %llu %llu %llu %llu %llu %llu %llu %llu %llu",
               iface_name, &rx_bytes, &rx_packets, &rx_errs, &rx_drop, &rx_fifo, &rx_frame,
               &rx_compressed, &rx_multicast, &tx_bytes, &tx_packets, &tx_errs, &tx_drop,
               &tx_fifo, &tx_colls, &tx_carrier, &tx_compressed);

        if(strlen(iface_name)<1){

            continue;
        }else{
            //printf("iface_name=%s\n",iface_name);
        }

        // 去除网卡名称中的冒号
        strcpy(stats[count].name, iface_name);
        stats[count].rx_bytes = rx_bytes;
        stats[count].tx_bytes = tx_bytes;
        stats[count].flag = 1;
        count++;
    }

    fclose(fp);
}

// 计算网卡的上传和下载速率
void calculate_rates(
    NetStats *old_stats,
    NetStats *new_stats,
    std::map<QString,NetstatInfo> & netstatInfo
    ) {
    unsigned long long totalRx = 0;
    unsigned long long totalTx = 0;

    for (int i = 0; i < INTERFACE_NUM; i++) {
        if(!old_stats[i].flag){
            continue;
        }
        unsigned long long rx_diff = new_stats[i].rx_bytes - old_stats[i].rx_bytes;
        unsigned long long tx_diff = new_stats[i].tx_bytes - old_stats[i].tx_bytes;

        // 计算每秒的传输速率（字节/秒）
        double rx_rate = (double)rx_diff / ELAPSED_TIME;//下载
        double tx_rate = (double)tx_diff / ELAPSED_TIME;//上传
        totalRx+=rx_rate;
        totalTx+=tx_rate;
        netstatInfo[QString::fromUtf8(new_stats[i].name)] = {
            formatDouble(rx_rate/MB),formatDouble(tx_rate/MB)
    };

}

netstatInfo["total"] = {
    formatDouble(totalRx/MB),formatDouble(totalTx/MB)
};
}



NetStats old_stats[INTERFACE_NUM];
NetStats new_stats[INTERFACE_NUM];
void Info::netstatInfo(){

    if(firstInit){
        for(int i =0;i<INTERFACE_NUM;i++){
            old_stats[i].flag = 0;
            new_stats[i].flag = 0;
        }
        read_net_stats(old_stats, INTERFACE_NUM);
    }

    read_net_stats(new_stats, INTERFACE_NUM);
    calculate_rates(old_stats,new_stats,d.netstatInfo);

    memcpy(old_stats, new_stats, sizeof(new_stats));

}



#endif
