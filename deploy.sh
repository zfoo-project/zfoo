#!/bin/bash

# 服务器部署脚本
# doc:
# 1.在Idea中下载安装阿里云Alibaba Cloud插件
# 2.使用Ecs的AccessKeyId和AccessSecurity登录，服务器信息里有具体密码
# 3.在upload选项中选择需要上传的jar包，location默认为根路径/，
# command中输入脚本运行命令：sh /deploy.sh stopUpdateStart /usr/local/zapp/user/zapp-user-1.0.jar /usr/local/zapp/user notPrint
# 注：
# /deploy.sh脚本包括了，启动服务器，优雅停止java服务器，更新文件，重新启动服务器的脚本，对应四个命令：start|stop|update|stopUpdateStart。
# 其中stopUpdateStart会按照顺序执行，stop，update，start命令。

# usage:
# sh deploy.sh start /usr/local/zapp/gateway/zapp-gateway-1.0.jar /usr/local/zapp/gateway
# sh deploy.sh stop gateway
# sh deploy.sh update /usr/local/zapp/gateway/zapp-gateway-1.0.jar
# sh deploy.sh stopUpdateStart /usr/local/zapp/gateway/zapp-gateway-1.0.jar /usr/local/zapp/gateway
# start启动服务器，/usr/local/zapp/gateway/zapp-gateway-1.0.jar是jar包的绝对路径，/usr/local/zapp/gateway是日志的输出的绝对路径
# stop关闭服务器，会使用jps | grep gateway，抓取需要关闭的java进程
# update更新jar包，默认会使用根路径/zapp-gateway-1.0.jar下的jar包去更新/usr/local/zapp/gateway/zapp-gateway-1.0.jar路径的jar包
# stopUpdateStart，按顺序执行命令stop，update，start

# 相关参数命令
# java -XX:+PrintFlagsInitial，查看jvm全部参数的默认值
#
# @author jaysunxiao
# @version 1.0


if [ $# -lt 1 ]; then
    echo "deploy.sh脚本使用错误，命令参数不合法"
    echo "usage: sh deploy.sh start|stop|update|stopUpdateStart"
    exit 1
fi

command=${1}

# 为了避免一些部署的异常，每次执行脚本都更新一下环境变量
source /etc/profile

function waitAllProcessesExit() {
    while true; do
        local runningProcesses
        runningProcesses=$(jps -lvm | grep ${1})

        if [ -n "${runningProcesses}" ]; then
            echo "正在关闭以下Java进程${1}："
            echo "${runningProcesses}"
            sleep 3
        else
            echo "已正常关闭所有${1}进程"
            return
        fi
    done
}

function waitInfoLog() {
    local infoLog
    while true; do
        infoLog=$(ls ./log | grep info)
        if [ -z "${infoLog}" ]; then
            echo "正在等待info.log创建"
            sleep 1
        else
            return
        fi
    done
}

# 停止所有进程,不包括login
function stop() {
    echo "########################## Ⅰ stop ##########################"
    local pids
    pids=$(jps | grep ${1} | awk '{print $1}' | paste -d " " -s)

    if [ -z "${pids}" ]; then
        echo "没有找到任何包含${1}关键字的Java进程"
        return
    fi

    echo "**************************  cpu  **************************"
    uptime
    echo "注："
    echo "uptime查看cpu的负载情况，load avarage分别是1分钟，5分钟，15分钟内系统的load值"
    echo "一般load值不大于3，我们就认为它的负载是正常的，大于了3就要想办法降低系统的负载"

    echo "**************************  内存  **************************"
    free -m
    echo "注："
    echo "free查看内存的使用情况，重点关注swap内存使用，swap大表示物理内存不够用，这时候容易导致OOM异常"

    echo "**************************  磁盘  **************************"
    df -h
    echo "注："
    echo "df查看磁盘的使用情况，应该特别关注日志和数据库的挂载路径的使用情况"

    echo "**************************  网络  **************************"
    sar -n DEV 1 2
    echo "注："
    echo "sar查看网络的使用情况，可以通过网络设备的吞吐量，判断网络设备是否已经饱和。"

    echo "**************************系统日志**************************"
    dmesg | tail -n 20
    echo "注："
    echo "df查看系统日志的最后20行，主要看有没有严重的系统问题"

    echo "**************************jmap实例**************************"
    for pid in ${pids}; do
        echo "${pid}->进程的class实例数量前20信息"
        jmap -histo ${pid} | head -n 20
        echo -e "\n"
    done

    echo "********************开始执行终止Java进程命令********************"
    echo "kill -15 $pids"
    kill -15 ${pids}

    waitAllProcessesExit ${1}
}

# 更新jar包
function update() {
    echo "######################### Ⅱ  update ########################"
    local jarPath=${1}

    if [ -z "${jarPath}" ]; then
        echo "需要被更新的jar路径不能为空"
        echo "usage: sh deploy.sh update jarPath"
        exit 1
    fi

    local jarFilePath=${jarPath%/*}
    local jarName=${jarPath##*/}

    # echo ${jarFilePath}
    # echo ${jarName}

    echo "更新之前源文件和目标文件信息:"
    local fileInfo=$(ls -lh "/${jarName}")
    echo "${fileInfo}"

    if [ -f "${jarPath}" ]; then
        fileInfo=$(ls -lh ${jarPath})
        echo "${fileInfo}"
    fi

    echo -e "\n\n"

    rm -rf ${jarPath}
    mkdir -p ${jarFilePath}
    cp "/${jarName}" ${jarFilePath}

    echo "更新之后源文件和目标文件信息:"
    fileInfo=$(ls -lh "/${jarName}")
    echo "${fileInfo}"
    fileInfo=$(ls -lh ${jarPath})
    echo "${fileInfo}"
}

# 启动一个java进程，必须指定jar路径和jar名称
# -n为notEmpty，-z为empty
function start() {
    echo "######################### Ⅲ  start #########################"
    local jarPath=${1}
    local logPath=${2}

    if [ -z "${jarPath}" ]; then
        echo "参数错误：启动的jar包名称不能为空"
        echo "usage: sh deploy.sh start jarPath logPath"
        exit 1
    fi

    if [ -z "${logPath}" ]; then
        echo "参数错误：启动的日志路径不能为空"
        echo "usage: sh deploy.sh start jarPath logPath"
        exit 1
    fi

    mkdir -p "${logPath}/log"
    cd ${logPath}

    local currentPath
    currentPath=$(pwd)
    echo "pwd当前的运行路径：${currentPath}"

    if [ ! -f "${jarPath}" ]; then
        echo "文件不存在：启动的jar包不存在，请检查jar的路径格式是否是绝对路径"
        echo "usage: sh deploy.sh start jarAbsPath logPath"
        exit 1
    fi

    # -XX:+AlwaysPreTouch，并置零内存页面，可能令得启动时慢上一点，但后面访问时会更流畅，比如页面会连续分配
    # 输出到文件  >> output.log 2>&1 &
    nohup java -XX:+UseG1GC -XX:InitialHeapSize=4g -XX:MaxHeapSize=4g -XX:MaxMetaspaceSize=256m -XX:AutoBoxCacheMax=20000 -XX:+UseStringDeduplication -XX:+HeapDumpOnOutOfMemoryError -Djdk.attach.allowAttachSelf=true -Dspring.profiles.active=pro -Dfile.encoding=UTF-8 -jar ${jarPath} >/dev/null 2>&1 &

    # 如果没有info的log，则一直等待
    waitInfoLog

    if [ -z "${3}" ]; then
        tailf log/info.log
    else
        sleep 4
        local jarName=${jarPath##*/}
        local runningProcesses
        runningProcesses=$(jps -lvm | grep ${jarName})
        echo "**************************jps信息**************************"
        echo "${runningProcesses}"

        sleep 4
        echo "************************jstat编译统计信息************************"
        local pids
        pids=$(jps | grep ${jarName} | awk '{print $1}' | paste -d " " -s)
        for pid in ${pids}; do
            echo "${pid}->进程的编译统计信息"
            jstat -compiler ${pid}
            echo -e "\n"
        done

        sleep 6
        echo "**************************启动日志**************************"
        tail -n 40 log/info.log

        exit 0
    fi
}


function stopUpdateStart() {
    local jarPath=${1}
    local logPath=${2}

    if [ -z "${jarPath}" ]; then
        echo "启动的路径不能为空"
        echo "usage: sh deploy.sh start jarPath logPath"
        exit 1
    fi

    if [ -z "${logPath}" ]; then
        echo "启动的jar包名称不能为空"
        echo "usage: sh deploy.sh start jarPath logPath"
        exit 1
    fi

    echo "开始按顺序执行任务： stop -> update -> start"
    echo -e "\n\n"
    local jarFilePath=${jarPath%/*}
    local jarName=${jarPath##*/}

    # 先停止
    stop ${jarName}
    echo
    echo
    echo
    # 再更新
    update ${jarPath}
    echo
    echo
    echo
    # 最后启动
    start ${jarPath} ${logPath} ${3}
}

# Linux性能调优
function optimizeLinux() {
    echo "--------------------------------------------------------------------------------------------------------------------------------------------------------------------------"
    echo "--------------------------------------------------------------------------------------------------------------------------------------------------------------------------"
    echo "--------------------------------------------------------------------------------------------------------------------------------------------------------------------------"
    echo "--------------------------------------------------------------------------------------------------------------------------------------------------------------------------"
    echo "--------------------------------------------------------------------------------------------------------------------------------------------------------------------------"
    echo "--------------------------------------------------------------------------------------------------------------------------------------------------------------------------"
    echo "--------------------------------------------------------------------------------------------------------------------------------------------------------------------------"
    echo "--------------------------------------------------------------------------------------------------------------------------------------------------------------------------"
    echo "--------------------------------------------------------------------------------------------------------------------------------------------------------------------------"
    echo "--------------------------------------------------------------------------------------------------------------------------------------------------------------------------"
    echo "--------------------------------------------------------------------------------------------------------------------------------------------------------------------------"
    echo "--------------------------------------------------------------------------------------------------------------------------------------------------------------------------"
    echo "--------------------------------------------------------------------------------------------------------------------------------------------------------------------------"
    echo "--------------------------------------------------------------------------------------------------------------------------------------------------------------------------"
    echo "--------------------------------------------------------------------------------------------------------------------------------------------------------------------------"
    echo "--------------------------------------------------------------------------------------------------------------------------------------------------------------------------"
    echo "--------------------------------------------------------------------------------------------------------------------------------------------------------------------------"
    echo "--------------------------------------------------------------------------------------------------------------------------------------------------------------------------"
    echo "--------------------------------------------------------------------------------------------------------------------------------------------------------------------------"
    echo "--------------------------------------------------------------------------------------------------------------------------------------------------------------------------"
    echo "性能调优预留方法"
}
# 立即执行调优
optimizeLinux

case ${command} in
"start")
    start ${2} ${3}
    ;;
"stop")
    stop ${2}
    ;;
"update")
    update ${2}
    ;;
"stopUpdateStart")
    stopUpdateStart ${2} ${3} ${4}
    ;;
*)
    echo "命令无法识别: ${1}"
    echo "usage: sh deploy.sh start|stop|udpate|stopUpdateStart"
    ;;
esac

exit 0
