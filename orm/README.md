English | [简体中文](./README_CN.md)

### Ⅰ. Introduction

- [orm](https://github.com/zfoo-project/zfoo/blob/main/orm/README.md) is a MongoDB-based ORM framework that provides
  mapping between POJOs and MongoDB databases.
- MongoDB is a distributed database that can be run on a single node or in a distributed cluster.
- ORM provides a lightweight wrapper around the official MongoDB driver, allowing near-native usage.

### Ⅱ. Usage

#### 1. Direct usage (low-level API): operate the database directly through the official MongoDB driver API

- Start by getting the collection through `OrmManager`:

```
var collection = OrmContext.getOrmManager().getCollection(UserEntity.class)
```

- Perform operations on the collection:
  - insert: `collection.insertOne(obj)`
  - delete: `collection.deleteOne(obj)`
  - update: `collection.updateOne(obj)`
  - query: `collection.find(Filters.eq("_id", userId))`

#### 2. Indirect usage (middle-level API): access data through the collection wrapper interfaces `IAccessor` and `IQuery`

- `IAccessor` is the data access interface:
  - Inserting data into the database uses the value of the field annotated with `@Id` as the primary key:
  ```
  OrmContext.getAccessor().insert(obj)
  ```
  - Deleting data from the database uses the return value of the object's `id()` method as the lookup key and deletes
    the record with that primary key:
  ```
  OrmContext.getAccessor().delete(obj);
  ```
  - Update data in the database:
  ```
  OrmContext.getAccessor().update(obj);
  ```
  - Load data from the database:
  ```
  OrmContext.getAccessor().load(id, class);
  ```

- `IQuery` is the complex query interface:
  - The methods currently provided are: `queryFieldLike`, `queryAll`, `queryFieldEqual`, `queryFieldIn`, `pageQuery`

#### 3. Cache usage (high-level API): cache entities and periodically flush cached data to the database

- Specify the database, cache, and persistence strategy lists through the following configuration:

```
<orm:config id="config" entity-package="com.zfoo.orm.**.entity">

    <orm:host database="${mongodb.database}" user="${mongodb.user}" password="${mongodb.password}">
        <orm:address name="${mongodb.address.name}" url="${mongodb.address.url}"/>
    </orm:host>

    <!-- Caching policies -->
    <orm:caches>
        <orm:cache strategy="default" size="6000" expire-millisecond="600000"/>
        <orm:cache strategy="thousand" size="1000" expire-millisecond="600000"/>
        <orm:cache strategy="threeThousand" size="3000" expire-millisecond="600000"/>
    </orm:caches>

    <!-- Persistence policies -->
    <orm:persisters>
        <orm:persister strategy="default" type="time" config="600000"/>
        <orm:persister strategy="cron30s" type="cron" config="0/30 * * * * ?"/>
        <orm:persister strategy="time30s" type="time" config="30000"/>
    </orm:persisters>
</orm:config>

database indicates which database to operate on.
address indicates the database address and supports sharding configuration.
```

- Configure mappings through annotations:

```
@EntityCaches(cacheStrategy = @Cache("tenThousand"), persister = @Persister("time30s"))
public class UserEntity implements IEntity<Long> {
}

cacheStrategy specifies the caching strategy: data from the database is loaded into EntityCaches in ORM.
For example, "thousand" caches up to 1,000 entries from the database with a 10-minute expiration time.

persister specifies the persistence strategy: for example, "time30s" writes cached data in EntityCaches
to the database every 30 seconds; even in the event of an unexpected crash, at most 30 seconds of data is lost.
```

- How to use the cache:
  1. By annotation:
  ```
  @Component
  public class UserManager {
  
      @EntityCachesInjection
      public IEntityCaches<Long, UserEntity> userEntityCaches;
  
  }
  ```
  2. Dynamically retrieved by entity class:
  ```
  IEntityCaches<Long, UserEntity> userEntityCaches = OrmContext.getOrmManager().getEntityCaches(UserEntity.class);
  ```

- Update an entity:

```
userEntityCaches.update(entity);
```

### Ⅲ. Notes

- If you don't want to map a field, add the `transient` keyword.
- Supports primitive types (`byte`, `short`, `int`, `long`, `float`, `double`, `boolean`), `String`, and custom objects;
  generics are not supported.
- Arrays support one-dimensional arrays, and collections support `List` and `Set`.
- Use integer primary keys whenever possible, because MongoDB's default primary key is a string, which takes up more
  space.
- The framework uses a custom ORM convention: for example, an entity class `UserEntity` maps to the `user` collection
  in the database (first letter lowercased, with the `Entity` suffix removed).
- Intelligent syntax analysis: generic types and circularly referenced objects are not supported, and invalid entity
  definitions will prevent the application from starting and emit an error.

### Ⅳ. Tutorial

- [test](https://github.com/zfoo-project/zfoo/tree/main/orm/src/test/java/com/zfoo/orm) contains examples for all CRUD
  (insert, delete, update, query) operations. Please install MongoDB before running them.
