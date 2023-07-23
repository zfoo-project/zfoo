# 一、MongoDB简介

## 1.MongoDB的特点

1. 数据文件为二进制Bson，一种Json的扩展
2. 模式自由，在生产环境可以轻松增加字段
3. 丰富的查询
4. 完整的索引支持
5. 复制和故障恢复
6. 自动分片
7. 客户端支持多种语言
8. 支持内存映射存储引擎

## 2.MongoDB的适用场景

- MongoDB 4.2 以后支持分布式事务，理论上适合所有场景

# 二、MongoDB安装

## Windows

### 1. MongoDB安装配置

1. 直接安装MongoDB，用custom自定义安装目录，一般用默认的安装目录即可，不要勾选mongoDB compass
2. 将C:\Program Files\MongoDB\Server\4.2\bin配置到环境变量中，便于全局使用
3. mongo -version # 查看安装的版本
4. MongoDB的工具

```
bsondump        # bson转换工具
mongodump       # 逻辑备份工具
mongorestore    # 逻辑恢复工具
mongoexport     # 数据导出工具
mongoimport     # 数据导入工具
mongofiles      # GriedFS文件工具
mongos          # 分片路由工具
mongostat       # 状态监控工具
mongotop        # 读写监控工具   
```

### 2. Navicat for MongoDB

- 客户端自行下载


## Linux

### 1.MongoDB安装配置

- 把MongoDB下载到/usr/local目录下，在/usr/local下新建文件夹MongoDB

```
tar -zxvf mongodb-linux-x86_64-ubuntu2204-6.0.6.tgz -C /usr/local
mv mongodb-linux-x86_64-ubuntu2204-6.0.6 mongodb
vim /usr/local/mongodb/mongodb.config # 创建自定义配置文件，解压的mongodb安装包没有默认的配置文件
```


- wiredTiger

```yaml
#security:
#   authorization: enabled
systemLog:
  destination: file
  path: "/data/mongodb/logs/mongodb.log"
  logAppend: true
storage:
  engine: wiredTiger
  dbPath: "/data/mongodb/db"
  directoryPerDB: true

# MongoDB默认端口是27017，为了安全，可以修改这个端口，避免恶意的连接尝试
# 生产环境需要设置ip：bind_ip=127.0.0.1,本机ip
net:
  bindIp: 0.0.0.0
  port: 27017
  maxIncomingConnections: 900
processManagement:
  fork: true
```

- inMemory

```yaml
#security:
#   authorization: enabled
systemLog:
  destination: file
  path: "/data/mongodb/logs/mongodb.log"
  logAppend: true
storage:
  engine: inMemory
  dbPath: "/data/mongodb/db"

# MongoDB默认端口是27017，为了安全，可以修改这个端口，避免恶意的连接尝试
# 生产环境需要设置ip：bind_ip=127.0.0.1,本机ip
net:
  bindIp: 0.0.0.0
  port: 22400
processManagement:
  fork: true
```

- mkdir -p /data/mongodb/db /data/mongodb/logs
- touch /data/mongodb/logs/mongodb.log

- vim /etc/profile # 添加命令运行环境变量，在文件最后面添加下面两

```
JAVA_HOME=/usr/local/java
JRE_HOME=$JAVA_HOME/lib

MONGODB_HOME=/usr/local/mongodb

PATH=$JAVA_HOME/bin:$MONGODB_HOME/bin:$PATH

export JAVA_HOME JRE_HOME MONGODB_HOME PATH
```

- source /etc/profile，加载环境变量

- mongod --config /usr/local/mongodb/mongodb.config # 启动MongoDB

```
mongod: error while loading shared libraries: libnetsnmpmibs.so.31: cannot open shared object file: No such file or directory
如果启动看到上面这个错误，则是缺少net-snmp安装包，yum install net-snmp
```

- mongosh -port 22400 # 使用mongo客户端链接MongoDB
```
dpkg -i mongodb-mongosh_1.10.1_amd64.deb
mongodb客户端命令行和服务器分开了，需要安装mongosh工具
```

- vim /etc/security/limits.conf 数据库需要大的文件句柄，保存并退出编辑器，重新启动机器使配置生效
```
* soft nofile 65536
* hard nofile 65536
root soft nofile 65536
root hard nofile 65536

# * 表示所有用户 
```

### 2.关闭MongoDB服务

- 方式一：进入mongo客户端关闭（推荐方式）

```
mongo -port 22400
use admin
db.shutdownServer()
```

- 方式二：kill -2 pid 或者 kill -15 pid

```
-2 和 -15 都会等MongoDB处理完，释放相应资源再停止。kill -9 会强制服务i停止，会导致数据的顺坏，不建议使用。
```

- 方式三：如果MongoDB注册为service，可以使用服务的命令关闭

```
systemctl stop mongodb
```

- 修复未正常关闭MongoDB，导致无法启动

```
进入/data/mongodb/db，将文件夹下的mongod.lock（也可以使用find命令查找）删除。
如果还有数据顺坏，需要使用mongod --repair命令修复一次，再正常启动。
修复数据库的实际过程很简单：将所有的文档导出后马上导入，忽略无效的文档，完成后会重建索引。
```

### 3.将MongoDB设置为开机自动启动

- vim /etc/systemd/system/mongodb.service，创建启动脚本，systemctl是最新的启动命令，避免用service
- chmod 754 /etc/systemd/system/mongodb.service，赋予启动脚本可执行的权限

```bash
[Unit]
#服务描述
Description=High-performance, schema-free document-oriented database
#指定了在systemd在执行完那些target之后再启动该服务
After=network.target

[Service]
#定义Service的运行类型，一般是forking(后台运行) 
Type=forking
LimitNOFILE=65536

ExecStart=/usr/local/mongodb/bin/mongod --config /usr/local/mongodb/mongodb.config
ExecReload=
ExecStop=/usr/bin/pkill mongod

[Install]
#多用户
WantedBy=multi-user.target
```

- systemctl daemon-reload，重新加载服务
- systemctl enable mongodb，会有一行反馈
- systemctl status mongodb，注意看 -> enabled; vendor preset: disabled)
- systemctl start mongodb

### 4.安全和访问控制

#### windows不用设置用户，为了方便便于开发

#### Linux用户设置

- 添加用户之前，要在数据库admin里添加一个拥有userAdminAnyDatabase角色的管理员账号，这个账号可以管理数据库admin和创建其它用户
- 创建管理员帐号，账号信息保存在admin数据库

```
use admin
db.createUser({ user: "root", pwd: "zfoo*5qtWy?cwajCGQl.Jh7o8D%X24A;", roles: [ 
    { role: "userAdminAnyDatabase", db: "admin" },
    { role: "clusterAdmin", db: "admin" },
    { role: "readWriteAnyDatabase", db: "admin" }
    ]})
```

- 创建普通帐号，账号信息保存在test数据库

```
use test
db.createUser({ user: "test", pwd: "123456", roles: [ { role: "readWrite", db: "test" }, { role: "read", db: "reporting" }]})
```

- 查看用户权限

```
use test
db.getUser("myTester")
db.getRole( "read", { showPrivileges:true } )  # 查看权限能执行哪些操作，例如要看test数据库中read权限能执行哪些操作
db.grantRolesToUser( "test", [{ role: "readWrite", db: "reporting" }])      # 授权
db.revokeRolesFromUser( "test", [{ role: "readWrite", db: "reporting" }])   # 取消权限
```

- 添加完成后，在配置文件中取消注释，#authorization
- 再重启mongod服务：systemctl restart mongodb

- 用户登录

```
# 启动mongo客户端时登录，--authenticationDatabase "admin"表示myUserAdmin用户在admin数据库下
# 远程用户登录注意打开防火墙的端口，或者直接关闭防火墙
mongo --port 22400 -u "root" -p "123456" --authenticationDatabase "admin"


# 进入mongo客户端后再登录:
mongo --port 22400
use admin
db.auth("root", "123456" )

# 修改密码，将root的密码修改为456789，需要admin管理员权限。
db.changeUserPassword("root", "456789")

# 删除用户，需要admin管理员权限
db.dropUser("root")
```
