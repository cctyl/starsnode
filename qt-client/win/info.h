#ifndef INFO_H
#define INFO_H
#include<QString>
#include <QHostAddress>
#include <QNetworkInterface>
#include <QNetworkAccessManager>
#include <QNetworkRequest>
#include <QNetworkReply>
#include <QSysInfo>
#include <QSettings>
#include <QDebug>
#include <QFileInfoList>
#include <QDir>
#include <QLibrary>
#include <QTimer>
#include <QHostInfo>
#include <Windows.h>
#include<QObject>
#include "devinfo.h"

class Info : public QObject
{

    Q_OBJECT
public:
    Info();

    DevInfo d;


    void memInfo();

    void cpuInfo();

    /*
     * 获取计算机名称
     */
    const QString localmachineName();

    const QList<QString> ip();

    const QString mac();

    const QString cpuType();



    const QString osVersion();

    const QString disk();

    unsigned short getCpuCount();


    double measureCpuUsage();
};

#endif // INFO_H
