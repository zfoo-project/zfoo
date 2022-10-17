<p align="center">
	<a href="https://github.com/zfoo-project/zfoo"><img src="/doc/image/logo.jpg"></a>
</p>
<p align="center">
	👉 <strong>Java server framework for extremely high performance</strong>👈
</p>

<p align="center">
	<a target="_blank" href="https://spring.io">
		<img src="https://img.shields.io/badge/spring-5.3.4-yellowgreen" />
	</a>
    <a target="_blank" href="https://openjdk.java.net/projects/jdk/">
		<img src="https://img.shields.io/badge/JDK-11+-green.svg" />
	</a>
	<a target="_blank" href="http://www.apache.org/licenses/LICENSE-2.0">
		<img src="https://img.shields.io/badge/license-Apache--2.0-blue" />
	</a>
</p>

<br/>

English | [简体中文](./README.md)
-----------
Why is zfoo protocol ?
-----------

- The protocol currently supports C++ C# Go Java Javascript TypeScript GDScript Lua
- Universal distributed server protocol implementation, there are implementations in other languages, which can easily
  implement RPC, servers, and microservices on other platforms
- The fastest custom format framework for Java binary serialization, with the fewest serialized bytes, more secure and
  compatible
- In the single-threaded benchmark test, serialization and deserialization are 50% faster than Protobuf and 100% faster
  than Kryo

Ⅰ. Introduction of zfoo🚩
-----------

- **Extremely fast, asynchronous, Actor design pattern, lock-free design, RPC framework based on Spring MVC**
- **Fastest serialization and deserialization**，use [zfoo protocol](protocol/README.md) as a network protocol
- **High scalability**，Single server deployment, microservice deployment, cluster deployment, gateway deployment
- **Can be used as a game server framework or website server framework.**

Perfect work development process, complete online solution

- Java projects, spring projects, distributed projects, container projects， **hot update code without
  downtime** [hotswap](hotswap/src/test/java/com/zfoo/hotswap/ApplicationTest.java)
- Excel json csv configuration is automatically mapped and parsed，**Online hot update of Excel
  configuration** [storage](storage/src/test/java/com/zfoo/storage/ApplicationTest.java)
- Automapping framework for MongoDB [orm](orm/README.md)
- Event bus [event](event/src/test/java/com/zfoo/event/ApplicationTest.java)
- Time task scheduling [scheduler](scheduler/README.md)
- **cpu, memory, hard disk, network monitoring built into the program** no code and extra tools
  required [monitor](monitor/src/test/java/com/zfoo/monitor/ApplicationTest.java)

Ⅱ. Who use this project
---------------

- Projects with extremely high performance requirements, such as website and game server frameworks, single server,
  global server, live chat, IM system, real-time push
- Projects such as saving, development, deployment, operation and maintenance costs
- As a backend infrastructure for **Godot，Unity，Cocos，Webgl，H5**，Network protocol supports tcp udp websocket http
- [Keep it Simple and Stupid](https://baike.baidu.com/item/KISS原则/3242383) ，simple configuration, lightweight code

Ⅲ. Tutorials
--------------------

- [FAQ](./doc/FAQ.md)，There are standard demo display and instructions in the test folder of each project directory,
  which can be run directly

- [zfoo video tutorials](./doc/video-tutorial.md)，In the continuous update, it is recommended to watch the hands-on
  instructional video first.

- [tank-game-server](https://github.com/zfoo-project/tank-game-server) Online game《The Fight of Tanks》，novice friendly,
  difficulty 2 stars
    - Quick star, tank game entry [tank.zfoo.net](http://tank.zfoo.net)
    - Quick star, tank background management [tank-admin.zfoo.net](http://tank-admin.zfoo.net)

- [zapp](https://github.com/zfoo-project/zapp) Image sharing chat website, standard https enterprise-level website,
  difficulty 5 stars

Ⅳ. Install and use⭐
------------

#### 1. Environmental Requirements and Installation

**JDK 11+**，**OpenJDK** or **Oracle JDK** ，maven install to local repository

```
If you do not have JDK 11+ installed, the quick installation method is to download directly 
from Project Structure, Platform Settings, SDKs in the upper right corner of Idea
```

#### 2. [protocol](protocol/README.md) fastest serialization and deserialization

```
// zfoo protocol registration, can only be initialized once
ProtocolManager.initProtocol(Set.of(ComplexObject.class, ObjectA.class, ObjectB.class));

// serialization
ProtocolManager.write(byteBuf, complexObject);

// deserialization
var packet = ProtocolManager.read(byteBuf);
```

#### 3. [net](net/README.md) fastest RPC framework, supports tcp udp websocket http

```
// Service provider, only need to add an annotation to the method, the interface will be automatically registered
@PacketReceiver
public void atUserInfoAsk(Session session, UserInfoAsk ask) {
}

// Consumers, synchronously requesting remote service, will block the current thread
var userInfoAsk = UserInfoAsk.valueOf(userId);
var answer = NetContext.getCosumer().syncAsk(userInfoAsk, UserInfoAnswer.class, userId).packet();

// Consumers, asynchronously requesting remote service, and will still execute logic in the current thread after the asynchronous
NetContext.getCosumer()
                    .asyncAsk(userInfoAsk, UserInfoAnswer.class, userId)
                    .whenComplete(sm -> {
                        // do something
                    );
```

#### 4. [hotswap](hotswap/src/test/java/com/zfoo/hotswap/ApplicationTest.java) hot update code, no need to stop the server, no additional configuration, just one line of code to start hot update

```
// Pass in the class file that needs to be updated
HotSwapUtils.hotswapClass(bytes);
```

#### 5. [orm](orm/README.md) automatic mapping framework based on mongodb，The secondary cache is designed using [caffeine](https://github.com/ben-manes/caffeine) to fully release the database pressure

```
// You don't need to write sql and any configuration yourself, define a table in the database directly through annotation definitions
@EntityCache
public class UserEntity implements IEntity<Long> {
    @Id
    private long id;
    private String name;
}

// update database data
entityCaches.update(userEntity);
```

#### 6. [event](event/src/test/java/com/zfoo/event/ApplicationTest.java) use the observer design pattern, decouples different modules and improves the quality of the code

```
// To receive an event, you only need to add an annotation to the method and the method will be automatically listen for the event
@EventReceiver
public void onMyNoticeEvent(MyNoticeEvent event) {
    // do something
}

// fire an event
EventBus.syncSubmit(MyNoticeEvent.valueOf("同步事件"));
EventBus.asyncSubmit(MyNoticeEvent.valueOf("异步事件"));
```

#### 7. [scheduler](scheduler/README.md) scheduling Framework Based on Cron Expression

````
@Scheduler(cron = "0/1 * * * * ?")
public void cronSchedulerPerSecond() {
    // do something
}
````

#### 8. [storage](storage/src/test/java/com/zfoo/storage/ApplicationTest.java) Excel and Java class automatic mapping framework, you only need to define a class corresponding to Excel, and directly parse Excel

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

Ⅴ. Commit specification👏
----------

- People who like this project are welcome to maintain this project together, and pay attention to the following
  specifications when submitting code
- The code formats uses the default formatting of IntelliJ Idea
- [conventional-changelog-metahub](https://github.com/pvdlg/conventional-changelog-metahub#commit-types)

Ⅵ. License
-----------

zfoo use [Apache License Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)

```
Copyright (C) 2020 The zfoo Authors

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.
```
