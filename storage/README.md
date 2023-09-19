English | [简体中文](./README_CN.md)

### Ⅰ. Introduction

- [storage](https://github.com/zfoo-project/zfoo/blob/main/storage/README.md)
  Java pojo and excel/csv/json automatic mapping framework, only need to define a class corresponding to Excel, directly parse
  Excel, json, csv

- Take advantage of the reflection nature of the Java dynamic language to parse Excel without any code

- Support Excel to export json, csv files

### Ⅱ. Automatic mapping

- The first line of Excel corresponds to the Java class attribute, and the second and third lines do not serve as
  comments, where the first column must be the Id attribute

![Image text](../doc/image/storage/storage01.png)

- The Java class for Excel

![Image text](../doc/image/storage/storage02.png)

- In addition, you can also use the record class provided by Java's new features

![Image text](../doc/image/storage/storage03.png)

- After parsing, there are two ways to use it
  1. By annotation
  ```
   @Component
   public class StudentManager {

    @ResInjection
    private IStorage<Integer, StudentResource> studentResources;

    }
  ```
  2. Dynamically obtained through classes
  ```
  IStorage<Integer, StudentResource> studentResources = StorageContext.getStorageManager().getStorage(StudentResource.class);
  ```

- Find the corresponding row by id

```
var studentResource = studentResources.get(1000);
```

Alternatively, it can be obtained through the following methods

```
var resource = StorageContext.get(StudentResource.class, 1001);
```

- Find the corresponding row by index, default to a repeatable index, and return a list

```
var students = studentResources.getIndex(StudentResource::getName, "james0");
```

If you are a record class, the usage is as follows

```
var students = studentStorage.getIndexes(StudentResource::name, "james0");
```

- The unique index is obtained through Storage.getUniqueIndex(), and the index annotation needs to be marked as @Index(
  unique = true)

```
StudentResource student = storage.getUniqueIndex(StudentResource::idCard, "110101200007281903");
```

### Ⅲ. Hot update Excel Json

- [tank](https://github.com/zfoo-project/tank-game-server/blob/main/common/src/main/java/com/zfoo/tank/common/util/HotUtils.java)
  Distributed hot update Excel Json csv configuration file implementation

### Ⅳ. Use

- Financial analysis, data analysis statistics
- Numerical configuration in the game
