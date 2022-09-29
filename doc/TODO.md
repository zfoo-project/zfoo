### protocol

- 使用primitive type原生类型的hashmap，IntIntMap，IntLongMap，LongIntHashMap，LongLongHashMap

### event

- 使用更加简单的ConcurrentMap优化threadMap，JDK自带的ConcurrentHashMap速度慢

### orm

- 使用更高性能的Cache2k替换Caffeine
- 重新规划包，现在的分包太集中


### other

1. protocol需要支持更多的语言，python，go，C++

2. Router中的ThreadLocal主要就是让上层@PacketReceiver所表示的收包方法少传一个参数IAttachment，这块设计可以优化

```
提前把这个参数存起来，就不用传到上层去用了，便于优雅的调用，但是也带来了一些不必要的性能损耗
```
