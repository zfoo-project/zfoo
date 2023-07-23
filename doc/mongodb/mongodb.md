# 一、MongoDB常用命令

## 1.数据库常用命令

```
show dbs            # 查询数据库列表
use student         # 该命令如果数据库不存在，将创建一个新的数据库，否则将返回现有的数据库。  
db.dropDatabase()   # 删除新的数据库
db.repairDatabase() # 整理数据库，不仅能整理碎片还可以回收磁盘空间，但是会产生锁，建议停服的时候做
db.getName()        # 查看当前使用的数据库
db.version()        # 当前db的版本
db.getMongo()       # 查看当前db的链接机器地址
db.getPrevError()   # 查询之前的错误信息
db.resetError()     # 清除错误记录
db.help()           # 查看数据库帮助文档
``` 

## 2.集合常用命令

```
show collections                 # 查看所有的集合
db.createCollection("student")   # 创建一个集合
db.createCollection("log", {size: 20, capped:true, max: 100});  # 或者带参数创建固定集合
db.student                       # 使用集合
db.student.drop()                # 删除当前集合
db.student.validate()            # 验证集合是否有问题，验证集合内容


db.student.count()               # 查询当前集合的数据条数
db.student.dataSize()            # 查看集合数据大小
db.student.totalIndexSize()      # 查看集合索引大小
db.student.storageSize()         # 集合分配的空间大小,包括未使用的空间
db.student.totalSize()           # 显示集合总大小，包括索引和数据的大小和分配空间的大小
db.student.getDB()               # 显示当前集合所在的db
db.student.stats()               # 显示当前集合的状态
db.student.getShardVersion()     # 集合的分片版本信息
db.student.renameCollection("students");    # 集合重命名 
db.printCollectionStats()        # 显示当前db所有集合的状态信息
db.student.help()                # 查看集合命令帮助文档
```

- 集合内存

```
在MongoDB中并不需要创建集合。 当插入一些文档 MongoDB 会自动创建集合。
capped:	Boolean	
（可选）如果为true，它启用上限集合。上限集合是一个固定大小的集合，当它达到其最大尺寸会自动覆盖最老的条目。 
如果指定true，则还需要指定参数的大小
size:	number	（可选）指定的上限集合字节的最大尺寸。如果capped 是true，那么还需要指定这个字段
autoIndexID	Boolean	（可选）如果为true，自动创建索引_id字段。默认的值是 false
max	number	（可选）指定上限集合允许的最大文件数

DB.createCollection("student", { capped : true, size : 6142800, autoIndexId : true, max : 10000 })
DB.createCollection("student")
```

## 3. 文档常用命令

### 增

```
db.student.insert({name:"jay", age: 100})

如果我们不指定_id参数插入的文档，那么 MongoDB 将为文档分配一个唯一的ObjectId。_id 是12个字节十六进制数在一个集合的每个文档是唯一的。
0-3时间戳，4-6机器唯一标识的散列标识，7-8进程号PID，9-11计数器。前三个保证同一个机器不同进程产生的标识唯一，后一个保证同一个进程产生不同的标识。
简介说就是在分布式系统中可以在本机产生全局唯一id。
```

### 删

```
db.student.remove({name : "jay"},true);    # true是只删除一条，默认是false全部删除
db.student.remove({name : "jay"});
```

### 改

```
db.student.save({name : "jay", age : 20})                            # save如果数据库已经有这条数据，则会更新它；如果没有，则写入

db.student.update({name:"jay"},{$set:{age : 20, company: "google"}});
db.student.update({name:"jay"},{$set:{age : 20}},{multi:true});      # $set修改器；multi默认false，只更新找到的第一条记录
db.student.update({name:"jay"},{$unset:{company:1}});                # $unset用于取消字段，去掉文档中的某个字段
db.student.update({name:"jay"},{$inc: {age: 50}});                     # $inc用于增加或减少数值
db.student.update({name:"jay"},{$push: {phone: {home:456789}}});     # $push用于把元素追加到数组中，如果字段不存在，会新增
db.student.update({name:"jay"},{$pushAll: {phone: [{home:456789}     # $pushAll用于把元素追加到数组中，多个元素
db.student.update({name:"jay"},{$pull: {phone:{home: 456789} }});    # $pull删除数组中满足条件的元素
db.student.update({name:"jay"},{$addToSet: {phone: {home:456789}}}); # $addToSet类似于push，但是只有这个值不在数组中才会增加
db.student.update({name:"jay"},{$pop: {phone:1}});                     # $pop删除数组中最后一个元素
db.student.update({name:"jay"},{$rename: {phone:"call"}});           # $rename修改字段名称
```

### 查

```
db.student.find()
db.student.find().pretty();                         # 如果需要结构化显示返回的文档可以加上pretty()方法
db.student.find({name:"jay"},{age:1})           # 查询name伟jay，只显示age

db.student.count({$or: [{age: 14}, {age: 28}]});    # 查询满足条件的文档数量
```

## 4.索引

### 创建索引

```
db.student.ensureIndex({age: 1});            # 1表示age索引按升序排序
db.student.ensureIndex({name: 1, age: -1});  # -1表示同组文档按降序排序

例如我们有数据:
{"name":"jay",age:14}
{"name":"ad",age:14}
{"name":"ad",age:38}
{"name":"ad",age:24}
{"name":"ab",age:14}
使用db.student.ensureIndex({name: 1, age: -1})建议索引后，索引中的数据组织为:
{"name":"ab",age:14}
{"name":"ad",age:38}
{"name":"ad",age:24}
{"name":"ad",age:14}
{"name":"jay",age:14}
```

### 创建唯一索引

```
db.student.ensureIndex({name:1},{unique:true}) # 如果创建唯一索引时已经存在重复项，通过dropDups在创建唯一索引时消除重复文档
db.student.ensureIndex({name:1},{unique:true,dropDups:true})
```

### 索引操作

```
db.student.getIndexes();     # 查询集合所有索引
db.student.totalIndexSize(); # 查看集合总索引记录大小 
db.student.reIndex();        # 修复索引
db.student.dropIndex("name");# 删除指定索引
```

### 索引调优explain

```
db.student.find({name: "jaysunxiao" }).explain();
```

## 5.查询

### 单个文档大小

```
Object.bsonsize(db.user.findOne({'account':'xxx'})) # 返回文档的bytes数量
```

### 迭代器，一般用于遍历数据集。通过hasNext()判断是否有下一条数据，next()获取下一条数据

```javascript
var iterator= db.student.find();
while(iterator.hasNext()){
    let temp=iterator.next();
    print(temp.name);
}

// 迭代器还实现了迭代器接口，所以可以使用forEach。
var iterator= db.student.find();
iterator.forEach(function(temp){
    print(temp.name);
});
```

### find语法

```
find( query, fields)  
query 查询条件，相当于sql的where
fields 查询的结果，字段映射,0不显示，1显示
explain("allPlansExecution")  返回查询计划信息和查询计划的执行统计信息 

db.student.find({}, { "_id":0, "name":1}).limit(9).skip(5).sort({"name":-1}).pretty()  
```

### 条件查询

```
db.student.find({name:"jay",age:16})             # 与操作
db.student.find({$or: [{age: 14}, {age: 28}]});      # 或操作$or
db.student.find({age: {$gt: 20}})                    # 大于$gt
db.student.find({age: {$lt: 20}})                    # 小于$lt
db.student.find({age: {$gte: 20}})                   # 大于等于$gte
db.student.find({age: {$lte: 20}})                   # 小于等于$lte
db.student.find({name: { $type : "string" }});       # 类型查询$type

db.student.find({age: {$exists: true}})            # 是否存在$exists
db.student.find({age: {$mod : [10, 0]}});          # 取模$mod
db.student.find({age: { "$ne" : 23}})              # 不等于$ne
db.student.find({name:{ "$in" : [ "jay" , "ab"]}}) # 包含$in
db.student.find({name:{$not:{"$in":["jay","ab"]}}})# 取非
db.student.find({name:{ "$nin":["jay","ab"]}})     # 不包含$nin
```

### null查询

```
db.student.find({company:null})                    # 查询company字段为null的文档
db.student.find({company:{$nin:[null]} })          # 查询company字段不为空的文档\
```

### 正则查询

```
db.student.find({"name":/^j/})   # 以j开头的字符串
```

### 嵌套查询

```
db.student.find({"phone":{"home" : 123321,"mobile" :  15820123123}})     # 指定完整的文档，查询出子文档完全匹配指定文档的文档。
db.student.find({"phone.home":123321})   # 如果我们不知道子文档的完整文档，只知道子文档中一个字段的值，可以通过点查询。
```

### 数组查询

```
db.student.find({favorite_number :[6, 8]});              # 精确匹配查询
db.student.find({favorite_number:6});                    # 数组单元素查询
db.student.find({favorite_number : {$all : [6, 8]}});    # 数组多元素查询
db.student.find({favorite_number: {$size: 3}});          # $size数组长度查询
db.student.find({},{favorite_number: {$slice: 2}});      # $slice返回数组子集
db.student.find({},{favorite_number: {$slice: -2}});
db.student.find({phone.home:123456});                  # 点查询用于查询更复杂的数组，例如数组中包含的是子文档的情况
db.student.find({phone.home:123456,"phone.mobile":13820123123});   #  数组的子文档如果有多个字段，查询出子文档同时满足两个条件的文档有两种方式
db.student.find( {       # 或者，两者等价
         phone: {
           $elemMatch: {
                home :123456,
              mobile: 13820123123
}}})   
```

### 索引查询

```
db.student.find({favorite_number.0:6});    # 数组都有索引，例如[6,8],6是第0个元素，8是第1个元素(数组索引以0开头)。要查找某个元素指定值的文档可以使用点和索引值
db.student.find({phone.2.home:123456});    # 点查询中只要数组的子文档里有一个home值满足查询值就会返回文档。如果我们要精确到第几个元素也可以用索引查询。
```

### 高级查询$where

- Javascript与$where结合使用，下面的全部等价

```
db.student.find({age: {$gt: 18}});                       
db.student.find({$where: "this.age > 18"}); 
db.student.find("this.age > 18"); 
f = function() {return this.age > 18}; db.student.find(f); 
```

## 6.聚合查询

### count和distinct

```
db.student.count({age: {$gte: 18}})                  # 数量查询count
db.runCommand({"distinct":"student", "key":"age"})   # 不同值distinct
```

### 分组group

```
db.collection.group({
    key:{field:1},    
    initial:{count:0},    
    cond:{},    
    reduce: function ( curr, result ) { },    
   keyf：function(doc){},   
   finalize:function(result) {}
})


数据如下:
{ "_id" : ObjectId("58d9b6afa159504ca6c572e0"), "name" : "jay", "age" : 28, "company" : "google", "name" : "a2" }
{ "_id" : ObjectId("58d9ba2d6097167df6313438"), "name" : "jay", "age" : 14 }
{ "_id" : ObjectId("58d9ba2d6097167df6313439"), "name" : "ad", "age" : 14 }
{ "_id" : ObjectId("58d9ba2d6097167df631343a"), "name" : "ad", "age" : 38 }
{ "_id" : ObjectId("58d9ba2d6097167df631343b"), "name" : "ad", "age" : 24 }
{ "_id" : ObjectId("58d9ba2e6097167df631343c"), "name" : "ab", "age" : 14 }


我们这里可以做一个统计:每个年龄有多少人，则使用age字段作为key，在reduce时进行同组的数量计数，finalize中组织结果文档格式。
使用代码如下:
db.student.group({
    key:{age:1},    
    initial:{count:0},    
    cond:{"age":{$gt:13}},    
    reduce: function(curr,result) {    
     result.count += 1;
    },     
   finalize:function(result) {
      result.年龄=result.age;
      result.人数=result.count;
    }
});


使用MapReduce做一个统计:每个年龄有多少人，this表示当前文档，使用this.age的值作为key。在reduce时进行同组的数量计数，finalize中组织结果文档格式。
代码如下:
db.student.mapReduce(
	function () {
		emit(this.age, {age: this.age, count: 1});
	},
	function (key, values) {
		var count = 0;
		values.forEach(function(val) {
			count += val.count;
		});
		return {age: key, count: count};
	},
	{
		out: { inline : 1 },
		finalize: function (key, reduced) {
			return {"年龄": reduced.age, "人数": reduced.count};
		}
	}
)
第一个function是map函数，提交两个参数key和value，数据会根据key的值进行分组，把同组的value的值放入values数组中。
第二个function是reduce函数，key和values是第一个map函数的分组结果。
第三个out指定结果集生成在什么地方，inline:1，表示结果集放在内存中，仅适用于结果集符合16MB限制的情况
```

### $project

```
数据如下:
{ "_id" : ObjectId("58d9ba2d6097167df6313438"), "name" : { "first" : "adrian", "last" : "jay" }, "age" : 14, "phone" : 111128912345 }
使用代码:
db.student.aggregate(
   [
        { $project: { _id:0, age: 1, phone: "123456789", lastName: "$name.last" } }
   ]
)
$project映射结果集的文档结构，0表示排除字段，1显示字段，"123456789"直接赋值
```

### $match

```
db.student.aggregate([{$match:{"name.last":"jay"}}])
```

### $limit

```
db.student.aggregate({ $limit : 1 });
```

### $skip

```
db.student.aggregate({ $skip : 2 });
```

### $unwind

```
数据:
db.product.find()
{ "_id" : 1, "item" : "ABC1", "sizes" : [ "S", "M", "L" ] }

db.product.aggregate( [ { $unwind : "$sizes" } ] )
```

### $group

```
数据:
db.student.find()
{ "_id" : ObjectId("58d9ba2d6097167df6313438"), "name" : "jay", "age" : 14 }
{ "_id" : ObjectId("58d9ba2d6097167df6313439"), "name" : "ad", "age" : 14 }
{ "_id" : ObjectId("58d9ba2d6097167df631343a"), "name" : "ad", "age" : 38 }

把数据根据age字段进行分组，使用命令:
db.student.aggregate([{$group : {_id : "$age"}}])
```

### $sort

```
$sort将输入文档排序后输出，1为按字段升序，-1为降序。
我们有数据:
db.student.find()
{ "_id" : ObjectId("58d9ba2d6097167df6313438"), "name" : "jay", "age" : 14 }
{ "_id" : ObjectId("58d9ba2d6097167df6313439"), "name" : "ad", "age" : 14 }
{ "_id" : ObjectId("58d9ba2d6097167df631343a"), "name" : "ad", "age" : 38 }
使用命令:
db.student.aggregate([{$sort : {age :-1}}])
```

### $lookup

```
数据:
db.product.insert({"_id":1,"name":"产品1","price":99})
db.product.insert({"_id":2,"name":"产品2","price":88})

db.order.insert({"_id":1,"pid":1,"name":"订单1"})
db.order.insert({"_id":2,"pid":2,"name":"订单2"})
db.order.insert({"_id":3,"pid":2,"name":"订单3"})
db.order.insert({"_id":4,"pid":1,"name":"订单4"})
db.order.insert({"_id":5,"name":"订单5"})
db.order.insert({"_id":6,"name":"订单6"})

$lookup作左连接组合两个集合的信息使用命令如下:
db.order.aggregate([
      {
            $lookup:
           {
            from: "product",
            localField: "pid",
            foreignField: "_id",
            as: "orderDetail"
          }
    }
 ])

```

### $geoNear

```
一、平面

我们使用lng和lat的命名，新建数据如下:
db.places.save({name:"肯德基",loc : { lng: 40.739037, lat: 73.992964 },category:"餐饮"})
db.places.save({name:"麦当劳",loc : { lng : 42.739037, lat: 73.992964 },category:"餐饮"})
db.places.save({name:"农行",loc : { lng: 41.739037, lat: 73.992964 },category:"银行"})
db.places.save({name:"地铁站",loc : { lng: 40.639037, lat: 73.992964 },category:"交通"})

2d索引创建方式如下:
db.places.createIndex( { "loc": "2d" } )


我们现在需要查到地铁站附近的文档信息，可以使用:
db.places.find({loc : {$near : { lng: 40.639037, lat:73.992964  }}})



aggregate和$geoNear能指定到范围，比如我们要查范围在坐标值相差2度(平面单位)以内的文档:
db.places.aggregate([
   {
     $geoNear: {
        spherical:false,
        distanceMultiplier:1,
        near: { lng: 40.639037, lat:73.992964 },
        distanceField: "dist.distacnce",
        maxDistance: 2,
        query: { category: "餐饮" },
        includeLocs: "dist.location",
        num: 1
     }
   }
])

二、球面
新建数据如下:
db.places.save({name:"肯德基",loc : { type: "Point", coordinates: [ 40.639037, 73.992964 ] },category:"餐饮"})
db.places.save({name:"麦当劳",loc : { type: "Point", coordinates: [ 42.739037, 73.992964 ] },category:"餐饮"})
db.places.save({name:"农行",loc : { type: "Point", coordinates: [ 41.739037, 73.992964 ] },category:"银行"})
db.places.save({name:"地铁站",loc : { type: "Point", coordinates: [ 40.639037, 73.992964 ] },category:"交通"})

创建2dsphere 索引使用代码:
db.places.createIndex( { loc : "2dsphere" } )

$geoNear用于输出某一地理位置2公里内的文档代码如下:
db.places.aggregate([
   {
     $geoNear: {
        spherical: true,
        near: { type: "Point", coordinates: [ 40.639037, 73.992964 ] },
        distanceField: "dist.distacnce",
        maxDistance: 2000,
        query: { category:"餐饮" },
        includeLocs: "dist.location",
        num: 5       
     }
   }
])
```

### $group和管道表达式

```
测试数据准备:
db.product.insert({"_id":1,"name":"产品1","price":99,"type":"服装"})
db.product.insert({"_id":2,"name":"产品2","price":88,"type":"服装"})
db.product.insert({"_id":3,"name":"产品3","price":29,"type":"饰品"})
db.product.insert({"_id":4,"name":"产品4","price":78,"type":"服装"})
db.product.insert({"_id":5,"name":"产品5","price":9,"type":"饰品"})
db.product.insert({"_id":6,"name":"产品6","price":18,"type":"饰品"})


db.product.aggregate([{$group : {_id : "$type", price : {$sum : "$price"}}}])   # 求和$sum
db.product.aggregate([{$group : {_id : "$type", price : {$avg : "$price"}}}])   # 平均值$avg
db.product.aggregate([{$group : {_id : "$type", price : {$min : "$price"}}}])   # 最小值$min
db.product.aggregate([{$group : {_id : "$type", price : {$max : "$price"}}}])   # 最大值$max
db.product.aggregate([{$group : {_id : "$type", tags : {$push : "$name"}}}])    # 数组添加$push
db.product.aggregate([{$group : {_id : "$type", tags : {$addToSet : "$name"}}}])# 数组添加$addToSet
db.product.aggregate([{$group : {_id : "$type", product : {$first : "$name"}}}])# 首元素$first
db.product.aggregate([{$group : {_id : "$type", product : {$last : "$name"}}}]) # 尾元素$last	
```

### 复合使用示例

```
数据如下:
{ "_id" : ObjectId("58d9b6afa159504ca6c572e0"), "name" : "jay", "age" : 28, "company" : "google", "name" : "a2" }
{ "_id" : ObjectId("58d9ba2d6097167df6313438"), "name" : "jay", "age" : 14 }
{ "_id" : ObjectId("58d9ba2d6097167df6313439"), "name" : "ad", "age" : 14 }
{ "_id" : ObjectId("58d9ba2d6097167df631343a"), "name" : "ad", "age" : 38 }
{ "_id" : ObjectId("58d9ba2d6097167df631343b"), "name" : "ad", "age" : 24 }
{ "_id" : ObjectId("58d9ba2e6097167df631343c"), "name" : "ab", "age" : 14 }

使用代码如下:
db.student.aggregate(
    [
        { $match: { age:{"$gt":13} } },
        { $sort: { age: 1 } },
        { $limit: 2 },
        { $group: { _id: "$age", "人数": { $sum: 1 } } },     
   ]
)

aggregate默认返回分组信息_id，如果要去掉，可使用$project。
因为aggregate是按顺序处理的管道阶段操作器，所以管道的排序也很重要，下面两端代码使用相同的管道阶段操作器，但是不同的顺序，实现的效果也是不同的:
代码一:
db.student.aggregate(
    [
        { $match: { age:{"$gt":13} } },
        { $sort: { age: 1 } },
        { $limit: 2 },
        { $group: { _id: "$age", "人数": { $sum: 1 } } }     
   ]
)


代码二:
db.student.aggregate(
    [
        { $match: { age:{"$gt":13} } },    
        { $group: { _id: "$age", "人数": { $sum: 1 } } },  
        { $sort: { _id: 1 } },
        { $limit: 2 }  
   ]
)
```

## 7.性能监控

- db.stats()，查看当前数据库的状态，可看到数据库的名称，集合（表）数量，索引数量、大小，数据文件大小，存储空间大小和物理文件大小。

- db.serverCmdLineOpts()，查看启动参数

- db.serverStatus()，查询MongoDB的服务状态，有助于了解诊断和性能分析

```
serverStatus命令是静态的监控，MongoDB提供了动态的监控执行工具mongostat。

db.serverStatus().host      # 主机名
db.serverStatus().locks     # 锁信息
db.serverStatus().globalLock        # 全局锁信息
db.serverStatus().mem               # 内存信息
db.serverStatus().connections       # 连接数信息
db.serverStatus().extra_info        # 额外信息
db.serverStatus().indexCounters     # 索引统计信息
db.serverStatus().backgroundFlushing    # 后台刷新信息
db.serverStatus().cursors           # 游标信息
db.serverStatus().network           # 网络信息
db.serverStatus().repl              # 副本集信息
db.serverStatus().opcountersRepl    # 副本集的操作计数器
db.serverStatus().opcounters        # 操作计数器
db.serverStatus().asserts           # 断言信息Asserts
db.serverStatus().writeBacksQueued  # writeBacksQueued
db.serverStatus().dur               # 持久化(dur)
db.serverStatus().recordStats       # 记录状态信息
db.serverStatus( { workingSet: 1 } ).workingSet    # 工作集配置
db.serverStatus().metrics           # 指标信息metrics
```

- mongostat，会动态输出一些serverStatus提供的重要信息，每秒输出一次。

```
mongostat会动态输出一些serverStatus提供的重要信息，每秒输出一次。

insert query update delete getmore command flushes mapped vsize   res faults qrw arw net_in net_out conn                time
    *0    *0     *0     *0       0    11|0       0     0B  237M 15.0M      0 0|0 0|0  1.50k   22.7k    1 May  1 14:24:25.425
    *0    *0     *0     *0       0    12|0       0     0B  237M 15.0M      0 0|0 0|0  1.55k   23.5k    1 May  1 14:24:26.405
    *0    *0     *0     *0       0    11|0       0     0B  237M 15.0M      0 0|0 0|0  1.52k   23.0k    1 May  1 14:24:27.405
    *0    *0     *0     *0       0    12|0       0     0B  237M 15.0M      0 0|0 0|0  1.52k   23.1k    1 May  1 14:24:28.404
    *0    *0     *0     *0       0    11|0       0     0B  237M 15.0M      0 0|0 0|0  1.52k   23.0k    1 May  1 14:24:29.404
    *0    *0     *0     *0       0    12|0       0     0B  237M 15.0M      0 0|0 0|0  1.53k   23.2k    1 May  1 14:24:30.396


insert、query、update、delete、getmore、command 每种对应操作的发生次数。
其中faults表示访问失败数，数据从内存交换出去，放到swap。值越小越好，最好不要大于100。

faults：查询从磁盘读取数据，标志服务器未达到最佳，所需的数据并未完全保存找内存中
qr/qw：队列等待的数目。
ar/aw：活动客户端的数目。
conn：打开的连接数。
flushes：数据刷写到磁盘的数目。
vsize：使用虚拟内存大小。
mapped：隐射的内存大小，约等于数据目录大小。
res：实际使用的内存大小
netId：表示通过网络传输进来的字节数。
netou：t表示通过网络传输出的字节数。
time：表示统计的时间。
```

- mongotop，查看哪个几个最繁忙

- db.currentOP()，可看到当前执行的进程，类似MySQL的show processlist

- show profile

```
db.setProfilingLevel(2)     # enable profiling
db.system.profile.find()    # show raw profile entries
show profile                # 该命令可以查看最近的5条记录
该命令可以查看最近的5条记录：
ts:该命令在何时执行。
millis：执行耗时，以毫秒为单位。
op：什么操作。
query：设置的查询条件。
nReturned：返回的条数。
docsExamined：文档扫描条数。

# 清空还原profile集合使用命令:
db.setProfilingLevel(0)
db.system.profile.drop()
db.createCollection("system.profile", { capped: true, size:4000000 } )
db.setProfilingLevel(1)
```

## 8.备份和恢复

### 1).数据库备份和恢复

- mongodump导出数据库中全部的内容，使用-q参数增加查询条件，注意-q参数值的标点符号，否则会报错 too many positional arguments

```
mongodump --uri mongodb://localhost:27017/robot --username=root --password=123456 --authenticationDatabase=admin -d test -o /home
mongodump --uri mongodb://localhost:27017/robot -u "root" -p "123456" --authenticationDatabase=admin -d test -c user -q "{name:'joe'}" -o /home
```

- mongorestore使用的数据文件就是mongodump备份的数据文件

```
# 使用/home路径下的BSON和JSON文件恢复数据库test，--drop参数表示如果已经存在test数据库则删除原数据库，去掉--drop则与原数据库合并
mongorestore --uri mongodb://localhost:27017/robot -u "root" -p "123456" --authenticationDatabase=admin /home/test --drop
```

### 2).表的备份和恢复

- 官网下载 MongoDB Database Tools Download
  - windows下直接拷贝bin可执行文件到MongoDB的安装目录下的bin
  - Linux下直接拷贝bin下的文件到MongoDB的安装目录下的bin，并且修改可执行权限


- 导出json格式的备份文件，从test数据库中student集合到/home/test.json文件中

```
mongoexport --uri mongodb://localhost:27017/test -u "root" -p "123456" --authenticationDatabase=admin --collection=student --type=json -o /home/student.json
```

- 导出csv格式的备份文件

```
# -f参数用于指定只导致id和name以及age字段，因为csv是表格类型的，所以对于内嵌文档太深的数据导出效果不是很好，所以一般来说会指定某些字段导出。
mongoexport --uri mongodb://localhost:27017/test -u "root" -p "123456" --authenticationDatabase=admin --collection=student --type=csv -fields="_id,name,age" -o /home/student.csv
```

- json格式导入，使用备份文件/home/test.json导入数据到test数据库的student集合中

```
# --upsert表示更新现有数据，如果不使用—upsert则导入时已经存在的文档会报_id重复，数据不再插入。也可以使用--drop删除原数据。
mongoimport --uri mongodb://localhost:27017/test -u "root" -p "123456" -authenticationDatabase=admin --collection=student --type=json /home/test.json --upsert
```

- csv格式导入，--headerline指明不导入第一行,csv格式的文件第一行为列名

```
mongoimport --uri mongodb://localhost:27017/test -u "root" -p "123456" -authenticationDatabase=admin --collection=student --type=csv --headerline --file /home/test.csv
```
