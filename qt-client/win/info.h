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






    //通用实现

    void netInterface();

    void ipInfo();

    void onTextMessageReceived(const QString &message);

    void onConnected();

    void onDisconnected();


    // 不同操作系统分别实现
    void memInfo();

    void cpuInfo();

    void driveInfo();

    void netstatInfo();

    void osInfo();




private:
    /*
     * 获取计算机名称
     */
    const QString localmachineName();





private:
    QWebSocket * clientSocket;
    QString urlStr = "ws://10.0.8.1:6080/?token=abcdef&type=dev&endpointName=";
    QUrl url;
};

double formatDouble(double source);

#endif // INFO_H
