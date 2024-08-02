cmake_minimum_required(VERSION 3.14)

project(qt-websocket LANGUAGES CXX)

set(CMAKE_AUTOUIC ON)
set(CMAKE_AUTOMOC ON)
set(CMAKE_AUTORCC ON)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

find_package(QT NAMES Qt6 Qt5 REQUIRED COMPONENTS Core)
find_package(Qt${QT_VERSION_MAJOR} REQUIRED COMPONENTS Core)
find_package(Qt6 REQUIRED COMPONENTS Network)
find_package(Qt6 REQUIRED COMPONENTS WebSockets)


link_libraries("IPHLPAPI.lib")
add_executable(qt-websocket
  main.cpp
  info.h info.cpp
  devinfo.h devinfo.cpp
  qtjson.hpp reflect.hpp
  wininfo.cpp
)
target_link_libraries(qt-websocket Qt${QT_VERSION_MAJOR}::Core  Qt6::Network Qt6::WebSockets)

include(GNUInstallDirs)
install(TARGETS qt-websocket
    LIBRARY DESTINATION ${CMAKE_INSTALL_LIBDIR}
    RUNTIME DESTINATION ${CMAKE_INSTALL_BINDIR}
)
