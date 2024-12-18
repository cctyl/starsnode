
#include "info.h"
#include<memory>
#include"devinfo.h"
#include<iostream>
#include <unistd.h>
#include "reflect_json.hpp"


int main(int argc, char *argv[]) {


    std::shared_ptr<Info> info = std::make_shared<Info>();


//     while(true){
//
//         std::cout<<"循环"<<std::endl;
//
//         // //cpuInfo 的封装
////          info->cpuInfo();
////          std::cout<<reflect_json::serialize(info->d.cpuInfo)<<std::endl;
////
//
//          //memInfo
////         info->memInfo();
////         std::cout<<reflect_json::serialize(info->d.memInfo);
//
//
////          driverInfo
////         info->driveInfo();
////         std::cout<<reflect_json::serialize(info->d.driveInfo);
//
////         netstatInfo
////          info->netstatInfo();
////          std::cout<<reflect_json::serialize(info->d.netstatInfo);
//
//         //netInterface
////         info-> netInterface();
////              std::cout<<reflect_json::serialize(info->d.netInterface);
//
//         //osInfo
////         info-> osInfo();
////         std::cout<<reflect_json::serialize(info->d.osInfo);
////
//         sleep(1);
//         info->firstInit = false;
//     }



    std::cout << "主线程退出" << std::endl;


    return 0;
}

