简体中文 | [English](./README.md)
<a href="https://github.com/zfoo-project/zfoo"><img src="/doc/image/logo.jpg" width="30%"></a>

-----------
注意
-----------

- 主干main是当前是面向java 21和GraalVM的开发版本，稳定版本请使用 [zfoo-java-11-17](https://github.com/zfoo-project/zfoo/tree/zfoo-java-11-17)
- 升级流程
  - 重新导入找不到的类
  - 不需要再继承IPacket
  - SafeRunnable删除了，请使用ThreadUtils.safeRunnable()
  - 重命名接口和注解，Storage -> IStorage，@Resource -> @Storage，@ResInjection -> @StorageAutowired
  - 重命名接口和注解，@EntityCachesInjection -> @EntityCacheAutowired，@EntityCaches -> @EntityCache

-----------
Why is zfoo protocol ?
-----------

- 协议原生支持 C++ C# Java Go Javascript TypeScript Lua GDScript Python
- 通用的分布式服务器协议实现，有其它语言的实现，可以轻易实现其它平台的的RPC，微服务
- 二进制序列化速度最快的自定义格式框架，并且为序列化字节最少的框架，更加安全，可前后兼容
- 单线程的Benchmark测试中，序列化和反序列化速度比Protobuf快50%，比Kryo快100%

Ⅰ. zfoo简介🚩
-----------

- **极致性能，天生异步，Actor设计思想，无锁化设计，MVC式用法的万能RPC框架**
- **极致序列化**，原生集成目前二进制序列化和反序列化速度最快的 [zfoo protocol](protocol/README.md) 作为网络通讯协议
- **高可拓展性**，单台服务器部署，微服务部署，注册中心加集群部署，网关加集群部署，随意搭配
- **上能做游戏服务器框架，下能做应用网站服务器框架**

完善的工作开发流程，完整的线上解决方案

- 普通java项目，spring项目，分布式项目，容器项目， **不停机完美热更新代码** [hotswap](hotswap/src/test/java/com/zfoo/hotswap/ApplicationTest.java)
- Excel json csv配置自动映射和解析，**在线不停机热更新Excel配置** [storage](storage/src/test/java/com/zfoo/storage/ApplicationTest.java)
- MongoDB的自动映射框架 [orm](orm/README.md)
- 事件总线 [event](event/src/test/java/com/zfoo/event/ApplicationTest.java)
- 时间任务调度 [scheduler](scheduler/README.md)
- 内置在程序里的**轻量级cpu，内存，硬盘，网络监控，** 无需代码和额外工具，解放运维生产力 [monitor](monitor/src/test/java/com/zfoo/monitor/ApplicationTest.java)

Ⅱ. 背景和适用项目
---------------

- 性能需求极高的项目，如网站和游戏服务器框架，单服滚服，全球服，直播聊天，IM系统，实时推送
- 节省研发成本的项目，如想节省，开发，部署，运维成本
- 适合作为 **Godot，Unity，Cocos，Webgl，H5** 的后端基础框架，网络通信协议支持 tcp udp websocket http
- 喜欢 [KISS法则](https://baike.baidu.com/item/KISS原则/3242383) 的项目 ，简单的配置，优雅的代码

Ⅲ. 详细的教程和完整的工程案例
--------------------

- 有问题，先看 [FAQ](./doc/FAQ.md) 能否解决你的问题
- 依然有问题，参考每个工程目录的test文件夹下的标准demo展示和注释说明，可以直接运行
    - [问题讨论群QQ: 876280300](https://qm.qq.com/cgi-bin/qm/qr?k=uSo2FnXz50i5UZ1LYZS1sPsVWW9A34v-&jump_from=webapi) <a target="_blank" href="https://qm.qq.com/cgi-bin/qm/qr?k=uSo2FnXz50i5UZ1LYZS1sPsVWW9A34v-&jump_from=webapi"><img src="https://img.shields.io/badge/qq%E8%AE%A8%E8%AE%BA%E7%BE%A4-876280300-green"/></a>

- [tank-game-server](https://github.com/zfoo-project/tank-game-server) 网络游戏《进击的坦克（The Fight of Tanks）》，新手友好，难度2星

- [godot-bird](https://github.com/zfoo-project/godot-bird) 鸟了个鸟，客户端使用godot开发，服务器依然使用的tank-game-server

- [cocos-fivechess](https://github.com/zfoo-project/fivechess) 五子棋，客户端使用cocos开发

- [zapp](https://github.com/zfoo-project/zapp) 图片分享聊天网站，标准的https企业级网站，难度5星

Ⅳ. 安装和使用⭐
------------

#### 1. 环境要求和安装

**JDK 11 或者 JDK 17**，可以在 **OpenJDK** 和 **Oracle JDK** 无缝切换，下载完本项目，maven install到本地仓库即可使用

```
如果你没有安装JDK，快速的安装方法是在Idea的右上角Project Structure，Platform Settings，SDKs中直接下载
```

#### 2. [protocol](protocol/README.md) 目前性能最好的Java序列化和反序列化库

```
// zfoo协议注册，只能初始化一次
// 注意：实际项目中是通过读取protocol.xml文件自动完成协议注册的，无需调用此方法手动注册
ProtocolManager.initProtocol(Set.of(ComplexObject.class, ObjectA.class, ObjectB.class));

// 序列化
ProtocolManager.write(byteBuf, complexObject);

// 反序列化
var packet = ProtocolManager.read(byteBuf);
```

#### 3. [net](net/README.md) 目前速度最快的RPC框架，支持 tcp udp websocket http

```
// 服务提供者，只需要在方法上加个注解，则自动注册接口
@PacketReceiver
public void atUserInfoAsk(Session session, UserInfoAsk ask) {
}

// 消费者，同步请求远程用户信息，会阻塞当前的线程，慎重考虑使用同步请求
var userInfoAsk = UserInfoAsk.valueOf(userId);
var answer = NetContext.getCosumer().syncAsk(userInfoAsk, UserInfoAnswer.class, userId).packet();

// 消费者，异步请求远程用户信息，不会柱塞当前的线程，异步请求成功过后依然会在当前线程执行逻辑
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

#### 5. [orm](orm/README.md) 基于mongodb的自动映射框架，使用 [caffeine](https://github.com/ben-manes/caffeine) 设计了二级缓存，充分释放数据库压力

```
// 无需自己写sql和任何配置，直接通过注解定义在数据库中定义一张表
@EntityCache
public class UserEntity implements IEntity<Long> {
    @Id
    private long id;
    private String name;
}

// 更新数据库的数据
entityCaches.update(userEntity);
```

#### 6. [event](event/src/test/java/com/zfoo/event/ApplicationTest.java) 事件总线解耦不同模块，提高代码的质量，核心思想是观察者设计模式

```
// 接收一个事件，只需要在需要接收事件的方法上加一个注解就会自动监听这个事件
@EventReceiver
public void onMyNoticeEvent(MyNoticeEvent event) {
    // do something
}

// 抛出一个事件
EventBus.post(MyNoticeEvent.valueOf("我的事件"));
```

#### 7. [scheduler](scheduler/README.md) 基于cron表达式的定时任务调度框架

````
@Scheduler(cron = "0/1 * * * * ?")
public void cronSchedulerPerSecond() {
    // do something
}
````

#### 8. [storage](storage/src/test/java/com/zfoo/storage/ApplicationTest.java) Excel类自动映射框架，只需要定义一个和Excel对应的类，直接解析Excel

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

Ⅴ. 提交规范👏
----------

- 欢迎喜欢这个项目的人来一起维护这个项目，提交代码的时候注意下面规范
- 项目格式化代码的方式采用IntelliJ Idea默认的格式化
- 优先使用英文提交注释和文档，拒绝内卷从走出去开始，在idea中安装Translation插件轻松翻译
- [代码提交的说明(commit message)](https://github.com/pvdlg/conventional-changelog-metahub#commit-types) 按照下面给出的些常用格式

```
feat[module]: 新增某一项功能
perf[module]: 优化了模块代码或者优化了什么功能
fix[module]: 修改了什么bug
test[module]: 测试了什么东西
del[module]: 删除了某些功能或者无用代码
ref[module]: 重命名或者重构了模块
doc[module]: 增加了什么文档
```

Ⅵ. License
-----------

zfoo使用 [Apache License Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)

![JetBrains Logo (Main) logo](https://resources.jetbrains.com/storage/products/company/brand/logos/jb_beam.svg)
