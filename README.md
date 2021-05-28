### Ⅰ. zfoo简介

- **性能炸裂，天生异步，Actor设计思想，无锁化设计，基于Spring的MVC式用法的万能RPC框架**
- **极致序列化**，原生集成的目前二进制序列化和反序列化速度最快的 [zfoo protocol](protocol/README.md) 作为网络通讯协议
- **高可拓展性**，单台服务器部署，集群部署，注册中心加集群部署，网关加集群部署，随意搭配

完善的工作开发流程，完整的线上解决方案

- **普通java项目，spring项目，spring boot项目，一行代码无差别热更新**  [hotswap](hotswap/src/test/java/com/zfoo/hotswap/ApplicationTest.java)
- **Excel配置自动映射和Excel热更新方案** [storage](storage/src/test/java/com/zfoo/storage/ApplicationTest.java)
- **轻量级cpu，内存，硬盘，网络监控，** 拒绝复杂的监控部署 [monitor](monitor/src/test/java/com/zfoo/monitor/ApplicationTest.java)
- mongodb的自动映射框架 [orm](orm/README.md)
- 事件总线 [event](event/src/test/java/com/zfoo/event/ApplicationTest.java)
- 时间任务调度 [scheduler](scheduler/README.md)

### Ⅱ. 背景和适用项目

- 性能需求极高的项目，如游戏服务器，单服滚服，全球服
- 节省研发成本的项目，如想节省，开发，部署，运维成本
- 喜欢 [KISS法则](https://baike.baidu.com/item/KISS原则/3242383) 的项目 ，简单的配置，优雅的代码。

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

### Ⅲ. 问题

- 每个工程目录的test文件夹下都有标准的demo展示和注释说明，可以直接运行
- 问题讨论群QQ: 876280300

### Ⅳ. 安装和使用

#### 1. 环境要求

**JDK 11+**，可以在 **OpenJDK** 和 **Oracle JDK** 无缝切换

```
如果你的机器没有安装JDK 11+，最快速的安装方法是在Idea中的Project Structure，Platform Settings，SDKs中直接下载
```

#### 2. [protocol](protocol/README.md) 目前性能最好的Java序列化和反序列化库

```
// zfoo协议注册，只能初始化一次
ProtocolManager.initProtocol(Set.of(ComplexObject.class, ObjectA.class, ObjectB.class));

// 序列化
ProtocolManager.write(byteBuf, complexObject);

// 反序列化
var packet = ProtocolManager.read(byteBuf);
```

#### 3. [net](net/README.md) 目前速度最快的RPC框架

```
// 服务提供者，只需要在方法上加个注解，则自动注册接口
@PacketReceiver
public void atUserInfoAsk(Session session, UserInfoAsk ask) {
}

// 消费者，同步请求远程用户信息，会阻塞当前的线程，慎重考虑使用同步请求
var userInfoAsk = UserInfoAsk.valueOf(userId);
var answer = NetContext.getCosumer().syncAsk(userInfoAsk, UserInfoAnswer.class, userId).packet();

// 消费者，异步请求远程用户信息，不会柱塞当前的线程，异步请求成功过后依然会在userId指定的线程执行逻辑
NetContext.getCosumer()
                    .asyncAsk(userInfoAsk, UserInfoAnswer.class, userId)
                    .whenComplete(sm -> {
                        // do something
                    );
```

#### 4. [hotswap](hotswap/src/test/java/com/zfoo/hotswap/ApplicationTest.java) 热更新代码，不需要停止服务器，不需要额外的任何配置，一行代码开启热更新

```
// 传入需要更新的class文件
HotSwapUtils.hotswapClass(bytes);
```

#### 5. [orm](orm/README.md) 基于mongodb的自动映射框架

```
// 无需自己写sql和任何配置，直接通过注解定义在数据库中定义一张表
@EntityCache(cacheStrategy = "tenThousand", persister = @Persister("time30s"))
public class UserEntity implements IEntity<Long> {
    @Id
    private long id;
    private String name;
}

// 更新数据库的数据
entityCaches.update(userEntity);
```

#### 6. [event](event/src/test/java/com/zfoo/event/ApplicationTest.java) 事件总线解耦不同模块，提高代码的质量

```
// 接收一个事件，只需要在需要接收事件的方法上加一个注解就会自动监听这个事件
@EventReceiver
public void onNoticeEvent(NoticeEvent event) {
    // do something
}

// 抛出一个事件
EventBus.syncSubmit(MyNoticeEvent.valueOf("同步事件"));
EventBus.asyncSubmit(MyNoticeEvent.valueOf("异步事件"));
```

#### 7. [scheduler](scheduler/README.md) 基于cron表达式的定时任务调度框架

````
@Scheduler(cron = "0/1 * * * * ?")
public void cronSchedulerPerSecond() {
    // do something
}
````

#### 8. [storage](storage/src/test/java/com/zfoo/storage/ApplicationTest.java) excel和Java类自动映射框架，无需代码和任何工具只需要定义一个和excel对应的类，直接解析excel

```
@Resource
public class StudentResource {
    @Id
    private int id;
    @Index
    private String name;
    private int age;
}
```

### Ⅴ. 为什么快

- 使用目前性能最好的 [zfoo protocol](protocol/README.md) 作为网关和RPC消息的序列化和反序列化协议
- 无锁化设计和优雅的线程池设计，用户的请求通过网关总能保证请求在同一台服务器的同一条线程去执行，所以就不需要用锁保证并发
- rpc调用天生异步支持，并且保证rpc异步调用结束过后在同一条线程去执行，类似于actor的设计思想，特别适合对性能有极高需求的场景，如直播聊天，游戏等等。
- 数据库使用高性能分布式数据库 [mongodb](https://github.com/mongodb/mongo) ，在其之上使用 [caffeine](https://github.com/ben-manes/caffeine)
  设计了 [zfoo orm](protocol/README.md) 二级缓存，充分释放数据库压力
- 服务器热更新和监控无需代码和额外工具，直接内置在程序里，解放运维生产力
- 使用MVC设计模式，规范开发，保证代码质量，高效执行

### Ⅵ. 完整的工程案例

- 图片分享聊天网站 [zapp](https://github.com/zfoo-project/zapp)

### Ⅶ. 提交规范

- Java项目格式化代码的方式采用IntelliJ Idea默认的格式化
- 所有的接口的参数和方法返回的参数都默认是非空的，如果参数可空，需要加上org.springframework.lang.Nullable注解
- 代码提交的时候需要固定格式，如给net增加了一个功能，feat[net]: 功能描述，下面给出了一些常用的格式

```
feat[module]: 新增某一项功能
perf[module]: 优化了模块代码或者优化了什么功能
fix[module]: 修改了什么bug
test[module]: 测试了什么东西
doc[module]: 增加了什么文档
del[module]: 删除了某些功能或者无用代码
```

### Ⅷ. License

zfoo使用 [Apache License Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)

```
Copyright (C) 2020 The zfoo Authors

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.
```