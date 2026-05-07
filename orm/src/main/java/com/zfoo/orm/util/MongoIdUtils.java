/*
 * Copyright (C) 2020 The zfoo Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.zfoo.orm.util;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.Updates;
import com.zfoo.orm.OrmContext;
import com.zfoo.protocol.collection.CollectionUtils;
import com.zfoo.protocol.util.AssertionUtils;
import com.zfoo.protocol.util.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.function.Consumer;


/**
 * @author godotg
 */
public abstract class MongoIdUtils {

    private static final Logger logger = LoggerFactory.getLogger(MongoIdUtils.class);

    private static final long INIT_ID = 1L;

    private static final String COLLECTION_NAME = "uuid";

    private static final String COUNT = "count";


    /**
     * Distributed unique ID generator backed by MongoDB auto-increment; guarantees atomicity and consistency.
     * <p>
     * Thread-safe: independent across threads.
     * <p>
     * Process-safe: unique IDs guaranteed across different application processes.
     *
     * @param collectionName collection name for storage
     * @param documentName   document ID name
     * @return the incremented unique ID
     */
    public static long getIncrementIdFromMongo(String collectionName, String documentName) {
        var collection = OrmContext.getOrmManager().getCollection(collectionName);

        try {
            var document = collection.findOneAndUpdate(Filters.eq("_id", documentName), new Document("$inc", new Document(COUNT, 1L)));
            if (document != null) {
                return document.getLong("count") + 1;
            }
        } catch (Throwable t) {
            logger.error("getIncrementIdFromMongo collection:[{}] document:[{}] default error", collectionName, documentName, t);
        }

        var query = Filters.eq("_id", documentName);
        var inc = new Document("$inc", new Document(COUNT, 1L));
        var setOnInsert = new Document("$setOnInsert", new Document("_id", documentName));
        // Retry once on error; under high concurrency MongoDB may report "duplicate key error!" once
        for (var i = 0; i < 3; i++) {
            try {
                var document = collection.findOneAndUpdate(query, Updates.combine(inc, setOnInsert), new FindOneAndUpdateOptions().upsert(true));
                return null == document ? INIT_ID : document.getLong(COUNT) + 1;
            } catch (Throwable t) {
                logger.error("getIncrementIdFromMongo collection:[{}] document:[{}] retry error! ", collectionName, documentName, t);
            }
        }

        throw new RuntimeException("getIncrementIdFromMongo error!");
    }

    public static long getIncrementIdFromMongoDefault(String documentName) {
        return getIncrementIdFromMongo(COLLECTION_NAME, documentName);
    }

    public static long getIncrementIdFromMongoDefault(Class<?> clazz) {
        return getIncrementIdFromMongo(COLLECTION_NAME, StringUtils.uncapitalize(clazz.getSimpleName()));
    }


    // ----------------------------------------------------------------------------------------------------

    /**
     * Reset the document counter to 0
     *
     * @param documentName document ID name
     */
    public static void resetIncrementIdFromMongoDefault(String documentName) {
        setIncrementIdFromMongo(COLLECTION_NAME, documentName, 0L);
    }

    public static void setIncrementIdFromMongo(Class<?> clazz, long value) {
        setIncrementIdFromMongo(COLLECTION_NAME, StringUtils.uncapitalize(clazz.getSimpleName()), value);
    }

    public static void setIncrementIdFromMongo(String documentName, long value) {
        setIncrementIdFromMongo(COLLECTION_NAME, documentName, value);
    }

    public static void setIncrementIdFromMongo(String collectionName, String documentName, long value) {
        var collection = OrmContext.getOrmManager().getCollection(collectionName);
        var document = collection.findOneAndUpdate(Filters.eq("_id", documentName), new Document("$set", new Document(COUNT, value)));

        if (document == null) {
            var result = collection.insertOne(new Document("_id", documentName).append(COUNT, value));
            AssertionUtils.notNull(result.getInsertedId());
        }
    }


    // ----------------------------------------------------------------------------------------------------

    /**
     * Get the current maximum ID
     *
     * @param collectionName collection name for storage
     * @param documentName   document ID name
     * @return the incremented unique ID
     */
    public static long getMaxIdFromMongo(String collectionName, String documentName) {
        var collection = OrmContext.getOrmManager().getCollection(collectionName);

        var list = new ArrayList<Document>();
        collection.find(Filters.eq("_id", documentName)).forEach(new Consumer<Document>() {
            @Override
            public void accept(Document document) {
                list.add(document);
            }
        });

        if (CollectionUtils.isEmpty(list)) {
            return 0;
        }

        return list.get(0).getLong("count");
    }

    public static long getMaxIdFromMongoDefault(String documentName) {
        return getMaxIdFromMongo(COLLECTION_NAME, documentName);
    }

    public static long getMaxIdFromMongoDefault(Class<?> clazz) {
        return getMaxIdFromMongo(COLLECTION_NAME, StringUtils.uncapitalize(clazz.getSimpleName()));
    }

}
