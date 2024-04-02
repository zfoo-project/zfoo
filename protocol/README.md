English | [简体中文](./README_CN.md)

### Ⅰ. Introduction

- A decentralized serialization library, secure, private, reliable, and compatible, allowing everyone to have a unique
  set of protocols
- The protocol is currently natively supported **C++ Java Javascript TypeScript C# Go Lua GDScript Python**，It's easy to
  do cross-platform
- The protocol can customize the private protocol format to make your protocol more secure, and supports adding fields
  and being compatible with previous and subsequent protocols
- Support to generate ProtoBuf's Proto file to Pojo protocol file, and the generated protocol can be converted to other
  protocols and used by Java

### Ⅱ. Quick use

- Environment requirement **JDK 17+**, support **OpenJDK**, **Oracle JDK** and **native GraalVM**

```
// The zfoo protocol is registered and can only be initialized once
ProtocolManager.initProtocol(Set.of(ComplexObject.class, ObjectA.class, ObjectB.class));

// serialization
ProtocolManager.write(byteBuf, complexObject);

// deserialization
var packet = ProtocolManager.read(buffer);
```

### Ⅲ. Performance testing

- [Benchmark Test](src/test/java/com/zfoo/protocol/BenchmarkTesting.java)

- Test the environment

```
system：win10
cpu： i9900k
memory：64g
```

- Single-threaded test with abscissa as the number of objects serialized and deserialized and time spent in milliseconds
  on the ordinate
  ![Image text](../doc/image/protocol/simple_object.png)
  ![Image text](../doc/image/protocol/normal_object.png)
  ![Image text](../doc/image/protocol/complex_object.png)

### Ⅳ. Why fast

- Use Javassist bytecode to enhance the dynamic generation of serialization and deserialization functions for sequential
  execution, and sequential functions can be easily JIT compiled to achieve extreme performance
- Natively integrated with netty's high-performance ByteBuf, support Zero Copy
- With primitive type collection, there is no boxing and unboxing, invalid GCs are avoided, and the performance is fast
  enough
- thread-safe and lock-free, without any performance loss in multi-threaded environment
- No reflection, no unsafe operations, support GraalVM
- Flattening the call depth of the method stack, and there is no performance penalty for nesting data structures, such
  as List<Set<Map<>>>; Comparing kryo and protobuf data structure nesting results in a performance penalty
- There is no risk of vulnerability injection, only bytecode enhancement will be performed during initialization, and no
  bytecode operations will be performed in the later stage
- Taking advantage of the principle of program locality, the serializer is placed in the short[] array, and the object
  serialization and deserialization will only look for the serializer once

```
The data compression volume is small, and the compression volume is smaller than that of Kryo and Protobuf; Smaller than kryo because kryo needs to write the registration number of each object
Intelligent syntax, incorrect protocol definitions will fail to start the program and give an error warning
Improve development efficiency, fully support POJO development, very easy to use
```

### Ⅴ. Why small

- Lightweight implementation, core serialization and deserialization code of about a thousand lines
- Optimized the zigzag and varint encoding algorithms for int and long, avoiding some redundant method calls and bit
  operations
- The data compression volume is small, and the compression volume is smaller than that of Kryo and Protobuf; Smaller
  than kryo because kryo needs to write the registration number of each object
- Intelligent syntax, incorrect protocol definitions will fail to start the program and give an error warning
- Improve development efficiency, fully support POJO development, very easy to use

```
The current size of the serialized object is as follows:
Simple objects, zfoo package size 8, kryo package size 5, protobuf package size 8
Regular objects, ZFOO package size 430, KRYO package size 483, Protobuf package size 793
For complex objects, ZFOO package size 2216, KRYO package size 2528, and Protobuf package size 5091
```

### Ⅵ. Data type

- Default data format support eliminates the need for users to register
  manually.[Reference class definition](src/test/java/com/zfoo/protocol/packet/ComplexObject.java)
    - boolean，byte，short，int，long，float，double，String
    - Boolean，Byte，Short，Integer，Long，Float，Double，String，If it is null during serialization, a default value will be
      given
    - int[]，Integer[]，If it is null, it is parsed as an array of length 0
        - Native generic List, Set, Map, deserialization return type Hash Set, Array List, Hash Map, and null pointer
          safe (returns a collection of size 0)
        - List<Integer>，You must specify a generic class if [1,1,null,1] is sent and [1,1,0,1] is received
        - List<XXXClass>，If [obj,obj,null,obj] is sent, [obj,obj,null,obj] is received, that is, the reference type is
          null before serialization, and it is also null after serialization

- Unsupported data formats, because ZFOO automatically recognizes unsupported types and gives error warnings, so users
  don't have to care too much
    - int[][]，Arrays above two dimensions, considering that not all languages support multidimensional arrays
    - List<Integer>[]，Map<Integer, Integer>[]，The Java language itself does not support generic class arrays
    - List<int[]>，Map<Integer, Integer[]>，Generics are set in arrays, which looks strange and has few actual uses
    - char or Character, many other languages do not support char and can be replaced with string
    - Enumerated classes, many other languages do not support enumerated classes and can be replaced with int or string
    - Custom generic class XXX Class, <T>generic classes are prone to performance and parsing problems in many
      frameworks, and are not supported in all languages
    - Circular references, although the underlying support circular references, but considering that circular references
      bring semantically difficult to understand and prone to errors, so they are blocked

### Ⅶ. Protocol specifications

- The protocol class must be a simple javabean, not inheriting from any other class, but can inherit an interface

- The protocol number is defined as a short type to reduce the packet size and memory size, a packet can be reduced by 2
  bytes, and the application memory of each protocol can also be reduced by 6 byte(protocols + IProtocolRegistration +
  protocolIdMap)

```
It is difficult for a project's protocol body class to exceed 3 w, and there will be tools that automatically package 
your protocol number a little more compactly, so that your protocol number will not exceed 3 w
```

- There are four ways to indicate that the protocol class
    - The first uses annotations: @Protocol(id = protocolId)
      ```
      @Protocol(id = 104)
      public class SimpleObject {
      
          public int c;
          public boolean g;
      
      }
      ```

    - The second use: Register the agreement through Protocol Manager.initProtocolAuto() without writing the protocol
      number
      ```
      public class SimpleObject {
      
          public int c;
      
          public boolean g;
      
      }
      ```

    - The third use: Register the protocol through ProtocolManager.initProtocol(xmlProtocols) in the protocol.xml file
      ```
      <protocols>
          <!-- Use class path -->
          <module id="1" name="common">
              <protocol id="100" location="com.zfoo.protocol.packet.ComplexObject"/>
              <protocol id="101" location="com.zfoo.protocol.packet.NormalObject"/>
              <protocol id="102" location="com.zfoo.protocol.packet.ObjectA"/>
              <protocol id="103" location="com.zfoo.protocol.packet.ObjectB"/>
              <protocol id="104" location="com.zfoo.protocol.packet.SimpleObject"/>
              <protocol id="105" location="com.zfoo.protocol.packet.VeryBigObject"/>
              <protocol id="106" location="com.zfoo.protocol.packet.EmptyObject"/>
          </module>
          <!-- Use a package name scans all protocol classes under the package path -->
          <module id="2" name="native">
              <protocol location="com.zfoo.net.packet.common"/>
              <protocol location="com.zfoo.tank.common.protocol.common"/>
          </module>
      </protocols>
      ```   

    - The fourth use: generate a protocol with a protocol number by defining a proto file, so that the protocol can be
      easily registered
      ```
      syntax = "proto3";
      package test.message;
    
      message SimpleObject {
          int64 aa = 1;
      }
    
      // If the tag of a field exceeds 1000, the field is considered to be a compatible protocol field
      message OneMessage {
          // This is a comment on the field
          int64 id = 1;
          // This is equivalent to adding a @Compatible annotation to this field
          string name = 1001;
      }
      ```

- If you add a field for version compatibility, you need to add a Compatible annotation, and the order needs to be
  naturally increased, so as to ensure that the old and new protocols can be compatible with each other, Protocol
  nesting is also still compatible
- In order to be compatible with versions and avoid modifying field names, default uses field names to read and write
  in the natural order of strings (can also be customized), so it will cause exceptions in serialization
- The official environment does not necessarily have to delete an unwanted field in order to be version compatible and
  avoid reducing fields
- Among the six principles of design patterns, the principle of opening and closing is open to expansion and closed to
  modification. The design of the protocol should also adhere to this principle when it comes to functionality,
  prioritizing the addition of new protocols over modifying existing ones

### Ⅷ. The difference between zfoo and Protobuf

- Protobuf can delete fields, but ZFOO does not support deleting fields, which improves performance by 1x and reduces
  the size by 1x at the expense

```
In fact, the officially launched project almost did not encounter anyone who would delete the field, 
and if this field was deleted, the server had to change the code not to reference the deleted field, 
and the client had to change the code not to reference the deleted field, which doubled the workload. 
If either of them forgets to modify the code, it will directly report an error, 
so adding a discarded field @Deprecated comment to this field in the actual project can avoid a lot of unnecessary trouble.
```

- zfoo takes the intersection of type declarations in all languages, instead of protobuf taking the union, simplifying
  the type implementation of protobuf
    - protobuf
      ```
      double
      float
      int32
      int64
      uint32
      uint64
      sint32
      sint64
      fixed32
      fixed64
      sfixed32
      sfixed64
      bool
      string
      bytes
      bytes
      ```
    - zfoo
      ```
      float
      double
      byte
      int16
      int32
      int64
      bool
      string
      ```

- zfoo takes the intersection of the grammars of all languages, instead of the union of protobuf, and adds the grammar
  implementation of protobuf
    - protobuf
      ```
      Collection nesting syntax is not supported
      ```
    - zfoo
      ```
      Supports nested collection syntax
      ```

- Simplified protobuf syntax, no enumeration, no oneof, no optional
