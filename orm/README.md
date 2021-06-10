### Ⅰ. 简介

- 基于MongoDB的orm框架，提供POJO对象和MongoDB数据库之间的映射

### Ⅱ. 注意事项

- POJO对象的属性必须提供get和set方法，否则无法映射
- 不支持泛型
- 如果不想映射某属性，直接加上transient关键字
- 目前支持基本数据属性（byte，short，int，long，float，double，boolean），字符串String，List，Set集合属性的映射，不支持Map
- 数据库主键能用整数尽量用整数，因为MongoDB默认的主键是一个字符串，比较占空间
- 数据库使用自研的orm框架，比如一个实体类UserEntity，映射到数据库中的集合为user，首字母小写，去掉Entity

### Ⅲ. 使用方法

#### 1. IAccessor接口，为数据访问接口

- 插入数据到数据库，会以对象的id()方法的返回值作为主键

```
OrmContext.getAccessor().insert(obj)
```

- 删除数据库中的数据，会以对象的id()方法的返回值作为查找关键字，删除以这个id()为主键的数据

```
OrmContext.getAccessor().delete(obj);
```

- 修改数据库中的数据

```
OrmContext.getAccessor().update(obj);
```

#### 2. IQuery接口，为数据复杂查询接口

#### 3. 缓存使用方法

- 例如有下列配置

```
<orm:config id="config" entity-package="com.zfoo.orm.**.entity">

    <orm:host database="test" url="mongodb://127.0.0.1:27017" />

    <!-- 缓存策略 -->
    <orm:caches>
        <orm:cache strategy="ten" size="10" expire-millisecond="600000"/>
        <orm:cache strategy="hundred" size="100" expire-millisecond="600000"/>
        <orm:cache strategy="thousand" size="1000" expire-millisecond="600000"/>
        <orm:cache strategy="threeThousand" size="3000" expire-millisecond="600000"/>
        <orm:cache strategy="tenThousand" size="10000" expire-millisecond="600000"/>
    </orm:caches>

    <!-- 持久化策略 -->
    <orm:persisters>
            <orm:persister strategy="cronDefault" type="cron" config="0,30 * * * * ?"/>
            <orm:persister strategy="cron3s" type="cron" config="0/3 * * * * ?"/>
            <orm:persister strategy="cron15s" type="cron" config="0/15 * * * * ?"/>
            <orm:persister strategy="cron30s" type="cron" config="0/30 * * * * ?"/>
            <orm:persister strategy="cron1m" type="cron" config="0 0/30 * * * ?"/>
            <orm:persister strategy="time30s" type="time" config="30000"/>
        </orm:persisters>
</orm:config>

```

- 有下列注解

```
@EntityCaches(cacheStrategy = "tenThousand", persister = @Persister("time30s"))
public class UserEntity implements IEntity<Long> {
}
```

- database表示操作哪个数据库
- address表示数据库的地址，支持分片的配置
- caches中的strategy表示一个缓存的策略，即将数据库中的数据先读入Orm中的EntityCaches缓存，如hundred这个策略表示，缓存数据库中1000条数据，10分钟过期
- persisters中的strategy表示一个持久化的策略，如3s这个策略表示，将EntityCaches中的缓存数据每3s写入到数据库中一次，即使中途宕机，也只损失3秒的数据
- EntityCaches这个注解表示将会被Orm管理，使用hundred策略，缓存的持久化策略为3s

### Ⅳ. 教程

- test下中包含了所有增删改查的教程，运行之前请先安装MongoDB