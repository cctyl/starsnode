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
#include <QWebSocket>
#include <QUrl>
#include <QTextStream>
class Info : public QObject
{

    Q_OBJECT
public:
    Info();

    ~Info();

    DevInfo d;

    void memInfo();

    void cpuInfo();

    void driveInfo();

    void netstatInfo();

    void netInterface();

    void osInfo();

    void ipInfo();

    void onTextMessageReceived(const QString &message);

    void onConnected();

    void onDisconnected();
private:
    /*
     * 获取计算机名称
     */
    const QString localmachineName();

    const QString mac();

    const QString cpuType();

    const QString osVersion();

    unsigned short getCpuCount();

    double measureCpuUsage();


private:
    QWebSocket * clientSocket;
    QUrl url = QUrl("ws://10.0.8.1:6080/?token=abcdef&type=dev&endpointName=y7000p");

};

#endif // INFO_H
