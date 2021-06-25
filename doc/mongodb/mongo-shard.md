# 一、分片的搭建

## 1.MongoDB环境准备

- config服务器三台，虚拟机的静态ip分别设置为

```
192.168.1.40 mongodb-config0
192.168.1.41 mongodb-config1
192.168.1.42 mongodb-config2
```

- route服务器三台，虚拟机的静态ip分别设置为

```
192.168.1.45 mongodb-route0
192.168.1.46 mongodb-route1
192.168.1.47 mongodb-route2
```

- shard服务器三台，虚拟机的静态ip分别设置为

```
192.168.1.50 mongodb-shard0
192.168.1.51 mongodb-shard1
192.168.1.52 mongodb-shard2
```

- shard副本集服务器三台，虚拟机的静态ip分别设置为

```
192.168.1.55 mongodb-shard3-replica0
192.168.1.56 mongodb-shard3-replica1
192.168.1.57 mongodb-shard3-replica2
```

- 设置好ip后还需要把ip与主机名对应起来

```
hostname        # 查看主机名使用命令

vim /etc/sysconfig/network，修改主机名，在最后一行加上HOSTNAME=mongodb-config0，其它的服务器同理

vim /etc/hosts，修改为我们设置的主机名，不同的主机对应不同的mongodb
127.0.0.1   localhost mongodb-config0 localhost4 localhost4.localdomain4
::1         localhost mongodb-config0 localhost6 localhost6.localdomain6

192.168.1.30 mongodb0
192.168.1.31 mongodb1
192.168.1.32 mongodb2

192.168.1.40 mongodb-config0
192.168.1.41 mongodb-config1
192.168.1.42 mongodb-config2

192.168.1.45 mongodb-route0
192.168.1.46 mongodb-route1
192.168.1.47 mongodb-route2

192.168.1.50 mongodb-shard0
192.168.1.51 mongodb-shard1
192.168.1.52 mongodb-shard2
192.168.1.55 mongodb-shard3-replica0
192.168.1.56 mongodb-shard3-replica1
192.168.1.57 mongodb-shard3-replica2
```

- 重启服务器后生效，使用hostname命令验证主机名，使用ping命令验证三个虚拟机之间相互可以使用主机名ping通。
- 开启防火墙端口。

## 2.config服务器

- mongodb-config0，mongodb-config1，mongodb-config2使用命令

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

configsvr=true
replSet=mongodbConfigSet

#keyFile默认会开启auth=true
#auth=true
#keyFile=/data/mongodb/key/myKey.txt

#生产环境需要设置ip：bind_ip=127.0.0.1,本机ip
bind_ip=0.0.0.0
```

- 创建副本集

```
mongo --port 22400      # 进入mongodb0
config = {_id: "mongodbConfigSet", members: [
    {_id: 0, host:"mongodb-config0:22400"}, 
    {_id: 1, host:"mongodb-config1:22400"}, 
    {_id: 2, host:"mongodb-config2:22400"}
    ]}
rs.initiate(config)     # 初始化副本集
```

- 观察副本集

```
rs.conf()       # 副本集配置
rs.status()     # 副本集状态
```

## 3.route路由服务器

- mongodb-route0，mongodb-route1，mongodb-route2使用命令

```
mkdir -p /data/mongodb/db   # 存放数据的目录
mkdir -p /data/mongodb/logs # 存放日记的目录
mkdir -p /data/mongodb/key  # 存放密钥的目录
```

- vim /usr/local/mongodb/mongodb.config，修改配置文件如下

```
logpath=/data/mongodb/logs/mongodb.log
logappend=true

port=22400
fork=true

#必须是1个或则3
configdb=mongodbConfigSet/mongodb-config0:22400,mongodb-config1:22400,mongodb-config2:22400

#keyFile默认会开启auth=true
#keyFile=/data/mongodb/key/myKey.txt

#生产环境需要设置ip：bind_ip=127.0.0.1,本机ip
bind_ip=0.0.0.0
```

- vim /usr/lib/systemd/system/mongodb.service，修改启动脚本

```bash
[Unit]
#服务描述
Description=High-performance, schema-free document-oriented database
#指定了在systemd在执行完那些target之后再启动该服务
After=network.target

[Service]
#定义Service的运行类型，一般是forking(后台运行) 
Type=forking

ExecStart=/usr/local/mongodb/bin/mongos --config /usr/local/mongodb/mongodb.config
ExecReload=
ExecStop=/usr/bin/pkill mongos

[Install]
#多用户
WantedBy=multi-user.target
```

- systemctl daemon-reload，重新加载服务
- systemctl restart mongodb

## 4.shard分片服务器

- mongodb-shard0，mongodb-shard1，mongodb-shard2使用命令

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

shardsvr=true

#keyFile默认会开启auth=true
#auth=true
#keyFile=/data/mongodb/key/myKey.txt

#生产环境需要设置ip：bind_ip=127.0.0.1,本机ip
bind_ip=0.0.0.0
```

- systemctl restart mongodb

## 5.配置sharding

- mongo客户端进入mongos服务所在的端口，在其中一台机子中进入mongos

```
mongo --port 22400
use admin                           # 需要先切换到admin数据库，才能进行分片的添加
sh.addShard("mongodb-shard0:22400")
sh.addShard("mongodb-shard1:22400")
sh.addShard("mongodb-shard2:22400")

# 单节点分片的缺点是如果其中一个分片挂了，会导致整个分片集群不可用，所以建议每个分片都使用副本集
sh.addShard("mongodbShard3Set/mongodb-shard3-replica0:22400,mongodb-shard3-replica1:22400,mongodb-shard3-replica2:22400")
```

## 6.使用分片数据库和使用分片集合

- 在任节点中进入mongos

```
mongo --port 22400  # 切换到admin数据库
use admin
sh.enableSharding("test")

sh.shardCollection("test.student", { _id: "hashed" })   # 把test数据库student集合的_id字段设置为哈希片键
sh.shardCollection("test.student", { _id:1 })           # 把test数据库student集合的_id字段设置为区间片键
```

- 标签片键

```
标签片键比较特别，通过对分片节点打标签，再将片键按范围对应到这些标签上，对应片键范围的集合中的数据就会落在这个分片节点上。
首先需要对分片打标签，对分片打标签需要知道分片的_id，在mongos中使用命令查看：
sh.status()


这里分片_id分别为: shard0000, shard0001, shard0002,则对分片打标签命令如下:
sh.addShardTag("shard0000", "tag1")
sh.addShardTag("shard0001", "tag2")
sh.addShardTag("shard0002", "tag3")


然后student的学号字段code设置标签片键范围。
这样设置后学号code为1到600的会存储在分片shard0001或者shard0002中，学号为601到5000的会存储在分片shard0000中。
sh.addTagRange("test.student", { _id: 0 }, { _id: 1000 }, "tag1")
sh.addTagRange("test.student", { _id: 1001 }, { _id: 9000 }, "tag2")
sh.addTagRange("test.student", { _id: 9001 }, { _id: 10000 }, "tag3")

查看标签的分片，在mongos中使用命令
use config
db.shards.find()

查看标签的片键值范围，使用命令
use config
db.tags.find()


移除某个分片的标签使用命令
sh.removeShardTag("shard0000", "tag1")
sh.removeShardTag("shard0001", "tag2")
sh.removeShardTag("shard0002", "tag3")

```

## 7.分片数据测试

- 集合测试

```
mongo --port 22400
use test
for(var i=0; i<10000; i++) db.student.insert({ _id:i, name:"jaysunxiao", addr:"myAddress", country:"myCountry" })
执行可能需要一段会时间，我们直接在工具中查看config集合中的chunks元数据信息，就可以看到随着数据的写入分了多少片，以及片键的最小到最大的范围。
    
3个shard服务器里可以看到都有了mytest数据库以及student集合。
```

## 8.分片的管理

- 移除Shard Server,回收数据，分片被删除之后，数据被移到其他分片中，不会丢失

```
use admin
# 如果是区间片键，已经指定了区间了，数据迁移会失败，会导致分片一直draining，因为找不到区间分片的节点
db.runCommand( { removeShard: "shard0002" } )
# 检查迁移的状态,再次在 admin 数据库运行 removeShard 命令，因为数据多的话要一处的节点需要很长时间迁移数据
db.runCommand( { removeShard: "shard0002" } )

db.runCommand({"moveprimary": "test", "to": "shard0000"})   # 要是原来分片是一个主分片，则需要执行movePrimary
```

## 9.均衡器

- 均衡器：均衡器负责数据迁移，周期性的检查分片是否存在不均衡。 config.locks集合里的state表示均衡器是否找正在运行，0表示非活动状态，2表示正在均衡。
  均衡迁移数据的过程会增加系统的负载：目标分片必须查询源分片的所有文档，将文档插入目标分片中，再清除源分片的数据。 可以关闭均衡器（不建议）：关闭会导致各分片数据分布不均衡，磁盘空间得不到有效的利用。

```
sh.setBalancerState(false)  #关闭自动均衡器，手动均衡
sh.setBalancerState(true)

use config
db.settings.find()          #查看均衡器状态

# 移动块的时候必须关闭均衡器，否则可能导致死循环
use admin
sh.moveChunk("db.collection",{块地址},"新片名称")  #手动均衡
sh.moveChunk("test.student",{"_id": "test.student-_id_6148914691236517204"},"shard0000")
use config
db.chunks.find().pretty()
```

## 10.认证分配

- 首先创建账号(Root角色)，其中route(mongos)不需要创建账号,需要两种权限

```
userAdminAnyDatabase    # 管理员权限，用来管理其它的账号
clusterAdmin            # 分片管理员权限
```

- 再生成一个KeyFile文件
- config，shard节点修改配置，vim /usr/local/mongodb/mongodb.config

```
#keyFile默认会开启auth=true
auth=true
keyFile=/data/mongodb/key/myKey.txt
```

- route节点修改配置，vim /usr/local/mongodb/mongodb.config

```
#因为mongos本来就是从config里加载数据的，所以只需要添加keyfile文件即可
keyFile=/data/mongodb/key/myKey.txt
```

- 重新启动各个节点

```
systemctl restart mongodb
```

## 11.分片备份、还原

- 先停止平衡器的工作，并检查没有chunk move动作，保证dump的时候没有进行数据迁移。

```
sh.stopBalancer()
```

- 锁定数据库，保证数据没有写入：在各个分片上和配置服务器上执行。

```
db.fsyncLock()
```

- 执行备份操作mongodump，备份各个分片服务器和配置服务器。

- 解锁数据库，备份完成之后在分片和配置服务器上解锁数据库，允许修改。

```
db.fsyncUnlock()
```

- 备份还原mongorestore各个分片和配置服务器
- 重启各个实例

# 二、原理解析

## 1.分片实现了数据的分布式存储，负载均衡，这些都是海量数据的云存储平台不可或缺的功能

- 配置服务器

```
是一个独立的mongod进程，保存集群和分片的元数据，即各分片包含了哪些数据的信息。
最先开始建立，启用日志功能。像启动普通的mongod一样启动配置服务器，指定configsvr选项。
不需要太多的空间和资源，配置服务器的1KB空间相当于真实数据的200MB。保存的只是数据的分布表。
当服务不可用，则变成只读，无法分块、迁移数据。
```

- 路由服务器

```
即mongos，起到一个路由的功能，供程序连接。
本身不保存数据，在启动时从配置服务器加载集群信息，开启mongos进程需要知道配置服务器的地址，指定configdb选项。
```

- 分片服务器

```
是一个独立普通的mongod进程，保存数据信息。可以是一个副本集也可以是单独的一台服务器。
```

## 2.mongodb什么时候开始分片？

MongoDB的分片是基于范围的，也就是说任何一个文档一定位于指定片键的某个范围。一旦片键选择好后，chunks就会按照片键来将一部分documents从逻辑上组合在一起。  
由于chunks的大小默认的阈值是64 MB或100000个文档，一旦达到这个阈值就会根据片键分片。  
片键一般是是索引，如果没有指定索引，则用集合默认的_id索引作为片键。

## 3.分片适用的场景

1，机器的磁盘不够用了。使用分片解决磁盘空间的问题。 2，单个mongod已经不能满足写数据的性能要求。通过分片让写压力分散到各个分片上面，使用分片服务器自身的资源。
3，想把大量数据放到内存里提高性能。和上面一样，通过分片使用分片服务器自身的资源。

## 4.集群dump备份恢复策略，对集群的备份就可以转化为对各复制集的备份：

1. 禁用平衡器，命令：sh.stopBalancer()；因为分片集群上会有一个balancer进程在后台维护各个片上数据块数量的均衡，如果不禁用平衡器可能会导致备份数据的重复或缺失
2. 停止毎个片（副本集）上的某个secondary节点，利用此节点进行备份；停止其中某个配置服务器（所有配置服务器的数据一样)，保证备份时配务器上元数据不会改变，备份可以当作一单节点的实例
3. 重启所有停掉的复制集成员，它们会自动从primary节点上的oplog同步数据，最终数据会达到一致性
4. 重启分片集群的平衡器。通过mongo连接到mongos上，执行命令如下。use config；sh.startBalancer()

## 5.集群恢复流程

1. 停止集群上的所有mongod实例和mongos实例。
2. 利用上面备份的dump文件，依次恢复片中的每个副本集
3. 恢复配置服务器。
4. 重启所有mongod实例与mongos实例。
5. 通过mongo连接上mongos，执行以下命令确保集群是可操作的：db.printShardingStatus() 
