#ifndef INFO_H
#define INFO_H
#include "devinfo.h"
#include <QDebug>
#include <QDir>
#include <QFileInfoList>
#include <QHostAddress>
#include <QHostInfo>
#include <QLibrary>
#include <QNetworkAccessManager>
#include <QNetworkInterface>
#include <QNetworkReply>
#include <QNetworkRequest>
#include <QObject>
#include <QSettings>
#include <QString>
#include <QSysInfo>
#include <QTextStream>
#include <QTimer>
#include <QUrl>
#include <QWebSocket>

#define GB (1024.0 * 1024.0 * 1024.0)
#define MB (1024.0 * 1024.0 )
class Info : public QObject {

    Q_OBJECT
public:
    Info();

    ~Info();

    void initConfig();

    DevInfo d;

    // 通用实现

    void netInterface();

    void ipInfo();

    void onTextMessageReceived(const QString &message);

    void onConnected();

    void onDisconnected();

    /*
     * 获取计算机名称
     */
    const QString localmachineName();

    // 不同操作系统分别实现
    void memInfo();

    void cpuInfo();

    void driveInfo();

    void netstatInfo();

    void osInfo();
    bool firstInit = true;
private:
    QWebSocket *clientSocket;
    QString urlStr;
    QUrl url;
};

double formatDouble(double source);

#endif // INFO_H
