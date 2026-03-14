#!/bin/sh

# To run shell scripts on Ubuntu, you need to disable dash，sudo dpkg-reconfigure dash

# doc:
# Server deployment script, this script encapsulates common deployment operations, not necessarily used

# The deploy.sh script includes, start the server, gracefully stop the server, update the program file, restart the server, corresponding to four commands

##############################################################################################################################################################################################################################################################################
# usage 1:
# sh deploy.sh start /usr/local/tank/single-boot/single-boot-1.0.jar

# start starts the server, the absolute path is very important,[/usr/local/tank/single-boot/single-boot-1.0.jar] is the absolute path of the jar package，

####################################################################################################################################################################################
# usage 2:
# sh deploy.sh stop single

# stop closes the server, use command jps | grep single，grab the java process that needs to be closed, so you don't need to enter the full name of single-boot
# If you have two launchers named [single-boot1.jar], [single-boot2], both will be turned off

####################################################################################################################################################################################
# usage 3:
# sh deploy.sh update /usr/local/tank/single-boot/single-boot-1.0.jar

# update updates the jar package. By default, the jar package under the root path [single-boot-1.0.jar] will be
# used to update the jar package of [/usr/local/tank/single-boot/single-boot-1.0.jar]
# In this way, the jar must be placed in the root directory (the topmost directory, not the root directory, don't make a mistake),
# it is very important, because the jar of the root path is used by default to update the jar at the location you specify

####################################################################################################################################################################################
# usage 4:
# sh deploy.sh stopUpdateStart /usr/local/tank/single-boot/single-boot-1.0.jar

# stop Update Start, execute the commands stop, update, start, three-in-one commands in order,
# because there is an update, so you must put your jar in the root directory

####################################################################################################################################################################################

## java config
JAVA_HOME="/usr/local/java"
JAVA_JVM_OPTIONS="-Dspring.profiles.active=pro -XX:InitialHeapSize=1g -XX:MaxHeapSize=1g -XX:+AlwaysPreTouch -XX:AutoBoxCacheMax=10000 -XX:+UseStringDeduplication -XX:+HeapDumpOnOutOfMemoryError -Djdk.attach.allowAttachSelf=true -Duser.timezone=Asia/Shanghai -Dfile.encoding=UTF-8"
## log config
WAIT_LOG=true
LOG_FILE="spring.log"


if [ $# -lt 1 ]; then
    echo "deploy.sh script use error, command parameter is illegal"
    echo "usage: sh deploy.sh start|stop|update|stopUpdateStart"
    exit 1
fi

command=${1}

. /etc/profile

waitAllProcessesExit() {
    while true; do
        local runningProcesses
        runningProcesses=$(${JAVA_HOME}/bin/jps -lvm | grep ${1})

        if [ -n "${runningProcesses}" ]; then
            echo "The following Java processes are being shut down ${1}："
            echo "${runningProcesses}"
            sleep 3
        else
            echo "Gracefully shut down ${1} processes"
            break
        fi
    done
}

waitLogFile() {
    local currentPath=$(pwd)
    while true; do
        local logfile=$(ls ./ | grep ${LOG_FILE})
        if [ -z "${logfile}" ]; then
            echo "Waiting for ${currentPath}/${LOG_FILE} to be created..."
            sleep 1
        else
            break
        fi
    done

    tail -f ${LOG_FILE}
}

# Stop all processes
stop() {
    echo "######################################################################################################################### Ⅰ stop #########################################################################################################################"
    local pids
    pids=$(${JAVA_HOME}/bin/jps | grep ${1} | awk '{print $1}' | paste -d " " -s)

    if [ -z "${pids}" ]; then
        echo "Can not find any Java process containing the [${1}] keyword"
        return
    fi

    echo "*************************************  cpu  *************************************"
    uptime
    echo "uptime -> cpu, load avarage represents the system load values within 1 minute, 5 minutes, and 15 minutes."

    echo "*************************************  memory  *************************************"
    free -h
    echo "free -> memory"

    echo "*************************************  disk  *************************************"
    df -h
    echo "df -> disk"

    echo "*************************************  network  *************************************"
    sar -n DEV 1 2
    echo "sar -> net io"

    echo "************************************* system log *************************************"
    dmesg | tail -n 8
    echo "dmesg -> system log"

    echo "*************************************  jmap  *************************************"
    for pid in ${pids}; do
        echo "${pid}->Information about the top 20 class instances of the process"
        ${JAVA_HOME}/bin/jmap -histo ${pid} | head -n 5
        echo -e "\n"
    done

    echo "************************************* stop process *************************************"
    echo "kill -15 $pids"
    kill -15 ${pids}

    waitAllProcessesExit ${1}
}

# 更新jar包
update() {
    echo "######################################################################################################################### Ⅱ  update #########################################################################################################################"
    local jarPath=${1}

    if [ -z "${jarPath}" ]; then
        echo "The jar path to be updated cannot be empty"
        echo "usage: sh deploy.sh update jarPath"
        exit 1
    fi

    local jarFilePath=${jarPath%/*}
    local jarName=${jarPath##*/}

    # echo ${jarFilePath}
    # echo ${jarName}

    echo "Before source|target:"
    local fileInfo=$(ls -lh "/${jarName}")
    echo "${fileInfo}"

    if [ -f "${jarPath}" ]; then
        fileInfo=$(ls -lh ${jarPath})
        echo "${fileInfo}"
    fi

    rm -rf ${jarPath}
    mkdir -p ${jarFilePath}
    cp "/${jarName}" ${jarFilePath}

    echo "After target ------>"
    fileInfo=$(ls -lh ${jarPath})
    echo "${fileInfo}"
}

# To start a Java process, you must specify the path to the JAR file and the JAR file name.
# -n is notEmpty, -z is empty
start() {
    echo "######################################################################################################################### Ⅲ  start #########################################################################################################################"
    local jarPath=${1}

    if [ -z "${jarPath}" ]; then
        echo "Parameter error: The name of the jar package to start cannot be empty"
        echo "usage: sh deploy.sh start jarPath"
        exit 1
    fi

    local jarFilePath=${jarPath%/*}
    cd ${jarFilePath}

    local currentPath=$(pwd)
    echo "pwd current path : ${currentPath}"

    if [ ! -f "${jarPath}" ]; then
        echo "usage: sh deploy.sh start jarAbsPath"
        exit 1
    fi

    echo "${JAVA_HOME}/bin/java ${JAVA_JVM_OPTIONS} -jar ${jarPath} &> /dev/null &"
    nohup ${JAVA_HOME}/bin/java ${JAVA_JVM_OPTIONS} -jar ${jarPath} &> /dev/null &

    # If there is no info log, keep waiting
    if $WAIT_LOG; then
        waitLogFile
    fi
}


stopUpdateStart() {
    local jarPath=${1}

    if [ -z "${jarPath}" ]; then
        echo "The startup path cannot be empty"
        echo "usage: sh deploy.sh start jarPath"
        exit 1
    fi

    echo
    echo "Start executing tasks： stop -> update -> start"
    echo
    local jarFilePath=${jarPath%/*}
    local jarName=${jarPath##*/}

    # stop first
    stop ${jarName}

    # update second
    update ${jarPath}

    # last start
    start ${jarPath} ${3}
}

# Linux性能调优
optimizeLinux() {
    echo "--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------"
    echo "--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------"
    echo "--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------"
}

# run optimizeLinux method immediately
optimizeLinux

case ${command} in
"stop")
    stop ${2}
    ;;
"update")
    update ${2}
    ;;
"start")
    start ${2}
    ;;
"stopUpdateStart")
    stopUpdateStart ${2}
    ;;
*)
    echo "command not recognized: ${1}"
    echo "usage: sh deploy.sh start|stop|udpate|stopUpdateStart"
    ;;
esac

exit 0
