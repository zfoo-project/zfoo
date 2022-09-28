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
- interface写同步rpc比较方便，不适合异步的写法。现在流行异步调用链的写法，基于interface有点过时了
- 并不是所有编程语言都有interface的，你用了interface的话其它编程语言就没法用了，就无法跨平台了
- 基于packet的容易扩展，可以轻易跨平台

---

#### zfoo的RPC实现原理

- zfoo的RPC和dubbo实现的原理一样，主要使用CompletableFuture，利用了唯一id

```
zfoo的RPC请求消息体会在IPacket的包后面附加上一个IAttachmet（SignalAttachment）作为同步和异步的信号。
SignalAttachment的signalId就是用于RPC的同步和异步的信号，通过这个唯一ID就可以确定哪个RPC请求对应了哪一个RPC回调。
```

---

#### protocol的实现原理

- 通过递归解析对象的结构，用字节码增强生成顺序的序列化和反序列化函数，可以自己线生成C#协议，就能看到Javassist生成的是什么样的结构

---

#### 为什么选择mongodb

- mongodb可以单机部署也可以分布式部署，是万能的数据库，而且速度足够快
- mongodb，副本集部署（主从模式），可以数据同步，读写分离
- mongodb，分片部署，故障转移（容灾）

---

#### 为什么部署的时候才用main，平时开发的时候从test启动

- 可以很好的隔离部署环境和开发测试环境
- 可以加上-Dspring.profiles.active=dev就可以从main启动
- 部署的时候从main启动（没有控制台日志），平时开发的时候从test启动（有控制台日志）
- 这样正式环境的配置放在main的resources里，测试环境的配置放在test的resources里，互不干扰
- 从test下启动的程序的配置文件会覆盖main中的配置文件

---

#### 为什么没有发布到maven仓库

- 项目非常轻量，容易修改，游戏场景很多都需要自己定制，打算java 21出来才会发布release版本
- zfoo安装很简单，maven install到本地就可以使用了，或者直接将zfoo源代码集成在在项目里当作一个module使用
- 没有用maven的<dependencyManagement>来管理这个root，是为了让下面的module不依赖于任何parent，复制出来就可以用
- 本地开发调试也可以直接用idea的add maven project当作一个第三方库使用

---

#### 前端h5的后台管理界面使用的技术栈

- 基础框架使用的，vue 2.6，https://cn.vuejs.org/
- UI框架使用的，vuetify 2.3，https://vuetifyjs.com
- 工具库使用的，lodash 4.17，https://www.lodashjs.com/
- icon使用的material font，mdi 5.3.45，https://pictogrammers.github.io/@mdi/font/5.3.45/

---

#### TODO：

1. 参看一下akka的actor实现，进一步优化net的线程模型，统一异步回调的argument参数

2. protocol需要支持更多的语言，python，go，C++

3. Router中的ThreadLocal主要就是让上层@PacketReceiver所表示的收包方法少传一个参数IAttachment，这块设计可以优化

```
提前把这个参数存起来，就不用传到上层去用了，便于优雅的调用，但是也带来了一些不必要的性能损耗
```
