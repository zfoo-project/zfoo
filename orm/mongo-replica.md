# 一、副本集的搭建

## 1.MongoDB环境准备

- 三台虚拟机的静态ip分别设置为

```
192.168.1.30 mongodb0
192.168.1.31 mongodb1
192.168.1.32 mongodb2
```

- 设置好ip后还需要把ip与主机名对应起来

```
hostname        # 查看主机名使用命令


vim /etc/sysconfig/network，修改主机名，在最后一行加上HOSTNAME=mongodb0，不同主机的对于关系如下
192.168.1.30 -> HOSTNAME=mongodb0
192.168.1.31 -> HOSTNAME=mongodb1
192.168.1.32 -> HOSTNAME=mongodb2

vim /etc/hosts，修改为我们设置的主机名，不同的主机对应不同的mongodb
127.0.0.1   localhost mongodb0 localhost4 localhost4.localdomain4
::1         localhost mongodb0 localhost6 localhost6.localdomain6
192.168.1.30 mongodb0
192.168.1.31 mongodb1
192.168.1.32 mongodb2

```

- 重启服务器后生效，使用hostname命令验证主机名，使用ping命令验证三个虚拟机之间相互可以使用主机名ping通。
- 开启防火墙端口。

## 2.创建目录和修改配置文件

- mongodb0，mongodb1，mongodb2使用命令

```
mkdir -p /data/mongodb/db   # 存放数据的目录
mkdir -p /data/mongodb/logs # 存放日记的目录
mkdir -p /data/mongodb/key  # 存放密钥的目录
```

- vim /usr/local/mongodb/mongodb.config，修改配置文件如下

```
dbpath=/data/mongodb/db

logpath=/data/mongodb/logs/mongodb.log
logappend=true

port=22400
fork=true

#keyFile默认会开启auth=true
#auth=true
#keyFile=/data/mongodb/key/myKey.txt
replSet=myReplicaSet

#生产环境需要设置ip：bind_ip=127.0.0.1,本机ip
bind_ip=0.0.0.0
```

## 3.初始化副本集

- 清空一下目录的历史数据，因为可能以前是用的单机节点，集群部署会有不可预知问题，所以先清空数据

```
/data/mongodb/db
/data/mongodb/logs
```

- 分别启动mongodb0，mongodb1，mongodb2

```
systemctl restart mongodb
```

- 创建副本集

```
mongo --port 22400      # 进入mongodb0
config = {_id: "myReplicaSet", members: [{_id: 0, host:"mongodb0:22400"}, {_id: 1, host:"mongodb1:22400"}, {_id: 2, host:"mongodb2:22400", arbiterOnly : true}]}
rs.initiate(config)     # 初始化副本集
```

- 观察副本集

```
rs.conf()       # 副本集配置
rs.status()     # 副本集状态
```

## 4.副本集初始化后，需要给整个副本集创建帐户、密码

- 创建Key，mongodb0，mongodb1，mongodb2使用命令

```
echo "replicaSet key" > /data/mongodb/key/myKey.txt
chmod 600 /data/mongodb/key/myKey.txt
```

- 在主节点上，用客户端连接，创建用户权限（主节点，可以用 rs.status() 查看），创建用户请看mongodb-setup.md

- 关闭副本集,分别关闭mongodb0，mongodb1，mongodb2

```
systemctl stop mongodb
```

- vim /usr/local/mongodb/mongodb.config，修改配置文件如下

```
dbpath=/data/mongodb/db

logpath=/data/mongodb/logs/mongodb.log
logappend=true

port=22400
fork=true

#keyFile默认会开启auth=true
auth=true
keyFile=/data/mongodb/key/myKey.txt
replSet=myReplicaSet

#生产环境需要设置ip：bind_ip=127.0.0.1,本机ip
bind_ip=0.0.0.0
```

- 重新启动副本集，分别启动mongodb0，mongodb1，mongodb2

```
systemctl restart mongodb
```

## 5.数据同步测试

- 首先向PRIMARY(主节点)写入一条数据

```
use test
db.say.insert({"text":"Hello World"})
db.say.find()
```

- 进入SECONDARY(副节点)查看数据是否同步

```
默认情况下SECONDARY节点不能读写，要设定slaveOk为true才可以从SECONDARY节点读取数据。
replSet里只能有一个Primary节点，只能在Primary写数据，不能在SECONDARY写数据。
首先需要在SECONDARY节点设置slaveOk为true

db.getMongo().setSlaveOk();
use test;
db.say.find();
```

## 6.故障切换测试

- 把主节点关点，看看副集点是否能接替主节点进行工作

# 二、原理解析

## 1.副本集实现了数据库的数据同步，故障转移，读写分离

副本集最少应该包含三个节点，其中有一个必须是主节点。

数据同步：从节点会从主节点同步oplog到自己的节点，oplog有自己的大小，可以通过--oplogSize来设置。主从之间的读写对程序员是透明的。
故障转移（容灾）：主从节点两秒钟就会有一个心跳，如果发现主节点挂了就从新选取主节点，选取规则是从节点中数据最新的，权重最高的节点。 如果从节点挂了就直接选取从节点，再将主节点的数据同步给从节点。
读写分离：可以设置是优先从主节点读数据，还是优先从从节点读数据。 注：在单个节点中，数据先被写到内存，操作被保存在内存的视图中，内存视图没100ms写到硬盘的journal日志中，mongodb每60s将内存中的数据同步到硬盘。

# 三、副本集的管理

## 1.诊断

- 副本集中的机子，查看主数据库的复制的状态

```
db.printReplicationInfo()

configured oplog size:   990MB
log length start to end: 174676secs (48.52hrs)
oplog first event time:  Wed May 01 2019 13:29:38 GMT+0800
oplog last event time:   Fri May 03 2019 14:00:54 GMT+0800
now:                     Fri May 03 2019 14:00:56 GMT+0800

输出信息oplog大小990MB
log length start to end = oplog last event time - oplog first event time
因为oplog是固定大小，有一个循环，所以必须等oplog循环几圈，oplog才和实际相符合。

如果oplog太小，写入的数据太快，oplog中的日志可能会被顶掉，从节点想要同步时候发现找不到切入的oplog，已经跟不上主节点了，就会停止同步。
如果log length start to end和下面的从节点behind the primary非常的接近，说明oplog可能太小了 
```

- 查看从数据库的复制的状态

```
db.printSlaveReplicationInfo()

source: mongodb-config1:22400
	syncedTo: Fri May 03 2019 13:56:31 GMT+0800
	0 secs (0 hrs) behind the primary 
source: mongodb-config2:22400
	syncedTo: Fri May 03 2019 13:56:31 GMT+0800
	0 secs (0 hrs) behind the primary 

```

## 2.副本集回滚丢失的数据

```
主节点故障后重新加入副本集时会回滚恢复写操作。
只有在主节点接受了写请求并且从节点没有复制前主节点就故障的时候才需要回滚。
当主节点作为从节点重新加入副本集，他恢复或回滚他的写操作来与其他成员保持数据库一致性。

当回滚确实发生了，MongoDB把回滚数据写入数据库的dbPath的rollback文件夹下的BSON文件中，可以用bsondump读取回滚文件的内容。
回滚文件名如下形式：
<database>.<collection>.<timestamp>.bson
```
