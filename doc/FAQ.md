#### 为什么选择 Java 11 而不是 Java 8

- var可以减少非常多的代码，使代码更加简介
- Java 11的版本统一了api，Oracle JDK 和 Open JDK 可以无缝切换
- 11对容器支持友好，现在是容器的时代，反观 Java 8 的一些老版本都不支持容器
- Java 8过两年会停止维护，能早用就早用
- 主流的框架全部都已经支持Java 11
- 下个版本的spring boot直接最低版本要求Java 17

---

#### 为什么不选择基于interface（接口）的形式提供RPC，像dubbo那样的RPC

- 基于interface的不够灵活，不方便
- 然interface写同步rpc比较方便，不适合异步的写法。现在流行异步调用链的写法，基于interface有点过时了
- 并不是所有编程语言都有interface的，你用了interface的话其它编程语言就没法用了，就无法跨平台了
- 基于packet的容易扩展，可以轻易跨平台

---

#### 为什么选择mongodb

- mongodb可以单机部署也可以分布式部署，是万能的数据库，而且速度足够快
- mongodb，副本集部署（主从模式），可以数据同步，读写分离
- mongodb，分片部署，故障转移（容灾）

#### TODO：

- 参看一下akka的actor实现，进一步优化net的线程模型，统一异步回调的argument参数
- protocol需要支持更多的语言，python，go，C++
