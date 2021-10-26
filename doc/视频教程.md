### 软件下载地址

- 百度网盘

```
链接：https://pan.baidu.com/s/1TMLtNiNz0aLpgnZkkMNB-g 
提取码：zfoo
```

### [15分钟学会zfoo](https://www.bilibili.com/video/BV1TL4y1z7oy?spm_id_from=333)

- 主要就是快速学习zfoo的每一个模块，想深入学习的话，还是得看下面的详细教程

### [第一讲](https://www.bilibili.com/video/BV1Mf4y1b7xA) ，[第二讲](https://www.bilibili.com/video/BV1J64y1b7Px)

- 主要就是环境搭建，软件安装，代理设置，git使用
- 知识点

```
oracle和openjdk的区别
jdk11的优势
```

### [第三讲](https://www.bilibili.com/video/BV1LU4y1V7QD)

- event原理讲解
- 知识点

```
maven本地仓库，maven远程仓库，idea的工程管理
spring自定义标签
spring生命周期，对应的事件
spring自带事件的优先级通过Ordered接口指定
javassist字节码增强，asm，cglib，ByteBuddy，了解
```

### [第四讲](https://www.bilibili.com/video/BV1Aw411Z7ic)

- storage原理讲解
- 知识点

```
通过spring的PathMatchingResourcePatternResolver扫描excel文件位置，有方法可以直接获取Resource
spring的Resource抽象，spring对文件的抽象
apache的poi，poi可以解析excel
通过spring的ConversionService去解析excel单元格的内容，通过ConditionalGenericConverter，Converter接口自定义解析规则
```

### [第五讲](https://www.bilibili.com/video/BV1Xv411H7Qo)

- Tank项目的运行

### [第六讲](https://www.bilibili.com/video/BV1to4y1C77Y)

- scheduler原理讲解
- 知识点

```
cron表达式的用法
spring自带的Scheduled和zfoo的Scheduler
java的线程池，SingleThreadScheduledExecutor，SingleThreadExecutor
通过spring自带的CronExpression计算下一次cron表达式要执行的时间
```

### [第七讲](https://www.bilibili.com/video/BV1rL411W7bo)

- orm原理讲解
- 学习MongoDB优先看官方的文档，https://docs.mongodb.com/manual/
- B站有个稍微权威一点的MongoDB视频教程，https://www.bilibili.com/video/BV13a4y1J7L6
- 知识点

```
Mysql中的数据库，表，一条数据，在Mongodb分别叫做，数据库，集合，文档
Elastic Search的全文搜索引擎
Java的缓存框架guava cache，还有它的升级版本caffeine
mongodb的map reduce操作
分布式唯一Id，Java自带的UUID，雪花算法（SnowFlake），Redis分布式唯一Id实现，Mongodb分布式唯一Id实现，Zookeeper分布式唯一Id实现
```

### [第八讲](https://www.bilibili.com/video/BV1JL411W7Eo)

- protocol原理讲解
- 知识点

```
netty的ByteBuf
java的序列化和反序列化
kryo的使用，注意不是线程安全
protobuf的使用
juc的CountDownLatch
Zigzag算法压缩，varint数据压缩算法，优先kryo的实现
Javassist字节码增强序列化函数比反射快非常多
```

### [第九讲](https://www.bilibili.com/video/BV1ch411a7GY)

- net原理讲解
- 知识点

```
netty的nio,epoll
同步和异步通过CompletableFuture去实现的
一致性hash算法的负载均衡
zookeeper的用法
```

### [第十讲](https://www.bilibili.com/video/BV13U4y137G7)

- 综合实战

### [第十一讲](https://www.bilibili.com/video/BV1i44y1177r)

- 云服务器部署

- [tank-game-server](https://github.com/zfoo-project/tank-game-server) 网络游戏《进击的坦克（The Fight of Tanks）》
  - 快速体验，tank游戏入口 [tank.zfoo.net](http://tank.zfoo.net)
  - 快速体验，tank后台入口 [tank-admin.zfoo.net](http://tank-admin.zfoo.net)，账号：a，密码：a

```
云服务器的安全组策略
java的安装
zookeeper的安装
mongodb的安装
用maven打包项目的流程

nohup java -XX:InitialHeapSize=300m -XX:MaxHeapSize=300m -XX:+HeapDumpOnOutOfMemoryError -Djdk.attach.allowAttachSelf=true -Dspring.profiles.active=pro -Dfile.encoding=UTF-8 -jar home-1.0.jar >/dev/null 2>&1 &

sh /deploy.sh stopUpdateStart /usr/local/tank/admin/admin-1.0.jar /usr/local/tank/admin
sh /deploy.sh stopUpdateStart /usr/local/tank/single/single-1.0.jar /usr/local/tank/single


sh /deploy.sh stopUpdateStart /usr/local/tank/admin/admin-1.0.jar /usr/local/tank/admin
sh /deploy.sh stopUpdateStart /usr/local/tank/cache/cache-1.0.jar /usr/local/tank/cache
sh /deploy.sh stopUpdateStart /usr/local/tank/gateway/gateway-1.0.jar /usr/local/tank/gateway
sh /deploy.sh stopUpdateStart /usr/local/tank/home/home-1.0.jar /usr/local/tank/home
```
