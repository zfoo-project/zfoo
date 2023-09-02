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

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.zfoo.orm.OrmContext;
import com.zfoo.orm.anno.*;
import com.zfoo.orm.cache.EntityCache;
import com.zfoo.orm.cache.IEntityCache;
import com.zfoo.orm.config.OrmConfig;
import com.zfoo.orm.model.EntityDef;
import com.zfoo.orm.model.IEntity;
import com.zfoo.orm.model.IndexDef;
import com.zfoo.orm.model.IndexTextDef;
import com.zfoo.protocol.collection.ArrayUtils;
import com.zfoo.protocol.collection.CollectionUtils;
import com.zfoo.protocol.exception.RunException;
import com.zfoo.protocol.util.*;
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
import org.springframework.stereotype.Component;

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * @author godotg
 * @version 3.0
 */
public class OrmManager implements IOrmManager {

    private OrmConfig ormConfig;

    private MongoClient mongoClient;
    private MongoDatabase mongodbDatabase;

    /**
     * 全部的Entity定义，key为对应的class，value为当前的Entity是否在当前项目中以缓存的形式使用
     */
    private final Map<Class<?>, Boolean> allEntityCachesUsableMap = new HashMap<>();

    private final Map<Class<? extends IEntity<?>>, IEntityCache<?, ?>> entityCachesMap = new HashMap<>();

    private final Map<Class<? extends IEntity<?>>, String> collectionNameMap = new ConcurrentHashMap<>();

    public OrmConfig getOrmConfig() {
        return ormConfig;
    }

    public void setOrmConfig(OrmConfig ormConfig) {
        this.ormConfig = ormConfig;
    }

    @Override
    public void initBefore() {
        var entityDefMap = entityClass();

        for (var entityDef : entityDefMap.values()) {
            var entityCaches = new EntityCache(entityDef);
            entityCachesMap.put(entityDef.getClazz(), entityCaches);
            allEntityCachesUsableMap.put(entityDef.getClazz(), false);
        }

        CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));

        var mongoBuilder = MongoClientSettings
                .builder()
                .codecRegistry(pojoCodecRegistry);

        // 设置数据库地址
        var hostConfig = ormConfig.getHost();
        if (CollectionUtils.isNotEmpty(hostConfig.getAddress())) {
            var hostList = hostConfig.getAddress().values()
                    .stream()
                    .map(it -> it.split(StringUtils.COMMA_REGEX))
                    .flatMap(it -> Arrays.stream(it))
                    .map(it -> StringUtils.trim(it))
                    .filter(it -> StringUtils.isNotBlank(it))
                    .map(it -> it.split(StringUtils.COLON_REGEX))
                    .map(it -> new ServerAddress(it[0], Integer.parseInt(it[1])))
                    .collect(Collectors.toList());
            mongoBuilder.applyToClusterSettings(builder -> builder.hosts(hostList));
        }

        // 设置数据库账号密码
        if (StringUtils.isNotBlank(hostConfig.getUser()) && StringUtils.isNotBlank(hostConfig.getPassword())) {
            mongoBuilder.credential(MongoCredential.createCredential(hostConfig.getUser(), "admin", hostConfig.getPassword().toCharArray()));
        }

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

                        if (index.getTtlExpireAfterSeconds() > 0) {
                            indexOptions.expireAfter(index.getTtlExpireAfterSeconds(), TimeUnit.SECONDS);
                        }

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

    @Override
    public void inject() {
        var applicationContext = OrmContext.getApplicationContext();
        var componentBeans = applicationContext.getBeansWithAnnotation(Component.class);
        for (var bean : componentBeans.values()) {
            ReflectionUtils.filterFieldsInClass(bean.getClass()
                    , field -> field.isAnnotationPresent(EntityCacheAutowired.class)
                    , field -> {
                        Type type = field.getGenericType();

                        if (!(type instanceof ParameterizedType)) {
                            throw new RuntimeException(StringUtils.format("变量[{}]的类型不是泛型类", field.getName()));
                        }

                        Type[] types = ((ParameterizedType) type).getActualTypeArguments();
                        Class<? extends IEntity<?>> entityClazz = (Class<? extends IEntity<?>>) types[1];
                        IEntityCache<?, ?> entityCaches = entityCachesMap.get(entityClazz);

                        if (entityCaches == null) {
                            throw new RunException("实体缓存对象不存在，请检查配置[entity-package:{}]和[entityCaches:{}]的位置是否正确", ormConfig.getEntityPackage(), entityClazz);
                        }

                        ReflectionUtils.makeAccessible(field);
                        ReflectionUtils.setField(field, bean, entityCaches);
                        allEntityCachesUsableMap.put(entityClazz, true);
                    });
        }
    }

    @Override
    public void initAfter() {
        allEntityCachesUsableMap.entrySet().stream()
                .filter(it -> !it.getValue())
                .map(it -> it.getKey())
                .forEach(it -> entityCachesMap.remove(it));
    }

    @Override
    public MongoClient mongoClient() {
        return mongoClient;
    }

    @Override
    public <E extends IEntity<?>> IEntityCache<?, E> getEntityCaches(Class<E> clazz) {
        var usable = allEntityCachesUsableMap.get(clazz);
        if (usable == null) {
            throw new RunException("没有定义[]的EntityCaches，无法获取", clazz.getCanonicalName());
        }
        if (!usable) {
            throw new RunException("Orm没有使用[]的EntityCaches，为了节省内存提前释放了它；只有使用EntityCachesInjection注解的Entity才能被动态获取", clazz.getCanonicalName());
        }
        return (IEntityCache<?, E>) entityCachesMap.get(clazz);
    }

    @Override
    public Collection<IEntityCache<?, ?>> getAllEntityCaches() {
        return Collections.unmodifiableCollection(entityCachesMap.values());
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

    private Map<Class<? extends IEntity<?>>, EntityDef> entityClass() {
        var classSet = new HashSet<>();
        // in graalvm environment, PathMatchingResourcePatternResolver/CachingMetadataReaderFactory unable to use, so get it directly in the spring container
        if (GraalVmUtils.isGraalVM()) {
            var applicationContext = OrmContext.getApplicationContext();
            var classes = applicationContext.getBeansWithAnnotation(GraalvmNativeEntityCache.class)
                    .values()
                    .stream()
                    .map(it -> it.getClass())
                    .collect(Collectors.toList());
            classSet.addAll(classes);
        } else {
            var classes = scanEntityCacheAnno();
            classSet.addAll(classes);
        }

        var cacheDefMap = new HashMap<Class<? extends IEntity<?>>, EntityDef>();
        for (var clazz : classSet) {
            var cacheDef = parserEntityDef((Class<? extends IEntity<?>>) clazz);
            cacheDefMap.putIfAbsent((Class<? extends IEntity<?>>) clazz, cacheDef);
        }
        return cacheDefMap;
    }

    private Set<Class<?>> scanEntityCacheAnno() {
        var scanLocation = ormConfig.getEntityPackage();
        var prefixPattern = "classpath*:";
        var suffixPattern = "**/*.class";


        var resourcePatternResolver = new PathMatchingResourcePatternResolver();
        var metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);
        try {
            String packageSearchPath = prefixPattern + scanLocation.replace(StringUtils.PERIOD, StringUtils.SLASH) + StringUtils.SLASH + suffixPattern;
            Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);
            var result = new HashSet<Class<?>>();
            String name = com.zfoo.orm.anno.EntityCache.class.getName();
            for (Resource resource : resources) {
                if (resource.isReadable()) {
                    MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                    AnnotationMetadata annoMeta = metadataReader.getAnnotationMetadata();
                    if (annoMeta.hasAnnotation(name)) {
                        ClassMetadata clazzMeta = metadataReader.getClassMetadata();
                        result.add(Class.forName(clazzMeta.getClassName()));
                    }
                }
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("无法读取实体信息:" + e);
        }
    }


    public EntityDef parserEntityDef(Class<? extends IEntity<?>> clazz) {
        if (!GraalVmUtils.isGraalVM()) {
            analyze(clazz);
        }

        var cacheStrategies = ormConfig.getCaches();
        var persisterStrategies = ormConfig.getPersisters();

        var entityCache = clazz.getAnnotation(com.zfoo.orm.anno.EntityCache.class);
        var cache = entityCache.cache();
        var cacheStrategyOptional = cacheStrategies.stream().filter(it -> it.getStrategy().equals(cache.value())).findFirst();
        AssertionUtils.isTrue(cacheStrategyOptional.isPresent(), "实体类Entity[{}]没有找到缓存策略[{}]", clazz.getSimpleName(), cache.value());

        var cacheStrategy = cacheStrategyOptional.get();
        var cacheSize = cacheStrategy.getSize();
        var expireMillisecond = cacheStrategy.getExpireMillisecond();

        var idField = ReflectionUtils.getFieldsByAnnoInPOJOClass(clazz, Id.class)[0];
        ReflectionUtils.makeAccessible(idField);

        var persister = entityCache.persister();
        var persisterStrategyOptional = persisterStrategies.stream().filter(it -> it.getStrategy().equals(persister.value())).findFirst();
        AssertionUtils.isTrue(persisterStrategyOptional.isPresent(), "实体类Entity[{}]没有找到持久化策略[{}]", clazz.getSimpleName(), persister.value());

        var persisterStrategy = persisterStrategyOptional.get();
        var indexDefMap = new HashMap<String, IndexDef>();
        var fields = ReflectionUtils.getFieldsByAnnoInPOJOClass(clazz, Index.class);
        for (var field : fields) {
            var indexAnnotation = field.getAnnotation(Index.class);

            if (indexAnnotation.ttlExpireAfterSeconds() > 0) {
                var fieldType = field.getGenericType();
                if (!(fieldType == Date.class || field.getGenericType().toString().equals("java.util.List<java.util.Date>"))) {
                    throw new IllegalArgumentException(StringUtils.format("MongoDB规定TTL类型[{}]必须是Date，List<Date>的其中一种类型", field.getName()));
                }
            }

            IndexDef indexDef = new IndexDef(field, indexAnnotation.ascending(), indexAnnotation.unique(), indexAnnotation.ttlExpireAfterSeconds());
            indexDefMap.put(field.getName(), indexDef);
        }

        var indexTextDefMap = new HashMap<String, IndexTextDef>();
        fields = ReflectionUtils.getFieldsByAnnoInPOJOClass(clazz, IndexText.class);
        for (var field : fields) {
            IndexTextDef indexTextDef = new IndexTextDef(field, field.getAnnotation(IndexText.class));
            indexTextDefMap.put(field.getName(), indexTextDef);
        }

        return EntityDef.valueOf(idField, clazz, cacheSize, expireMillisecond, persisterStrategy, indexDefMap, indexTextDefMap);
    }

    private void analyze(Class<?> clazz) {
        // 是否实现了IEntity接口
        AssertionUtils.isTrue(IEntity.class.isAssignableFrom(clazz), "被[{}]注解标注的实体类[{}]没有实现接口[{}]", com.zfoo.orm.anno.EntityCache.class.getName(), clazz.getCanonicalName(), IEntity.class.getCanonicalName());
        // 实体类Entity必须被注解EntityCache标注
        AssertionUtils.notNull(clazz.getAnnotation(com.zfoo.orm.anno.EntityCache.class), "实体类Entity[{}]必须被注解[{}]标注", clazz.getCanonicalName(), com.zfoo.orm.anno.EntityCache.class.getName());

        // 校验entity格式
        var entitySubClassMap = new HashMap<Class<?>, Set<Class<?>>>();
        checkEntity(clazz);
        // 对象循环引用检测
        for (var entry : entitySubClassMap.entrySet()) {
            var subClass = entry.getKey();
            var subClassSet = entry.getValue();
            if (subClassSet.contains(subClass)) {
                throw new RunException("ORM[class:{}]在第一层包含循环引用对象[class:{}]", clazz.getSimpleName(), subClass.getSimpleName());
            }

            var queue = new LinkedList<>(subClassSet);
            var allSubClassSet = new HashSet<>(queue);
            while (!queue.isEmpty()) {
                var firstSubClass = queue.poll();
                if (entitySubClassMap.containsKey(firstSubClass)) {
                    for (var elementClass : entitySubClassMap.get(firstSubClass)) {
                        if (subClass.equals(elementClass)) {
                            throw new RunException("ORM[class:{}]在下层对象[class:{}]包含循环引用对象[class:{}]", clazz.getSimpleName(), elementClass.getSimpleName(), elementClass.getSimpleName());
                        }

                        if (!allSubClassSet.contains(elementClass)) {
                            allSubClassSet.add(elementClass);
                            queue.offer(elementClass);
                        }
                    }
                }
            }
        }

        // 校验id字段和id()方法的格式
        var idFields = ReflectionUtils.getFieldsByAnnoInPOJOClass(clazz, Id.class);
        AssertionUtils.isTrue(ArrayUtils.isNotEmpty(idFields) && idFields.length == 1
                , "实体类Entity[{}]必须只有且仅有一个Id注解（如果确实已经被Id注解标注，注意不要使用Storage的Id注解）", clazz.getSimpleName());
        var idField = idFields[0];
        // idField必须用private修饰
        AssertionUtils.isTrue(Modifier.isPrivate(idField.getModifiers()), "实体类Entity[{}]的id必须是private私有的", clazz.getSimpleName());

        // 随机给id字段赋值，然后调用id()方法，看看两者的返回值是不是一样的，避免出错
        var entityInstance = ReflectionUtils.newInstance(clazz);
        var idFieldType = idField.getType();
        Object idFiledValue = null;
        if (idFieldType.equals(int.class) || idFieldType.equals(Integer.class)) {
            idFiledValue = RandomUtils.randomInt();
        } else if (idFieldType.equals(long.class) || idFieldType.equals(Long.class)) {
            idFiledValue = RandomUtils.randomLong();
        } else if (idFieldType.equals(float.class) || idFieldType.equals(Float.class)) {
            idFiledValue = (float) RandomUtils.randomDouble();
        } else if (idFieldType.equals(double.class) || idFieldType.equals(Double.class)) {
            idFiledValue = RandomUtils.randomDouble();
        } else if (idFieldType.equals(String.class)) {
            idFiledValue = RandomUtils.randomString(10);
        } else {
            throw new RunException("orm现在仅支持int long float double String");
        }

        ReflectionUtils.makeAccessible(idField);
        ReflectionUtils.setField(idField, entityInstance, idFiledValue);
        var idMethodOptional = Arrays.stream(ReflectionUtils.getAllMethods(clazz)).filter(it -> it.getName().equalsIgnoreCase("id"))
                .filter(it -> it.getParameterCount() <= 0)
                .findFirst();
        AssertionUtils.isTrue(idMethodOptional.isPresent(), "实体类Entity[{}]必须重写id()方法", clazz.getSimpleName());
        var idMethod = idMethodOptional.get();
        ReflectionUtils.makeAccessible(idMethod);
        var idMethodReturnValue = ReflectionUtils.invokeMethod(entityInstance, idMethod);
        AssertionUtils.isTrue(idFiledValue.equals(idMethodReturnValue), "实体类Entity[{}]的id字段的返回值[field:{}]和id方法的返回值[method:{}]不相等，请检查id()方法实现是否正确"
                , clazz.getSimpleName(), idFiledValue, idMethodReturnValue);

        // 校验gvs()方法和svs()方法的格式
        var gvsMethodOptional = Arrays.stream(ReflectionUtils.getAllMethods(clazz))
                .filter(it -> it.getName().equals("gvs"))
                .filter(it -> it.getParameterCount() <= 0)
                .findFirst();

        var svsMethodOptional = Arrays.stream(ReflectionUtils.getAllMethods(clazz))
                .filter(it -> it.getName().equals("svs"))
                .filter(it -> it.getParameterCount() == 1)
                .filter(it -> it.getParameterTypes()[0].equals(long.class))
                .findFirst();
        // gvs和svs要实现都实现，不实现都不实现
        if (gvsMethodOptional.isEmpty() || svsMethodOptional.isEmpty()) {
            AssertionUtils.isTrue(gvsMethodOptional.isEmpty() && svsMethodOptional.isEmpty(), "实体类Entity[{}]的gvs和svs方法要实现都实现，不实现都不实现", clazz.getSimpleName());
            return;
        }

        var gvsMethod = gvsMethodOptional.get();
        var svsMethod = svsMethodOptional.get();
        var vsValue = RandomUtils.randomLong();
        ReflectionUtils.invokeMethod(entityInstance, svsMethod, vsValue);
        var gvsReturnValue = ReflectionUtils.invokeMethod(entityInstance, gvsMethod);
        AssertionUtils.isTrue(gvsReturnValue.equals(vsValue), "实体类Entity[{}]的gvs方法和svs方法定义格式不正确", clazz.getSimpleName());
    }

    private void checkEntity(Class<?> clazz) {
        // 是否为一个简单的javabean，为了防止不同层对象混用造成潜在的并发问题，特别是网络层和po层混用
        ReflectionUtils.assertIsPojoClass(clazz);
        // 不能是泛型类
        AssertionUtils.isTrue(ArrayUtils.isEmpty(clazz.getTypeParameters()), "[class:{}]不能是泛型类", clazz.getCanonicalName());
        // 必须要有一个空的构造器
        ReflectionUtils.publicEmptyConstructor(clazz);

        // 不能使用Storage的Index注解
        var storageIndexes = ReflectionUtils.getFieldsByAnnoNameInPOJOClass(clazz, "com.zfoo.storage.anno.Index");
        if (ArrayUtils.isNotEmpty(storageIndexes)) {
            throw new RunException("在Orm中只能使用Orm的Index注解，不能使用Storage的Index注解，为了避免不必要的误解和增强项目的健壮性，禁止这样使用");
        }

        var filedList = ReflectionUtils.notStaticAndTransientFields(clazz);

        for (var field : filedList) {
            // entity必须包含属性的get和set方法
            ReflectionUtils.fieldToGetMethod(clazz, field);
            ReflectionUtils.fieldToSetMethod(clazz, field);

            // 是一个基本类型变量
            var fieldType = field.getType();
            if (isBaseType(fieldType)) {
                // do nothing
            } else if (fieldType.isArray()) {
                // 是一个数组
                Class<?> arrayClazz = fieldType.getComponentType();
                checkSubEntity(clazz, arrayClazz);
            } else if (Set.class.isAssignableFrom(fieldType)) {
                AssertionUtils.isTrue(fieldType.equals(Set.class), "ORM[class:{}]类型声明不正确，必须是Set接口类型", clazz.getCanonicalName());

                var type = field.getGenericType();
                AssertionUtils.isTrue(type instanceof ParameterizedType, "ORM[class:{}]类型声明不正确，不是泛型类[field:{}]", clazz.getCanonicalName(), field.getName());

                var types = ((ParameterizedType) type).getActualTypeArguments();
                AssertionUtils.isTrue(types.length == 1, "ORM[class:{}]中Set类型声明不正确，[field:{}]必须声明泛型类", clazz.getCanonicalName(), field.getName());

                checkSubEntity(clazz, types[0]);
            } else if (List.class.isAssignableFrom(fieldType)) {
                // 是一个List
                AssertionUtils.isTrue(fieldType.equals(List.class), "ORM[class:{}]类型声明不正确，必须是List接口类型", clazz.getCanonicalName());

                var type = field.getGenericType();
                AssertionUtils.isTrue(type instanceof ParameterizedType, "ORM[class:{}]类型声明不正确，不是泛型类[field:{}]", clazz.getCanonicalName(), field.getName());

                var types = ((ParameterizedType) type).getActualTypeArguments();
                AssertionUtils.isTrue(types.length == 1, "ORM[class:{}]中List类型声明不正确，[field:{}]必须声明泛型类", clazz.getCanonicalName(), field.getName());

                checkSubEntity(clazz, types[0]);
            } else if (Map.class.isAssignableFrom(fieldType)) {
                if (!fieldType.equals(Map.class)) {
                    throw new RunException("ORM[class:{}]类型声明不正确，必须是Map接口类型", clazz.getCanonicalName());
                }

                var type = field.getGenericType();

                if (!(type instanceof ParameterizedType)) {
                    throw new RunException("ORM[class:{}]中数组类型声明不正确，[field:{}]不是泛型类", clazz.getCanonicalName(), field.getName());
                }

                var types = ((ParameterizedType) type).getActualTypeArguments();

                if (types.length != 2) {
                    throw new RunException("ORM[class:{}]中数组类型声明不正确，[field:{}]必须声明泛型类", clazz.getCanonicalName(), field.getName());
                }

                var keyType = types[0];
                var valueType = types[1];

                if (!String.class.isAssignableFrom((Class<?>) keyType)) {
                    throw new RunException("ORM[class:{}]类型声明不正确，Map的key类型必须为String类型", clazz.getCanonicalName());
                }

                checkSubEntity(clazz, valueType);
            } else {
                checkEntity(fieldType);
            }
        }
    }


    private void checkSubEntity(Class<?> currentEntityClass, Type type) {
        if (type instanceof ParameterizedType) {
            // 泛型类
            Class<?> clazz = (Class<?>) ((ParameterizedType) type).getRawType();
            if (Set.class.equals(clazz)) {
                // Set<Set<String>>
                checkSubEntity(currentEntityClass, ((ParameterizedType) type).getActualTypeArguments()[0]);
                return;
            } else if (List.class.equals(clazz)) {
                // List<List<String>>
                checkSubEntity(currentEntityClass, ((ParameterizedType) type).getActualTypeArguments()[0]);
                return;
            } else if (Map.class.equals(clazz)) {
                // Map<List<String>, List<String>>
                var types = ((ParameterizedType) type).getActualTypeArguments();
                var keyType = types[0];
                var valueType = types[1];
                if (!String.class.isAssignableFrom((Class<?>) keyType)) {
                    throw new RunException("ORM中Map的key必须是String类型");
                }
                checkSubEntity(currentEntityClass, valueType);
                return;
            }
        } else if (type instanceof Class) {
            Class<?> clazz = ((Class<?>) type);
            if (isBaseType(clazz)) {
                // do nothing
                return;
            } else if (clazz.getComponentType() != null) {
                // 是一个二维以上数组
                throw new RunException("ORM不支持多维数组或集合嵌套数组[type:{}]类型，仅支持一维数组", type);
            } else if (clazz.equals(List.class) || clazz.equals(Set.class) || clazz.equals(Map.class)) {
                throw new RunException("ORM不支持数组和集合联合使用[type:{}]类型", type);
            } else {
                checkEntity(clazz);
                return;
            }
        }
        throw new RunException("[type:{}]类型不正确", type);
    }

    private boolean isBaseType(Class<?> clazz) {
        return clazz.isPrimitive() || Number.class.isAssignableFrom(clazz) || String.class.isAssignableFrom(clazz);
    }
}
