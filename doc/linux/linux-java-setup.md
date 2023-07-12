# 一、卸载java

## 1. 卸载JDK安装路径

- java -version
- javac

```
判断jdk是否安装(显示版本号等信息，说明已经安装)
```

- which java（查看JDK的安装路径）

```
rm -rf JDK的安装路径
```

## 2. 删除环境变量

- java -version
- javac

```
判断jdk是否安装(显示版本号等信息，说明已经安装)
```

- vim /etc/profile，删除配置中的环境变量
- ls /etc，看看有没有别的java配置文件，如果有就全部删除

# 二、安装JDK

```
把JDK下载到/usr/local目录下，在/usr/local下新建文件夹java
```

- tar -zxvf openjdk-17.0.2_linux-x64_bin.tar.gz -C /usr/local
- mv jdk-17.0.2 java

- vim /etc/profile

```
JAVA_HOME=/usr/local/java
JRE_HOME=$JAVA_HOME/lib
PATH=$JAVA_HOME/bin:$PATH
export JAVA_HOME JRE_HOME PATH
```

- source /etc/profile，加载环境变量

```
- java -version
- javac
- jps
判断jdk是否安装(显示版本号等信息，说明已经安装)
```

# 三、Linux内核参数设置

- vim /etc/sysctl.conf，然后执行sysctl -p使参数生效，永久生效
```

# ******************* SYN状态的内核参数调优 *******************
# 新建连接如果无响应，内核要发送多少次SYN连接才放弃，默认值为5
net.ipv4.tcp_syn_retries = 2
# 控制回应SYN失败的重试次数，默认值也是5
net.ipv4.tcp_synack_retries = 2
# 当出现SYN等待队列溢出时，启用cookies来处理，可防范少量SYN攻击，默认为0，表示关闭
net.ipv4.tcp_syncookies = 1
# 指定所能接受SYN同步包的最大客户端数量，即半连接上限，默认值为128，对于web服务，频繁大量的SYN同步包，应该放大这个值
net.ipv4.tcp_max_syn_backlog = 65535

# ******************* FIN_WAIT_2状态的内核参数调优 *******************
# 如果被动关闭不发送FIN关闭连接，那么这个状态就会一直存在，当然Linux有针对该状态的超时时间，默认为60秒
net.ipv4.tcp_fin_timeout = 8

# ******************* TIME_WAIT状态的内核参数调优 *******************
# 对于web服务器，由于我们需要经常去连接mysql、redis或者一些RPC调用等，会有大量的主动关闭状态(TIME_WAIT)，因此可以修改内核参数限制TIME_WAIT的数量
net.ipv4.tcp_max_tw_buckets = 36000
# 限制timewait 的数量，防止大量timewait导致系统负载升高，一旦达到限定值，则强制清理TIME_WAIT状态的连接并在打印系统日志(time wait bucket table overflow)，该参数官方文档说明主要用来对抗DDos攻击
net.ipv4.tcp_tw_recycle = 1
# 时间戳，0关闭，1开启。不能和net.ipv4.tcp_tw_recycle参数同时开启，因为一旦开启net.ipv4.tcp_tw_recycle，服务器就会检查包的时间戳，如果对方发来的包的时间戳是乱跳或者说时间戳是滞后的，这样服务器就不会回复，服务器会把带了"倒退"的时间戳包当作是"recycle"的tw连接的重传数据，不是新的请求，于是丢掉不回包，就容易出现syn不响应
net.ipv4.tcp_timestamps = 0
# 开启重用，允许将TIME-WAIT sockets 重新用于新的TCP 连接
net.ipv4.tcp_tw_reuse = 1

# ******************* 长连接(keepalive)的内核参数调整 *******************
# 表示TCP连接在多少秒没有数据报文传输时启动探测报文，探测连接是否正常
net.ipv4.tcp_keepalive_time = 120
# 探测次数，超过设置后丢弃
net.ipv4.tcp_keepalive_probes = 3
# 前后探测报文之间的时间间隔
net.ipv4.tcp_keepalive_intvl = 15


# ******************* TCP/UDP内存参数调整 *******************
net.ipv4.tcp_mem = 786432 1048576 1572864
net.ipv4.tcp_wmem = 8192 131072 16777216
net.ipv4.tcp_rmem = 32768 131072 16777216

# 系统所能处理不属于任何进程的TCP sockets最大数量，默认值8192
net.ipv4.tcp_max_orphans = 32768

# ******************* 其他内核参数 *******************
# 表示用于向外连接的临时端口范围。缺省情况下很小：32768到61000
net.ipv4.ip_local_port_range = 1024 65000
# 表示socket监听(listen)的backlog上限，backlog是socket的监听队列，也就是服务端所能accept(socket编程中accpet()函数为建立TCP连接接受连接状态)即处理数据的最大客户端数量队列，默认值为128，如果队列满了的时候新来一条建立连接，该连接会被拒绝
net.core.somaxconn = 16384
# 设置系统所有进程一共可以打开多少个文件句柄，这是一个系统级的设置，管控的是所有进程总共可以同时打开多少文件句柄
fs.file-max = 6553600
# 每个网络接口接收数据包的速率比内核处理这些包的速率快时，允许送到队列的数据包的最大数目
net.core.netdev_max_backlog = 16384
```
