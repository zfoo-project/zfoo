### net

- zfoo的一致性hash现在实现的就不好，可以模仿redis的这个虚拟槽分区

### protocol

- 考虑支持protobuf，放弃使用jprotobuf（可以省略很多不必要的注解，代码更加简洁），用约定大于配置的方式，还可以原生的和pojo对象结合起来


### orm

- 使用更高性能的Cache2k替换Caffeine

### other

1. protocol需要支持更多的语言，python，go，C++

2. Router中的ThreadLocal主要就是让上层@PacketReceiver所表示的收包方法少传一个参数IAttachment，这块设计可以优化

```
提前把这个参数存起来，就不用传到上层去用了，便于优雅的调用，但是也带来了一些不必要的性能损耗
```
