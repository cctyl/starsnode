#include <QCoreApplication>
#include "info.h"
#include<memory>
#include<QString>
#include"qtjson.hpp"
#include"devinfo.h"


int main(int argc, char *argv[])
{


    QCoreApplication a(argc,argv);

    DevInfo dev;
    std::shared_ptr<Info> info = std::make_shared<Info>();


    // QList<QString> ipList =  info->ip();
    // for(QString & i : ipList){
    //     qDebug().noquote()<<i ;
    // }

    // qDebug().noquote()<<  info->localmachineName();
    // qDebug().noquote()<< info->mac();
    // qDebug().noquote()<< info->cpuType();
    // qDebug().noquote()<<  info->osVersion();
    // qDebug().noquote()<<  info->disk();



    //cpuInfo 的封装
    dev.cpuInfo.cpuUsage =   QString::number(info->measure_cpu_usage(), 'f', 2).toDouble();
    dev.cpuInfo.cpuModel = info->cpuType();
    dev.cpuInfo.cpuCount = info->getCpuCount();
    qDebug().noquote()<<qtjson::serialize(dev.cpuInfo);




    qDebug()<<"启动";
    //int r = a.exec();
    //return r;

    return 0;
}


