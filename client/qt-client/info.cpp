#include "info.h"
#include<QThread>
#include<QEventLoop>

/**
 * 保留两位小数
 * @brief formatDouble
 * @param source
 * @return
 */
double formatDouble(double source){
    return QString::number(source, 'f', 2).toDouble();
}


QString readConfigFile(QString & path){
    QFile file(path);
    if (!file.open(QIODevice::ReadOnly | QIODevice::Text)) {
        qDebug() << "Failed to open file:" << path;
        return QString();
    }

    // 一次性读取文件的全部内容
    QByteArray content = file.readAll();

    // 关闭文件
    file.close();

    // 将 QByteArray 转换为 QString
    return  QString(content);
}


/**
 * 接收到服务端返回值
 *
 * @brief Info::onTextMessageReceived
 * @param message
 */
void Info::onTextMessageReceived(const QString &message){

    qDebug()<<"接收到服务端消息:"<<message;

}


void Info::onConnected(){
    qDebug()<<"连接成功";
}
void Info::onDisconnected(){
    qDebug()<<"断开连接";
    QThread::msleep(1000);
    qDebug()<<"重试...";
    clientSocket->close();
    clientSocket->open(url);
}
Info::~Info(){

    // 先关闭旧连接
    if (clientSocket) {
        clientSocket->close();
        delete clientSocket;
        clientSocket = nullptr;
    }

}


void Info::initConfig(){
    QString configPath = "config.json";
    if(!QFileInfo(configPath).exists()){
        qDebug()<<"文件不存在";
        return;
    }

    QString jsonStr =  readConfigFile(configPath);
    qDebug().noquote()<<jsonStr;
    QJsonObject obj = qtjson::strToJson(jsonStr).toObject();

    if(!( obj.contains("server")
          &&
          obj.contains("port")
          &&
          obj.contains("token")
          )){

        qDebug()<<"文件格式异常";
        return ;
    }

    this->urlStr= QString("ws://%1:%2/?token=%3&type=dev&endpointName=%4")
                       .arg(obj.value("server").toString())
                       .arg(obj.value("port").toInt())
                       .arg(obj.value("token").toString())
                       .arg(localmachineName())
        ;

    qDebug()<<"初始化连接:"<<this->urlStr;


}
Info::Info() {

    this->initConfig();
    if(this->urlStr.isEmpty()){
        qDebug()<<"初始化失败";
        exit(0);
        return;
    }
    clientSocket = new QWebSocket();

    qDebug()<<"打开链接";
    url = QUrl(urlStr);
    clientSocket->open(url);

    QObject::connect(clientSocket,
                     &QWebSocket::connected,
                     this,
                     &Info::onConnected
                     );

    QObject::connect(clientSocket,
                     &QWebSocket::disconnected,
                     this,
                     &Info::onDisconnected
                     );

    QObject::connect(clientSocket, &QWebSocket::textMessageReceived,this,&Info::onTextMessageReceived);

    //等待连接成功后，才开始发送信息
    QEventLoop loop;
    QObject::connect(clientSocket,  &QWebSocket::connected, &loop, &QEventLoop::quit);
    loop.exec();

    qDebug()<<"启动定时任务";

    //启动时更新一次ip信息
    ipInfo();

    QTimer * timer = new QTimer(this);

    connect(
        timer,
        &QTimer::timeout,
        [&]{

            // //cpuInfo 的封装
            cpuInfo();
            // qDebug().noquote()<<qtjson::serialize(info->d.cpuInfo);


            // //memInfo
            memInfo();
            // qDebug().noquote()<<qtjson::serialize(info->d.memInfo);


            // driverInfo
            driveInfo();
            // qDebug().noquote()<<qtjson::serialize(info->d.driveInfo);

            //netstatInfo
            netstatInfo();
            //qDebug().noquote()<<qtjson::serialize(info->d.netstatInfo);

            //netInterface
            netInterface();
                // qDebug().noquote()<<qtjson::serialize(info->d.netInterface);

            //osInfo
            osInfo();
            //qDebug().noquote()<<qtjson::serialize(info->d.osInfo);

            clientSocket->sendTextMessage(qtjson::serialize(d));


            if(firstInit){
                firstInit = false;
            }
        }
        );

    timer->start(3000);//间隔的毫秒值




    QTimer * slowTimer = new QTimer(this);
    connect(
        slowTimer,
        &QTimer::timeout,
        [&]{

            //一小时更新一次ip地址
            ipInfo();
            qDebug().noquote()<< "更新ip信息"<<qtjson::serialize(d.ipInfo);

        }
        );

    slowTimer->start(3600000);//间隔的秒数 3600秒

}


void Info::ipInfo(){

    QNetworkAccessManager manager;

    QUrl url("https://api.qjqq.cn/api/Local");
    QNetworkRequest request(url);

    // 发送GET请求
    QNetworkReply *reply = manager.get(request);

    // 等待请求完成
    QEventLoop loop;
    QObject::connect(reply, &QNetworkReply::finished, &loop, &QEventLoop::quit);
    loop.exec();

    // 检查请求状态
    if (reply->error() == QNetworkReply::NoError) {
        QByteArray data = reply->readAll();
        QJsonDocument doc = QJsonDocument::fromJson(data);
        QJsonObject root = doc.object();
        QJsonObject dataObj = root["data"].toObject();

        dataObj.insert("time", root["time"] );

        d.ipInfo = dataObj;
    } else {
        qDebug() << "Error:" << reply->errorString();
    }

    reply->deleteLater();



}
/*
 *
 * 计算机名
 */
const QString Info::localmachineName()
{
    QString machineName = QHostInfo::localHostName();
    return machineName;
}

/*
 * 本机局域网ip
 */
void Info::netInterface()
{

    QList<QNetworkInterface> interFaceList = QNetworkInterface::allInterfaces();
    for(int i=0; i< interFaceList.size(); i++)
    {
        QNetworkInterface f = interFaceList.at(i);
        if(f.flags().testFlag(QNetworkInterface::IsRunning))
        {
            std::vector<NetInterfaceInfo> v;



            // qDebug()<< "===========QNetworkInterface============";
            // qDebug()<< f.hardwareAddress();
            // qDebug()<< f.humanReadableName();
            // qDebug()<< f.name();
            // qDebug()<< f.index();

            QList<QNetworkAddressEntry> entryList = f.addressEntries();
            foreach(QNetworkAddressEntry entry, entryList)
            {


                if(entry.ip() != QHostAddress::LocalHost )
                {
                    // qDebug()<< "===========QNetworkInterface  QNetworkAddressEntry============";

                    // qDebug()<< entry.ip().toString();
                    // qDebug()<< entry.ip().protocol();
                    // qDebug()<< entry.netmask().toString();
                    // qDebug()<< entry.broadcast().toString();


                    QString family;
                    if(QAbstractSocket::IPv4Protocol == entry.ip().protocol()){
                        family = "IPv4";



                    }else{
                        family = "IPv6";


                    }

                    v.push_back({
                        entry.ip().toString(),
                        entry.netmask().toString(),
                        family,
                        f.hardwareAddress(),
                        entry.broadcast().toString(),
                        true,


                    });


                }
            }


            d.netInterface[f.humanReadableName()] = v;
        }
    }


}





