简体中文 | [English](./README.md)

### Ⅰ. 简介

- [zfoo protocol](https://github.com/zfoo-project/zfoo/blob/main/protocol/README.md)
  是极致性能的Java二进制序列化和反序列化的框架，并且为序列化字节最少的框架
- 协议目前原生支持 **C++ Java Javascript C# Go Lua GDScript Python**，可以轻易实现跨平台
- 协议可以自定义私有协议格式，让你的协议更加安全，支持增加字段和兼容前后版本协议
- 兼容protobuf，支持生成protobuf协议文件，提供从pojo到proto的生成方式
- proto文件到生成pojo的方式已经有不少jetbrains插件可以做到，可以在idea的plugin中搜索proto

### Ⅱ. 快速使用

- 环境要求**JDK 17+**，支持**OpenJDK**, **Oracle JDK**, **native GraalVM**

- protocol是独立项目，不依赖其它项目，可以直接打开，本地install到自己的本地maven仓库，即可单独使用

```
// zfoo协议注册，只能初始化一次
ProtocolManager.initProtocol(Set.of(ComplexObject.class, ObjectA.class, ObjectB.class));

// 序列化
ProtocolManager.write(byteBuf, complexObject);

// 反序列化
var packet = ProtocolManager.read(buffer);
```

### Ⅲ. 性能测试

- 单线程环境，在没有任何JVM参数调优的情况下速度比Protobuf快50%，比Kryo快100%，[参见性能测试](src/test/java/com/zfoo/protocol/BenchmarkTesting.java)

- 线程安全，zfoo和Protobuf的性能不受任何影响，kryo因为线程不安全性能会有所损失，[参见性能测试](src/test/java/com/zfoo/protocol/BenchmarkTesting.java)


- 测试环境

```
操作系统：win10
cpu： i9900k
内存：64g
```

- 单线程测试，横坐标为序列化和反序列化的对象数量，纵坐标为花费的时间（单位毫秒）
  ![Image text](../doc/image/protocol/simple_object.png)
  ![Image text](../doc/image/protocol/normal_object.png)
  ![Image text](../doc/image/protocol/complex_object.png)

### Ⅳ. 为什么快

- 使用Javassist字节码增强动态生成顺序执行的序列化和反序列化函数，顺序化的函数可以轻易的被JIT编译以达到极致的性能
- 原生集成netty的高性能ByteBuf
- 使用primitive type collection，没有装箱和拆箱，避免了无效GC，性能也足够快
- 天生线程安全并且无锁化；kryo强制要求每条线程都有自己的一个Kryo实例，这是一个比较重的设计，特别是线程比较多的场景
- 没有反射，没有unsafe操作；对比kryo中使用objenesis导致大量unsafe，而且在Java11中运行会出现警告
- 扁平化了方法栈的调用深度，数据结构嵌套没有任何性能损失，如List<Set<Map<>>>；对比kryo和protobuf数据结构嵌套会出现性能损失
- 无漏洞注入风险，只有初始化时会进行字节码增强，后期不会再进行任何字节码的操作

```
数据压缩体积小，压缩体积比kryo和protobuf都要小；比kryo小是因为kryo需要写入每个对象的注册号
智能语法分析，错误的协议定义将无法启动程序并给出错误警告
提升开发效率，完全支持POJO方式开发，使用非常简单
```

### Ⅴ. 为什么小

- 轻量级实现，核心序列化和反序列化代码一千行左右
- 优化了int和long的zigzag和varint编码的算法，避免了一些多余的方法调用和位操作
- 数据压缩体积小，压缩体积比kryo和protobuf都要小；比kryo小是因为kryo需要写入每个对象的注册号
- 智能语法分析，错误的协议定义将无法启动程序并给出错误警告
- 提升开发效率，完全支持POJO方式开发，使用非常简单

```
目前的序列化过后对象的大小如下：
简单对象，zfoo包体大小8，kryo包体大小5，protobuf包体大小8
常规对象，zfoo包体大小430，kryo包体大小483，protobuf包体大小793
复杂对象，zfoo包体大小2216，kryo包体大小2528，protobuf包体大小5091
```

### Ⅵ. 数据类型

- 默认的数据格式支持，无需用户手动注册，[参考类定义](src/test/java/com/zfoo/protocol/packet/ComplexObject.java)
    - boolean，byte，short，int，long，float，double，char，String
    - Boolean，Byte，Short，Integer，Long，Float，Double，Character，序列化的时候如果null，会给个默认值0（Character默认值为Character.MIN_VALUE）
    - int[]，Integer[]，如果是null，则解析后的为一个长度为0的数组
        - 原生泛型List，Set，Map，反序列化返回类型为HashSet，ArrayList，HashMap，并且空指针安全（返回大小为0的集合）
        - List<Integer>，必须指定泛型类，如果发送的是[1,1,null,1]，接收到的是[1,1,0,1]
        - List<XXXClass>，如果发送的是[obj,obj,null,obj]，接收到的是[obj,obj,null,obj]，即引用类型序列化之前为null，序列化之后同样为null

- 不支持的数据格式，因为zfoo会自动识别不支持的类型并且给出错误警告，所以用户不必太关心
    - int[][]，二维以上数组，考虑到不是所有语言都支持多维数组
    - List<Integer>[]，Map<Integer, Integer>[]，Java语言本身就没有支持泛型类数组
    - List<int[]>，Map<Integer, Integer[]>，泛型里面套数组，这种写法看起来比较奇怪，实际使用的地方很少
    - 枚举类，考虑到很多其他语言不支持枚举类，可以用int或者string在代码层面做替换
    - 自定义泛型类XXXClass<T>，泛型类在很多框架中都极易出现性能上和解析上的问题，而且并不是所有语言都支持
    - 循环引用，虽然底层支持循环引用，但是考虑到循环引用带来语义上难以理解，容易出现错误，所以就屏蔽了

### Ⅶ. 协议规范

- 协议类必须是简单的javabean，不能继承任何其它的类，但是可以继承接口

- 协议号定义为short类型是为了减少包体积和内存大小，一个包可以减少2个byte，每个协议的在应用内存也可以降低6byte（protocols +
  IProtocolRegistration + protocolIdMap）

```
一个项目的协议体类很难超过3w，会有工具自动把你的协议号打包的紧凑一点，这样你的协议号就不会超过3w了
```

- 协议类必须标注协议号，有三种方式
    - 第一种使用注解：@Protocol(id = protocolId)
      ```
      @Protocol(id = 104)
      public class SimpleObject {
      
          public int c;
          public boolean g;
      
      }
      ```

    - 第二种使用：通过ProtocolManager.initProtocolAuto()去注册协议就可以不写协议号
      ```
      public class SimpleObject {
      
          public int c;
      
          public boolean g;
      
      }
      ```

- 如果为了版本兼容，增加字段，需要加上Compatible注解，order需要自然增大，这样就可以保证新老协议可以互相兼容
- 正式环境为了版本兼容，避免修改字段名称，内部默认使用字段的名称按照字符串的自然顺序来依次读写的（也可以自定义），所以会导致序列化出现异常
- 正式环境为了版本兼容，避免减少字段，没必要一定要删除一个不需要的字段，所以不考虑支持这种减少字段兼容协议的情况
- 设计模式六大原则中的开闭原则是对扩展开放，对修改关闭。协议的设计涉及到功能应该也要遵守这个原则，优先增加新的协议而不是修改现有协议

### Ⅷ. 在zfoo中使用Protobuf

- zfoo只提供了jprotobuf那种pojo的方式使用protobuf，但是可以通过pojo对象生成proto文件给客户端使用

- [jprotobuf](https://github.com/jhunters/jprotobuf) 针对Java程序开发一套简易类库，可以直接使用简单的pojo对象无需再去了解proto文件操作与语法

- 通过pojo对象生成proto文件给客户端使用，[生成proto配置](src/test/resources/protobuf.xml)

- 也可以自定义自己的生成方式，
  [使用代码自定义生成proto](https://github.com/zfoo-project/tank-game-server/tree/main/common/src/main/java/com/zfoo/tank/common/generate)



