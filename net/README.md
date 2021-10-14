### Ⅰ. 简介

1. 优雅的同步和异步请求，速度更快，支持 tcp udp websocket
2. 服务注册和发现，配置中心使用的是zookeeper，可扩展成其它注册中心
3. 自带高性能网关，自定义转发策略
4. 服务可伸缩，负载均衡，集群监控，应有尽有。
4. 基于Java11，所有的依赖包都是最新的jar包

### Ⅱ. 相关模块

- net提供了两层api接口
  - IRouter为底层网络接口，直接通过Session连接发送消息
  - IConsumer为高层网络接口，是对IRouter的封装，消费者向服务提供者发送RPC消息

```
极致的高性能，高可用性，高伸缩性（一般指增加机器），高扩展性（一般指代码层面的开闭原则）

config，本地配置，zookeeper的注册发现，请求的负载均衡，都放在这个包下
core，核心包，服务器，客户端的统一封装
router，消息的路由
handler，netty的handler，定义了客户端，服务器的一些通用handler
protocol，消息类的注册，消息的编解码，字节码增强等
schema，spring的自定义标签的解析
session，对netty的channel的封装
task，任务线程池，任务的分发
```

#### 诞生背景

```
用Java做网络游戏涉及到比较复杂的交互场景的时候，远程请求非常多，这时候就需要一个高性能的rpc框架。
Java生态的rpc框架非常多，但是很少有rpc框架能够契合到游戏项目的rpc框架，下面列了一些：
    dubbo，代码重量级，异步调用繁琐，没有网关，无法直接和客户端交互；
    grpc，代码重量级，使用复杂，部署复杂，没有网关，很难自定义；
    akka，用Scala语言，学习成本极高，直接放弃；
所以就写了一个万能的rpc框架，代码轻量级，可以方便的自定义，自带网关的，天生异步，工具链完整。
经过后续的不断优化，还做出了一个目前Java速度最快的序列化框架，然后速度就直接起飞了。
后来发现其实不仅仅能用来做游戏，只要有对性能有极致需求的rpc场景都能使用的上这个框架。
```

### Ⅱ. 服务器架构图

<img src="./../doc/image/general-game-architect.jpg" width="70%" height="70%" alt="服务器架构图"/><br/>

### Ⅲ. 为什么快
----------

- 使用目前性能最好的 [zfoo protocol](protocol/README.md) 作为网关和RPC消息的序列化和反序列化协议
- 无锁化设计和优雅的线程池设计，用户的请求通过网关总能保证请求在同一台服务器的同一条线程去执行，所以就不需要用锁保证并发
- rpc调用天生异步支持，并且保证rpc异步调用结束过后在同一条线程去执行，类似于actor的设计思想，特别适合对性能有极高需求的场景
- 使用MVC设计模式，规范开发，保证代码质量，高效执行

### Ⅳ. 网络通信规范

- 客户端对服务器的请求以Request结尾，返回以Response结尾
- 服务器内部之间的调用以Ask结尾，返回以Answer结尾。

### Ⅴ. 教程

- [单机服务器教程](src/test/java/com/zfoo/net/core/tcp/server/TcpServerTest.java)
- [RPC教程](src/test/java/com/zfoo/net/core/provider/ProviderTest.java)
- [网关教程](src/test/java/com/zfoo/net/core/gateway/GatewayTest.java)

### Ⅵ. 依赖的中间件zookeeper教程

- [zk基础的增删改查](src/test/java/com/zfoo/net/zookeeper/base)
- [curator的增删改查](src/test/java/com/zfoo/net/zookeeper/curator)
- [zookeeper实现的分布式自增ID](src/test/java/com/zfoo/net/zookeeper/recipes/atomicint)
- [分布式Barrier](src/test/java/com/zfoo/net/zookeeper/recipes/distributedbarrier)
- [分布式锁](src/test/java/com/zfoo/net/zookeeper/recipes/distributedbarrier)
- [数据发布和订阅](src/test/java/com/zfoo/net/zookeeper/recipes/nodecache)
- [zookeeper的分布式选举算法](src/test/java/com/zfoo/net/zookeeper/recipes/mastersel)

