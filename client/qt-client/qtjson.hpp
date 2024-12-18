#ifndef QTJSON_H
#define QTJSON_H

#include"reflect.hpp"
#include<QString>
#include<QMap>
#include <QJsonObject>
#include <QJsonDocument>
#include <QStringList>
#include<QJsonArray>
#include<vector>
#include<map>
#include<QDebug>


namespace qtjson {


// 1.===========================对象与json对象的转换=================================================
template <class T>
struct special_traits {
    static constexpr bool value = false;
};

template <class T, std::enable_if_t<!reflect::has_member<T>() && !special_traits<T>::value, int> = 0>
QJsonValue objToJson(T const &object) {
    return object;
}

template <class T, std::enable_if_t<!reflect::has_member<T>() && special_traits<T>::value, int> = 0>
QJsonValue objToJson(T const &object) {
    return special_traits<T>::objToJson(object);
}

template <class T, std::enable_if_t<reflect::has_member<T>(), int> = 0>
QJsonObject objToJson(T const &object) {
    QJsonObject root;
    reflect::foreach_member(object, [&](const char *key, auto &value) {
        root[key] = objToJson(value);
    });
    return root;
}



template <class T, std::enable_if_t<!reflect::has_member<T>() && !special_traits<T>::value, int> = 0>
T jsonToObj(QJsonValue const &root) {

    static_assert(std::is_same<T, T>::value, "Type not supported");
    qDebug()<<"不支持转换的类型:"<<root;
    return T(); // 返回默认构造的 T 对象
}



template <class T, std::enable_if_t<!reflect::has_member<T>() && special_traits<T>::value, int> = 0>
T jsonToObj(QJsonValue const &root) {
    return special_traits<T>::jsonToObj(root);
}

template <class T, std::enable_if_t<reflect::has_member<T>(), int> = 0>
T jsonToObj(QJsonValue const &root) {
    T object;
    reflect::foreach_member(object, [&](const char *key, auto &value) {
        qDebug()<<"jsonToObj==》 key="<<key;
        value = jsonToObj<std::decay_t<decltype(value)>>(root[key]);

    });
    return object;
}

/**
 * @brief jsonArrayToVector
 * @param arr
 * @return
 */
template<class T>
std::vector<T> jsonArrayToVector(QJsonArray & arr){
    std::vector<T> v;

    for (const QJsonValue &value : arr) {
        T t = jsonToObj<T>(value);
        v.push_back(t);
    }
    return v;
}


// 2.============================字符串与json对象的转换=========================================


/**
 * QJsonObject -> QString
 * @brief jsonToStr
 * @param obj
 * @return
 */
inline QString jsonToStr(QJsonObject obj) {
    // 转换为 QJsonDocument
    QJsonDocument doc(obj);

    // 将 QJsonDocument 转换为 QByteArray
    QByteArray jsonData = doc.toJson(QJsonDocument::Indented); // 使用 Indented 格式化输出

    // 将 QByteArray 转换为 QString
    return QString::fromUtf8(jsonData);
}

inline QString jsonToStr(QJsonArray array) {

    // 创建一个 QJsonDocument，并将 QJsonArray 设置为其根节点
    QJsonDocument doc(array);

    // 将 QJsonDocument 转换为 QByteArray
    QByteArray jsonData = doc.toJson(QJsonDocument::Indented); // 使用 Indented 格式化输出

    // 将 QByteArray 转换为 QString
    return QString::fromUtf8(jsonData);
}
inline QString jsonToStr(QJsonValue value) {
    if(value.isObject()){
        return jsonToStr(value.toObject());
    }else {
        return jsonToStr(value.toArray());
    }
}
/**
 * QString -> QJsonObject
 * @brief strToJson
 * @param jsonString
 * @return
 */
inline QJsonValue strToJson(QString const &jsonString) {
    QJsonParseError parseError;
    QJsonDocument jsonDoc = QJsonDocument::fromJson(jsonString.toUtf8(), &parseError);

    // 检查是否解析成功
    if (parseError.error != QJsonParseError::NoError) {
        qDebug() << "Parse error at offset" << parseError.offset << ":" << parseError.errorString();
        return QJsonObject();
    }

    if(jsonDoc.isObject()){
        // 获取 QJsonObject
        return jsonDoc.object();
    }else if(jsonDoc.isArray()){

        return jsonDoc.array();
    }else{
        return QJsonValue();
    }

}


// 3.==================自定义特化扩展============================================
// 特化 QString
template <>
struct special_traits<QString> {
    static constexpr bool value = true;
    static QString jsonToObj(QJsonValue const &root) {
        if (root.isString()) {
            return root.toString();
        }
        qDebug()<<"转换QString失败:"<<root;
        return QString(); // 返回空字符串
    }

    static QJsonValue objToJson(QString const &object) {
        return object;
    }

};

// 特化 QJsonArray
template <>
struct special_traits<QJsonArray> {
    static constexpr bool value = true;
    static  QJsonArray jsonToObj(QJsonValue const &root) {
        if (root.isArray()) {
            qDebug()<<"特化QJsonArray:";
            return root.toArray();
        }
        qDebug()<<"转换QJsonArray失败:"<<root;
        return QJsonArray(); // 返回空字符串
    }


    static QJsonValue objToJson(QJsonArray const &object) {
        return object;
    }
};

// 特化 int
template <>
struct special_traits<int> {
    static constexpr bool value = true;
    static int jsonToObj(QJsonValue const &root) {
        if (root.isDouble()) {
            return root.toInt();
        }else if(root.isString()){
            return root.toString().toInt();
        }
        qDebug()<<"转换int失败:"<<root;
        return 0; // 返回零
    }


    static QJsonValue objToJson(int const &object) {
        return object;
    }
};
// 特化 long
template <>
struct special_traits<long> {
    static constexpr bool value = true;
    static long jsonToObj(QJsonValue const &root) {
        if (root.isDouble()) {
            return static_cast<long>(root.toInt());
        }
        else if(root.isString()){
            return root.toString().toLong();
        }
        qDebug()<<"转换long失败:"<<root;
        return 0; // 返回零
    }



    static QJsonValue objToJson(long const &object) {
        return QJsonValue(static_cast<double>(object));
    }
};

// 特化 unsigned short
template <>
struct special_traits<unsigned short> {
    static constexpr bool value = true;
    static unsigned short jsonToObj(QJsonValue const &root) {
        if (root.isDouble()) {
            return static_cast<int>(root.toInt());
        }else if(root.isString()){
            return root.toString().toUShort();
        }
        qDebug()<<"转换unsigned short失败:"<<root;
        return 0; // 返回零
    }



    static QJsonValue objToJson(unsigned short const &object) {
        return object;
    }
};

// 特化 short
template <>
struct special_traits<short> {
    static constexpr bool value = true;
    static short jsonToObj(QJsonValue const &root) {
        if (root.isDouble()) {
            return static_cast<int>(root.toInt());
        }else if(root.isString()){
            return root.toString().toShort();
        }
        qDebug()<<"转换short失败:"<<root;
        return 0; // 返回零
    }

    static QJsonValue objToJson( short const &object) {
        return object;
    }
};

// 特化 bool
template <>
struct special_traits  <bool>    {
    static constexpr bool value = true;
    static bool jsonToObj(QJsonValue const &root) {
        if (root.isBool()) {
            return root.toBool();
        }
        qDebug()<<"转换bool失败:"<<root;
        return false; // 返回 false
    }
    static QJsonValue objToJson(bool const &object) {
        return object;
    }
};



// 特化 double
template <>
struct special_traits    <double>    {
    static constexpr bool value = true;
    static double jsonToObj(QJsonValue const &root) {
        if (root.isDouble()) {
            return root.toDouble();
        }else if(root.isString()){
            return root.toString().toDouble();
        }
        qDebug()<<"转换double失败:"<<root;
        return 0.0; // 返回零
    }
    static QJsonValue objToJson(double const &object) {
        return object;
    }

};
// 特化 QJsonObject

template <>
struct special_traits     <QJsonObject>   {
    static constexpr bool value = true;
    static QJsonObject jsonToObj(QJsonValue const &root) {
        if (root.isObject()) {
            return root.toObject();
        }
        qDebug()<<"转换QJsonObject失败:"<<root;
        return QJsonObject(); // 返回空对象
    }
    static QJsonValue objToJson(QJsonObject const &object) {
        return object;
    }
};


template <class T, class Alloc>
struct special_traits<std::vector<T, Alloc>> {
    static constexpr bool value = true;

    static QJsonValue objToJson(std::vector<T, Alloc> const &object) {
        QJsonArray root;
        for (auto const &elem: object) {

           // qDebug()<< elem;
            //qDebug()<<  qtjson::objToJson(elem);
            root.append(qtjson::objToJson(elem));
        }

        //qDebug()<<root;
        return root;
    }

    static std::vector<T, Alloc> jsonToObj(QJsonValue const &root) {
        std::vector<T, Alloc> object;
        for (auto const &elem : root.toArray()) {
            object.push_back(qtjson::jsonToObj<T>(elem));
        }
        return object;
    }
};


template <class K, class V, class Alloc>
struct special_traits<std::map<K, V, Alloc>> {
    static constexpr bool value = true;

    static QJsonValue objToJson(std::map<K, V, Alloc> const &object) {
        QJsonObject root;
        for (auto const &elem: object) {
            root[elem.first] = qtjson::objToJson(elem.second);
        }
        return root;
    }

    static std::map<K, V, Alloc> jsonToObj(QJsonValue const &root) {

        std::map<K, V, Alloc> object;
        QJsonObject  jsonObject = root.toObject();

        for (auto it = jsonObject.begin(); it != jsonObject.end(); ++it) {
            const QString &key = it.key();
            const QJsonValue &value = it.value();

            object[key] = qtjson::jsonToObj<V>(value);


        }
        return object;
    }
};



template <class T>
QString serialize(T const &object) {
    return jsonToStr(objToJson(object));
}

template <class T>
T deserialize(QString const &json) {
    return jsonToObj<T>(strToJson(json));
}


}

#endif // QTJSON_H
