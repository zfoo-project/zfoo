### protocol

- 使用primitive type原生类型的hashmap，IntIntMap，IntLongMap，LongIntHashMap，LongLongHashMap

### event

- 使用更加简单的ConcurrentMap优化threadMap，JDK自带的ConcurrentHashMap速度慢

### orm

- 使用更高性能的Cache2k替换Caffeine
- 重新规划包，现在的分包太集中
