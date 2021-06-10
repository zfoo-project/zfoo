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

package com.zfoo.orm.manager;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.zfoo.orm.model.anno.EntityCache;
import com.zfoo.orm.model.cache.EntityCaches;
import com.zfoo.orm.model.cache.IEntityCaches;
import com.zfoo.orm.model.config.OrmConfig;
import com.zfoo.orm.model.entity.IEntity;
import com.zfoo.orm.model.vo.EntityDef;
import com.zfoo.protocol.collection.CollectionUtils;
import com.zfoo.protocol.exception.RunException;
import com.zfoo.protocol.util.AssertionUtils;
import com.zfoo.protocol.util.JsonUtils;
import com.zfoo.protocol.util.StringUtils;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


/**
 * @author jaysunxiao
 * @version 3.0
 */
public class OrmManager implements IOrmManager {

    private static final Map<Class<? extends IEntity<?>>, IEntityCaches<?, ?>> entityCachesMap = new HashMap<>();

    private OrmConfig ormConfig;

    private MongoClient mongoClient;
    private MongoDatabase mongodbDatabase;

    private final Map<Class<? extends IEntity<?>>, String> collectionNameMap = new ConcurrentHashMap<>();

    public OrmConfig getOrmConfig() {
        return ormConfig;
    }

    public void setOrmConfig(OrmConfig ormConfig) {
        this.ormConfig = ormConfig;
    }

    @Override
    public void initBefore() {
        var entityDefMap = scanEntity();

        for (var entityDef : entityDefMap.values()) {
            var entityCaches = new EntityCaches(entityDef);
            entityCachesMap.put(entityDef.getClazz(), entityCaches);
        }

        CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));

        var mongoBuilder = MongoClientSettings
                .builder()
                .codecRegistry(pojoCodecRegistry);

        // 设置数据库连接地址，数据库账号密码
        var hostConfig = ormConfig.getHostConfig();
        if (StringUtils.isBlank(hostConfig.getUrl())) {
            throw new RunException("数据库MongoDB的连接地址url不能为空，请检查orm的配置");
        }

        mongoBuilder.applyToClusterSettings(builder -> builder.applyConnectionString(new ConnectionString(hostConfig.getUrl())));

        // 设置连接池的大小
        var maxConnection = Runtime.getRuntime().availableProcessors() * 2 + 1;
        mongoBuilder.applyToConnectionPoolSettings(builder -> builder.maxSize(maxConnection).minSize(1));

        mongoClient = MongoClients.create(mongoBuilder.build());
        mongodbDatabase = mongoClient.getDatabase(hostConfig.getDatabase());

        // 创建索引
        for (var entityDef : entityDefMap.values()) {
            var indexDefMap = entityDef.getIndexDefMap();
            if (CollectionUtils.isNotEmpty(indexDefMap)) {
                var collection = getCollection(entityDef.getClazz());
                for (var indexDef : indexDefMap.entrySet()) {
                    var fieldName = indexDef.getKey();
                    var index = indexDef.getValue();
                    var hasIndex = false;
                    for (var document : collection.listIndexes()) {
                        var keyDocument = (Document) document.get("key");
                        if (keyDocument.containsKey(fieldName)) {
                            hasIndex = true;
                        }
                    }
                    if (!hasIndex) {
                        var indexOptions = new IndexOptions();
                        indexOptions.unique(index.isUnique());
                        if (index.isAscending()) {
                            collection.createIndex(Indexes.ascending(fieldName), indexOptions);
                        } else {
                            collection.createIndex(Indexes.descending(fieldName), indexOptions);
                        }
                    }
                }
            }

            var indexTextDefMap = entityDef.getIndexTextDefMap();
            if (CollectionUtils.isNotEmpty(indexTextDefMap)) {
                AssertionUtils.isTrue(indexTextDefMap.size() == 1
                        , StringUtils.format("一个集合的text索引[{}]只能有一个", JsonUtils.object2String(indexTextDefMap.keySet())));
                var collection = getCollection(entityDef.getClazz());
                for (var indexTextDef : indexTextDefMap.entrySet()) {
                    var fieldName = indexTextDef.getKey();
                    var hasIndex = false;
                    for (var document : collection.listIndexes()) {
                        var keyDocument = (Document) document.get("key");
                        if (keyDocument.containsKey(fieldName)) {
                            hasIndex = true;
                        }
                    }
                    if (!hasIndex) {
                        collection.createIndex(Indexes.text(fieldName));
                    }
                }
            }
        }
    }

    private Map<Class<? extends IEntity<?>>, EntityDef> scanEntity() {
        var cacheDefMap = new HashMap<Class<? extends IEntity<?>>, EntityDef>();
        var entityPackage = ormConfig.getEntityPackage();
        var cacheStrategies = ormConfig.getCachesConfig().getCacheStrategies();
        var persisterStrategies = ormConfig.getPersistersConfig().getPersisterStrategies();

        var locationSet = getEntityLocation(entityPackage);
        for (var location : locationSet) {
            Class<?> entityClazz;
            try {
                entityClazz = Class.forName(location);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(StringUtils.format("无法获取实体类[{}]", location));
            }
            var cacheDef = EntityDef.valueOf(entityClazz, cacheStrategies, persisterStrategies);
            var previousCacheDef = cacheDefMap.putIfAbsent((Class<? extends IEntity<?>>) entityClazz, cacheDef);
            AssertionUtils.isNull(previousCacheDef, "缓存实体不能包含重复的[class:{}]", entityClazz.getSimpleName());
        }
        return cacheDefMap;
    }

    private Set<String> getEntityLocation(String scanLocation) {
        var prefixPattern = "classpath*:";
        var suffixPattern = "**/*.class";


        var resourcePatternResolver = new PathMatchingResourcePatternResolver();
        var metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);
        try {
            String packageSearchPath = prefixPattern + scanLocation.replace(StringUtils.PERIOD, StringUtils.SLASH) + StringUtils.SLASH + suffixPattern;
            Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);
            Set<String> result = new HashSet<>();
            String name = EntityCache.class.getName();
            for (Resource resource : resources) {
                if (resource.isReadable()) {
                    MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                    AnnotationMetadata annoMeta = metadataReader.getAnnotationMetadata();
                    if (annoMeta.hasAnnotation(name)) {
                        ClassMetadata clazzMeta = metadataReader.getClassMetadata();
                        result.add(clazzMeta.getClassName());
                    }
                }
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException("无法读取实体信息:" + e);
        }
    }

    @Override
    public void initAfter() {
        var unusableStorageClassList = entityCachesMap.entrySet().stream()
                .filter(it -> !it.getValue().isUsable())
                .map(it -> it.getKey())
                .collect(Collectors.toList());

        unusableStorageClassList.forEach(it -> {
            entityCachesMap.remove(it);
        });
    }

    @Override
    public <E extends IEntity<?>> IEntityCaches<?, E> getEntityCaches(Class<E> clazz) {
        return (IEntityCaches<?, E>) entityCachesMap.get(clazz);
    }

    @Override
    public Collection<IEntityCaches<?, ?>> getAllEntityCaches() {
        return Collections.unmodifiableCollection(entityCachesMap.values());
    }

    @Override
    public ClientSession getClientSession() {
        return mongoClient.startSession();
    }

    @Override
    public <E extends IEntity<?>> MongoCollection<E> getCollection(Class<E> entityClazz) {
        var collectionName = collectionNameMap.get(entityClazz);
        if (collectionName == null) {
            collectionName = StringUtils.substringBeforeLast(StringUtils.uncapitalize(entityClazz.getSimpleName()), "Entity");
            collectionNameMap.put(entityClazz, collectionName);
        }

        return mongodbDatabase.getCollection(collectionName, entityClazz);
    }


    @Override
    public MongoCollection<Document> getCollection(String collection) {
        return mongodbDatabase.getCollection(collection);
    }

}
