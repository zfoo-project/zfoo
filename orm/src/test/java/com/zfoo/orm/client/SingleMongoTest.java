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

        System.out.println("All collections in current database:");
        for (String name : mongodb.listCollectionNames()) {
            System.out.println(name);
        }

        // Find and iterate all documents in collection 'student'
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
        // Group documents where 'likeNum' contains 9 by age and count
        var result = collection.aggregate(Arrays.asList(
                Aggregates.match(Filters.eq("name", "jay1")),
                Aggregates.group("$age", Accumulators.sum("count", 1)))
        );
        System.out.println(result.first());
    }


    @Test
    public void insertOneTest() {
        // Find and iterate all documents in collection 'student'
        MongoCollection<Document> collection = mongodb.getCollection("student");

        // Insert a document
        Document document = new Document("_id", 1)
                .append("name", "hello mongodb");
        collection.insertOne(document);

        collection.find().forEach(doc -> System.out.println(doc.toJson()));
    }

    @Test
    public void insertManyTest() {
        // Find and iterate all documents in collection 'student'
        MongoCollection<Document> collection = mongodb.getCollection("student");

        // Insert data
        var datas = new ArrayList<Document>();
        for (int i = 0; i < 10; i++) {
            var studentName = "jay" + i;
            var document = new Document("name", studentName).append("email", studentName + "@zfoo.com").append("age", i);
            datas.add(document);
        }
        collection.insertMany(datas);
        // Query: age >= 20 and age < 25
        FindIterable<Document> result = collection.find(new Document("age", new Document("$gte", 20).append("$lt", 25)));
        System.out.println(result.first());
    }

    @Test
    public void updateOneTest() {
        // Find and iterate all documents in collection 'student'
        MongoCollection<Document> collection = mongodb.getCollection("student");

        // Update one document; first arg is filter, second is update (only modifies first match)
        collection.updateOne(eq("age", 10), new Document("$set", new Document("name", "new hello mongodb")));

        collection.find().forEach(doc -> System.out.println(doc.toJson()));
    }


    @Test
    public void updateManyTest() {
        // Find and iterate all documents in collection 'student'
        MongoCollection<Document> collection = mongodb.getCollection("student");

        // Update one document
        collection.updateMany(eq("age", 10), new Document("$set", new Document("name", "new hello mongodb")));

        collection.find().forEach(doc -> System.out.println(doc.toJson()));
    }

    @Test
    public void deleteTest() {
        MongoCollection<Document> collection = mongodb.getCollection("student");

        // Delete one document
        collection.deleteOne(eq("_id", 1));

        collection.find().forEach(doc -> System.out.println(doc.toJson()));
    }



    // ******************************************************************************************************
    // Test updating one field vs multiple fields; results show negligible difference


    // time: ~42598ms
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
                // Insert a document
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
        System.out.println(StringUtils.format("elapsed[{}]ms", endTime - startTime));
    }

    // time: ~41971ms
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
                // Insert a document
                Document document = new Document("_id", key)
                        .append("name0", value);
                collection.updateOne(eq("_id", key), new Document("$set", document));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }


        var endTime = TimeUtils.currentTimeMillis();
        System.out.println(StringUtils.format("elapsed[{}]ms", endTime - startTime));
    }


    // ******************************************************************************************************
    // WiredTiger engine test
    // InMemory engine test
    private static final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 10);

    /**
     * InMemory engine benchmark results:
     * threads[1], ops[10000], elapsed[4212]ms
     * threads[1], ops[1000000], elapsed[394215]ms
     * threads[4], ops[1000000], elapsed[223355]ms
     * threads[8], ops[1000000], elapsed[297476]ms
     * threads[16], ops[1000000], elapsed[442782]ms
     * threads[32], ops[1000000], elapsed[713675]ms
     * <p>
     * <p>
     * WiredTiger engine benchmark results:
     * threads[1], ops[10000], elapsed[4170]ms
     * threads[1], ops[1000000], elapsed[400208]ms
     * threads[4], ops[1000000], elapsed[230471]ms
     * threads[8], ops[1000000], elapsed[298409]ms
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
                            // Insert a document
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
        System.out.println(StringUtils.format("threads[{}], ops[{}], elapsed[{}]ms"
                , threadNum, count, endTime - startTime));
    }


    /**
     * threads[1], ops[10000], elapsed[4252]ms
     * threads[1], ops[1000000], elapsed[407131]ms
     * threads[4], ops[1000000], elapsed[226040]ms
     * threads[8], ops[1000000], elapsed[297418]ms
     * threads[16], ops[1000000], elapsed[436799]ms
     * threads[32], ops[1000000], elapsed[716651]ms
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
        System.out.println(StringUtils.format("threads[{}], ops[{}], elapsed[{}]ms"
                , threadNum, count, endTime - startTime));
    }

}
