#include <QCoreApplication>
#include "info.h"
#include<memory>
#include<QString>
#include"qtjson.hpp"
#include"devinfo.h"
#include<QThread>
#include <filesystem>
#include<iostream>



int main(int argc, char *argv[])
{


    QCoreApplication a(argc,argv);


    std::shared_ptr<Info> info = std::make_shared<Info>();


    // while(true){

    //     // //cpuInfo 的封装
    //     // info->cpuInfo();
    //     // qDebug().noquote()<<qtjson::serialize(info->d.cpuInfo);


    //     // //memInfo
    //     //info->memInfo();
    //     //qDebug().noquote()<<qtjson::serialize(info->d.memInfo);


    //     // driverInfo
    //     //info->driveInfo();
    //     //qDebug().noquote()<<qtjson::serialize(info->d.driveInfo);

    //     //netstatInfo
    //     // info->netstatInfo();
    //     // qDebug().noquote()<<qtjson::serialize(info->d.netstatInfo);

    //     //netInterface
    //    // info-> netInterface();
    //         // qDebug().noquote()<<qtjson::serialize(info->d.netInterface);

    //     //osInfo
    //     info-> osInfo();
    //     qDebug().noquote()<<qtjson::serialize(info->d.osInfo);


    //     sleep(1);
    //     info->firstInit = false;
    // }



    int r = a.exec();
    return r;


    // return 0;
}

