cmake_minimum_required(VERSION 3.0)

project(qt-websocket LANGUAGES CXX)


set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
file(GLOB fileList ${CMAKE_CURRENT_SOURCE_DIR}/*.cpp)
file(GLOB jsonFileList ${CMAKE_CURRENT_SOURCE_DIR}/json/*.h)
file(GLOB hppFileList ${CMAKE_CURRENT_SOURCE_DIR}/*.hpp)
set(fileList ${fileList} ${jsonFileList} ${hppFileList})
file(COPY config.json DESTINATION ${CMAKE_BINARY_DIR} )
message("库文件有：${fileList}")
#add_executable(qt-websocket
#   main.cpp
#   info.h info.cpp
#   devinfo.h devinfo.cpp reflect.hpp
#   linuxinfo.cpp
#   reflect_json.hpp
#        jsoncpp.cpp
#        json/json.h
#        json/json-forwards.h
#
#)

add_executable(qt-websocket ${fileList})
target_link_libraries(qt-websocket ssl crypto curl pthread)
