### Ⅰ. 简介

- [orm](https://github.com/zfoo-project/zfoo/blob/main/orm/README.md) 基于MongoDB的orm框架，提供POJO对象和MongoDB数据库之间的映射
- mongodb是分布式数据库，可以单机使用，也可以分布式使用

### Ⅱ. 直接使用方法(low level api)

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

- 加载数据库中的数据

```
OrmContext.getAccessor().load(id, class);
```

#### 2. IQuery接口，为数据复杂查询接口

- 目前提供的方法有：queryFieldLike，queryAll，queryFieldEqual，queryFieldIn，pageQuery

### Ⅲ. 缓存使用方法(high level api)

- 通过下列配置指定数据库和缓存的策略列表

```
<orm:config id="config" entity-package="com.zfoo.orm.**.entity">

    <orm:host database="${mongodb.database}" user="${mongodb.user}" password="${mongodb.password}">
        <orm:address name="${mongodb.address.name}" url="${mongodb.address.url}"/>
    </orm:host>

    <!-- 缓存策略 -->
    <orm:caches>
        <orm:cache strategy="hundred" size="100" expire-millisecond="600000"/>
        <orm:cache strategy="thousand" size="1000" expire-millisecond="600000"/>
        <orm:cache strategy="threeThousand" size="3000" expire-millisecond="600000"/>
    </orm:caches>

    <!-- 持久化策略 -->
    <orm:persisters>
        <orm:persister strategy="cronDefault" type="cron" config="0,30 * * * * ?"/>
        <orm:persister strategy="cron30s" type="cron" config="0/30 * * * * ?"/>
        <orm:persister strategy="time30s" type="time" config="30000"/>
    </orm:persisters>
</orm:config>

database表示操作哪个数据库
address表示数据库的地址，支持分片的配置
```

- 通过注解引入映射

```
@EntityCaches(cacheStrategy = "tenThousand", persister = @Persister("time30s"))
public class UserEntity implements IEntity<Long> {
}

cacheStrategy表示一个缓存的策略，即将数据库中的数据先读入Orm中的EntityCaches缓存，如thousand这个策略表示，缓存数据库中1000条数据，10分钟过期
persisters表示一个持久化的策略，如time30s这个策略表示，将EntityCaches中的缓存数据每30s写入到数据库中一次，即使中途宕机，也只损失30s秒的数据
```

- 缓存的使用方法
  1. 通过注解
  ```
  @Component
  public class UserManager {
  
      @EntityCachesInjection
      public IEntityCaches<Long, UserEntity> userEntityCaches;
  
  }
  ```
  2. 通过类动态获取
  ```
  IEntityCaches<Long, UserEntity> userEntityCaches = OrmContext.getOrmManager().getEntityCaches(UserEntity.class);
  ```

- 更新对象

```
userEntityCaches.update(entity);
```

### Ⅳ. 注意事项

- POJO对象的属性必须提供get和set方法，否则无法映射
- 如果不想映射某属性，直接加上transient关键字
- 目前支持基本数据属性（byte，short，int，long，float，double，boolean），字符串String，List，Set集合属性的映射，不支持Map
- 数据库主键能用整数尽量用整数，因为MongoDB默认的主键是一个字符串，比较占空间
- 数据库使用自研的orm框架，比如一个实体类UserEntity，映射到数据库中的集合为user，首字母小写，去掉Entity
- 基于 [caffeine](https://github.com/ben-manes/caffeine) 的高性能数据缓存
- 智能语法分析，不支持泛型和循环引用的对象，错误的entity对象定义将无法启动程序并给出错误警告

### Ⅳ. 教程

- [test](https://github.com/zfoo-project/zfoo/tree/main/orm/src/test/java/com/zfoo/orm) 下中包含了所有增删改查的教程，运行之前请先安装MongoDB
