#include <QCoreApplication>
#include "info.h"
#include<memory>
#include<QString>
#include"qtjson.hpp"
#include"devinfo.h"

QDebug operator<<(QDebug debug, const NetInterfaceInfo &p){

    debug.nospace() << "NetInterfaceInfo";
    return debug;
}



int main(int argc, char *argv[])
{


    QCoreApplication a(argc,argv);


    std::shared_ptr<Info> info = std::make_shared<Info>();





    int r = a.exec();
    return r;

    // return 0;
}

