English | [简体中文](./README_CN.md)

### Ⅰ. Introduction

- [orm](https://github.com/zfoo-project/zfoo/blob/main/orm/README.md) Based on MongoDB's orm framework, it provides a
  mapping between POJO objects and Mongo DB databases
- MongoDB is a distributed database that can be used on a single machine or distributed in a single machine
- High-performance database entity object caching based on [caffeine](https://github.com/ben-manes/caffeine)

### Ⅱ. Use

#### 1. Direct use (low level API), through the low-level API provided by MongoDB to operate the database

- Start by getting the Collection through Orm Manager

```
var collection = OrmContext.getOrmManager().getCollection(UserEntity.class)
```

- Manipulate collections through collections
  - insert，collection.insertOne(obj)
  - delete，collection.deleteOne(obj)
  - update，collection.updateOne(obj)
  - query，collection.find(Filters.eq("_id", userId))

#### 2. Indirect use (middle level API) to access data through the collection's simple wrapper IAccessor and IQuery interfaces

- IAccessor is a data access interface
  - Inserting data into the database uses the return value of the object's id() method as the primary key
  ```
  OrmContext.getAccessor().insert(obj)
  ```
  - To delete data in the database, the return value of the id() method of the object will be used as the search
    keyword, and the data with this id() as the main key will be deleted
  ```
  OrmContext.getAccessor().delete(obj);
  ```
  - Modify the data in the database
  ```
  OrmContext.getAccessor().update(obj);
  ```
  - Load data from the database
  ```
  OrmContext.getAccessor().load(id, class);
  ```

- IQuery is data complex query interface
  - The methods currently offered are: queryFieldLike，queryAll，queryFieldEqual，queryFieldIn，pageQuery

#### 3. Cache usage (high level API), cache entity regular refresh cache to the database

- Specify a list of policies for the database and cache through the following configuration

```
<orm:config id="config" entity-package="com.zfoo.orm.**.entity">

    <orm:host database="${mongodb.database}" user="${mongodb.user}" password="${mongodb.password}">
        <orm:address name="${mongodb.address.name}" url="${mongodb.address.url}"/>
    </orm:host>

    <!-- Caching policy -->
    <orm:caches>
        <orm:cache strategy="default" size="6000" expire-millisecond="600000"/>
        <orm:cache strategy="thousand" size="1000" expire-millisecond="600000"/>
        <orm:cache strategy="threeThousand" size="3000" expire-millisecond="600000"/>
    </orm:caches>

    <!-- Persistence policy -->
    <orm:persisters>
        <orm:persister strategy="default" type="time" config="600000"/>
        <orm:persister strategy="cron30s" type="cron" config="0/30 * * * * ?"/>
        <orm:persister strategy="time30s" type="time" config="30000"/>
    </orm:persisters>
</orm:config>

database indicates which database to operate
address indicates the address of the database and supports sharding configuration
```

- mappings through annotations

```
@EntityCaches(cacheStrategy = @Cache("tenThousand"), persister = @Persister("time30s"))
public class UserEntity implements IEntity<Long> {
}

cache strategy represents a caching strategy, the data in the database is first read into the Entity Caches cache in Orm, 
such as thousand, which means that 1000 pieces of data in the cache database expire in 10 minutes

persisters represents a persistence strategy, such as time 30 s, which means that the cached data in the Entity Caches is 
written to the database every 30 seconds, and even if it goes down midway, only 30 s seconds of data is lost
```

- How to use the cache
  1. By annotation
  ```
  @Component
  public class UserManager {
  
      @EntityCachesInjection
      public IEntityCaches<Long, UserEntity> userEntityCaches;
  
  }
  ```
  2. Dynamically obtained through classes
  ```
  IEntityCaches<Long, UserEntity> userEntityCaches = OrmContext.getOrmManager().getEntityCaches(UserEntity.class);
  ```

- Update the object

```
userEntityCaches.update(entity);
```

### Ⅲ. Notes

- If you don't want to map an attribute, just add the transient keyword
- Supports basic data properties (byte, short, int, long, float, double, boolean), string String, custom objects, does
  not support generics
- Arrays support one-dimensional arrays, and collections support List, Set
- Map type: Mongo DB officially restricts key to String
- Database primary keys can use integers as much as possible, because Mongo DB's default primary key is a string, which
  takes up space
- The database uses a self-developed ORM framework, such as an entity class User Entity, which maps to the collection in
  the database as user, with the first letter lowercase, and the Entity removed
- Intelligent parsing, does not support generic and circular referenced objects, and the wrong entity object definition
  will fail to start the program with an error warning

### Ⅳ. Tutorial

- [test](https://github.com/zfoo-project/zfoo/tree/main/orm/src/test/java/com/zfoo/orm) contains all tutorials for
  adding, deleting, and modifying, please install Mongo DB before running
