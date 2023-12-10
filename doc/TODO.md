### protocol

- 考虑用javassist的方式支持protobuf，用约定大于配置的方式，还可以原生的和pojo对象结合起来
- 通过protobuf的proto文件生成java的pojo源代码，通过动态编译java源代码的方式生成其它语言的源代码


### orm

- 使用更高性能的Cache2k替换Caffeine