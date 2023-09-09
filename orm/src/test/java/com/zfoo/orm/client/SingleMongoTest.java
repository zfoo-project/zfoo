package com.zfoo.orm.client;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.*;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.scheduler.util.TimeUtils;
import org.bson.Document;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static com.mongodb.client.model.Filters.eq;

/**
 * @author godotg
 * @version 1.0
 * @since 2018-07-11 12:20
 */
@Ignore
public class SingleMongoTest {

    // To connect to mongodb server
    private final MongoClient mongoClient = MongoClients.create(MongoClientSettings.builder()
            .applyToClusterSettings(builder -> builder.hosts(Arrays.asList(new ServerAddress("localhost", 27017))))
//            .applyToConnectionPoolSettings(builder -> builder.applySettings(ConnectionPoolSettings.builder().maxSize(300).build()))
//            .credential(MongoCredential.createCredential("root", "admin", "123456".toCharArray()))
            .build());

    // If MongoDB in secure mode, authentication is required.
    // Now connect to your databases
    private final MongoDatabase mongodb = mongoClient.getDatabase("test");

    @Test
    public void findTest() {
        System.out.println("Connect to database successfully!");
        System.out.println("MongoDatabase inof is : " + mongodb.getName());

        System.out.println("当前数据库中的所有集合是：");
        for (String name : mongodb.listCollectionNames()) {
            System.out.println(name);
        }

        // 查找并且遍历集合student的所有文档
        MongoCollection<Document> collection = mongodb.getCollection("student");
        System.out.println("Collection created successfully");
        collection.find().forEach(document -> System.out.println(document));
    }

    @Test
    public void findEqTest() {
        var collection = mongodb.getCollection("student");
        collection.find(eq("_id", 1)).forEach(document -> System.out.println(document));
    }

    @Test
    public void findPatternTest() {
        var pattern = Pattern.compile("jay");
        var query = new BasicDBObject("name", pattern);
        var collection = mongodb.getCollection("student");
        collection.find(query).forEach(document -> System.out.println(document));
    }

    @Test
    public void aggregateTest() {
        var collection = mongodb.getCollection("student");
        // likeNum中有9的文档根据age分组后统计数量。
        var result = collection.aggregate(Arrays.asList(
                Aggregates.match(Filters.eq("name", "jay1")),
                Aggregates.group("$age", Accumulators.sum("count", 1)))
        );
        System.out.println(result.first());
    }


    @Test
    public void insertOneTest() {
        // 查找并且便利集合student的所有文档
        MongoCollection<Document> collection = mongodb.getCollection("student");

        // 插入一个文档
        Document document = new Document("_id", 1)
                .append("name", "hello mongodb");
        collection.insertOne(document);

        collection.find().forEach(doc -> System.out.println(doc.toJson()));
    }

    @Test
    public void insertManyTest() {
        // 查找并且便利集合student的所有文档
        MongoCollection<Document> collection = mongodb.getCollection("student");

        //插入数据
        var datas = new ArrayList<Document>();
        for (int i = 0; i < 10; i++) {
            var studentName = "jay" + i;
            var document = new Document("name", studentName).append("email", studentName + "@zfoo.com").append("age", i);
            datas.add(document);
        }
        collection.insertMany(datas);
        // 查询年龄大于等于20，小于25
        FindIterable<Document> result = collection.find(new Document("age", new Document("$gte", 20).append("$lt", 25)));
        System.out.println(result.first());
    }

    @Test
    public void updateOneTest() {
        // 查找并且便利集合student的所有文档
        MongoCollection<Document> collection = mongodb.getCollection("student");

        // 更新一个文档，updateOne方法第一个参数是查询条件，如果查出多条也只修改第一条;第二个参数是修改条件。
        collection.updateOne(eq("age", 10), new Document("$set", new Document("name", "new hello mongodb")));

        collection.find().forEach(doc -> System.out.println(doc.toJson()));
    }


    @Test
    public void updateManyTest() {
        // 查找并且便利集合student的所有文档
        MongoCollection<Document> collection = mongodb.getCollection("student");

        // 更新一个文档
        collection.updateMany(eq("age", 10), new Document("$set", new Document("name", "new hello mongodb")));

        collection.find().forEach(doc -> System.out.println(doc.toJson()));
    }

    @Test
    public void deleteTest() {
        MongoCollection<Document> collection = mongodb.getCollection("student");

        // 删除一个文档
        collection.deleteOne(eq("_id", 1));

        collection.find().forEach(doc -> System.out.println(doc.toJson()));
    }



    // ******************************************************************************************************
    // 更新一个字段和更新多个字段测试，测试结果，区别不大，可以忽略


    // 耗时[42598]
    @Test
    public void updateAllFieldTest() {
        var startTime = TimeUtils.currentTimeMillis();
        var count = 10_0000;
        var mongoClient = MongoClients.create(MongoClientSettings.builder()
                .applyToClusterSettings(builder ->
                        builder.hosts(Arrays.asList(new ServerAddress("localhost", 27017))))
                .build());

        var mongodb = mongoClient.getDatabase("test");
        var collection = mongodb.getCollection("student");
        for (var j = 0; j < count; j++) {
            var key = j;
            var value = j;
            try {
                // 插入一个文档
                Document document = new Document("_id", key)
                        .append("name0", value)
                        .append("name1", value)
                        .append("name2", value)
                        .append("name3", value)
                        .append("name4", value)
                        .append("name5", value)
                        .append("name6", value)
                        .append("name7", value)
                        .append("name8", value)
                        .append("name9", value)
                        .append("name10", value);
                collection.updateOne(eq("_id", key), new Document("$set", document));
//                collection.insertOne(document);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }


        var endTime = TimeUtils.currentTimeMillis();
        System.out.println(StringUtils.format("耗时[{}]", endTime - startTime));
    }

    // 耗时[41971]
    @Test
    public void updateOneFieldTest() {
        var startTime = TimeUtils.currentTimeMillis();
        var count = 10_0000;
        var mongoClient = MongoClients.create(MongoClientSettings.builder()
                .applyToClusterSettings(builder ->
                        builder.hosts(Arrays.asList(new ServerAddress("localhost", 27017))))
                .build());
        var mongodb = mongoClient.getDatabase("test");
        var collection = mongodb.getCollection("student");
        for (var j = 0; j < count; j++) {
            var key = j;
            var value = j;
            try {
                // 插入一个文档
                Document document = new Document("_id", key)
                        .append("name0", value);
                collection.updateOne(eq("_id", key), new Document("$set", document));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }


        var endTime = TimeUtils.currentTimeMillis();
        System.out.println(StringUtils.format("耗时[{}]", endTime - startTime));
    }


    // ******************************************************************************************************
    // wiredTiger引擎测试
    // inMemory引擎测试
    private static final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 10);

    /**
     * inMemory测试结果如下：
     * 总线程数[1]，操作数[10000]，耗时[4212]
     * 总线程数[1]，操作数[1000000]，耗时[394215]
     * 总线程数[4]，操作数[1000000]，耗时[223355]
     * 总线程数[8]，操作数[1000000]，耗时[297476]
     * 总线程数[16]，操作数[1000000]，耗时[442782]
     * 总线程数[32]，操作数[1000000]，耗时[713675]
     * <p>
     * <p>
     * wiredTiger测试结果如下：
     * 总线程数[1]，操作数[10000]，耗时[4170]
     * 总线程数[1]，操作数[1000000]，耗时[400208]
     * 总线程数[4]，操作数[1000000]，耗时[230471]
     * 总线程数[8]，操作数[1000000]，耗时[298409]
     */
    @Test
    public void mongodbWriteTest() {
        var startTime = TimeUtils.currentTimeMillis();
        var threadNum = 8;
        var count = 100_0000;
        var list = new ArrayList<AtomicBoolean>();
        for (var i = 0; i < threadNum; i++) {
            var flag = new AtomicBoolean(false);
            list.add(flag);

            var mongoClient = MongoClients.create(MongoClientSettings.builder()
                    .applyToClusterSettings(builder ->
                            builder.hosts(Arrays.asList(new ServerAddress("localhost", 27017))))
                    .build());
            var mongodb = mongoClient.getDatabase("test");
            var collection = mongodb.getCollection("student");
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    for (var j = 0; j < count; j++) {
                        var key = j;
                        var value = j;
                        try {
                            // 插入一个文档
                            Document document = new Document("_id", key)
                                    .append("name", value);
                            collection.updateOne(eq("_id", key), new Document("$set", document));
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }

                    flag.set(true);
                }
            });
        }

        while (true) {
            var flag = list.stream().anyMatch(it -> it.get() == false);
            if (flag) {
                continue;
            } else {
                break;
            }
        }

        var endTime = TimeUtils.currentTimeMillis();
        System.out.println(StringUtils.format("总线程数[{}]，操作数[{}]，耗时[{}]"
                , threadNum, count, endTime - startTime));
    }


    /**
     * 总线程数[1]，操作数[10000]，耗时[4252]
     * 总线程数[1]，操作数[1000000]，耗时[407131]
     * 总线程数[4]，操作数[1000000]，耗时[226040]
     * 总线程数[8]，操作数[1000000]，耗时[297418]
     * 总线程数[16]，操作数[1000000]，耗时[436799]
     * 总线程数[32]，操作数[1000000]，耗时[716651]
     */
    @Test
    public void mongodbReadTest() {
        var startTime = TimeUtils.currentTimeMillis();
        var threadNum = 32;
        var count = 100_0000;
        var list = new ArrayList<AtomicBoolean>();
        for (var i = 0; i < threadNum; i++) {
            var flag = new AtomicBoolean(false);
            list.add(flag);

            var mongoClient = MongoClients.create(MongoClientSettings.builder()
                    .applyToClusterSettings(builder ->
                            builder.hosts(Arrays.asList(new ServerAddress("localhost", 27017))))
                    .build());
            var mongodb = mongoClient.getDatabase("test");
            var collection = mongodb.getCollection("student");
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    for (var j = 0; j < count; j++) {
                        try {
                            collection.find(eq("_id", j)).forEach(document -> {
                                if (document == null) {

                                }
                            });
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                    flag.set(true);
                }
            });
        }

        while (true) {
            var flag = list.stream().anyMatch(it -> it.get() == false);
            if (flag) {
                continue;
            } else {
                break;
            }
        }

        var endTime = TimeUtils.currentTimeMillis();
        System.out.println(StringUtils.format("总线程数[{}]，操作数[{}]，耗时[{}]"
                , threadNum, count, endTime - startTime));
    }

}
