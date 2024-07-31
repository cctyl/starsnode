#include <QCoreApplication>
#include "info.h"
#include<memory>
#include<QString>
#include"qtjson.hpp"
#include"devinfo.h"


int main(int argc, char *argv[])
{


    QCoreApplication a(argc,argv);


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
    info->cpuInfo();
    qDebug().noquote()<<qtjson::serialize(info->d.cpuInfo);


    //memInfo

    info->memInfo();
    qDebug().noquote()<<qtjson::serialize(info->d.memInfo);

    qDebug()<<"启动";









    //int r = a.exec();
    //return r;

    return 0;
}


