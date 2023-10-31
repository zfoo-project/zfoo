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


/**
 * @author godotg
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
            @SuppressWarnings("rawtypes")
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
                    .filter(it -> StringUtils.isNotBlank(it))
                    .map(it -> StringUtils.trim(it))
                    .map(it -> it.split(StringUtils.COLON_REGEX))
                    .map(it -> new ServerAddress(it[0], Integer.parseInt(it[1])))
                    .toList();
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
                        , StringUtils.format("A collection can have only one text index [{}]", JsonUtils.object2String(indexTextDefMap.keySet())));
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
                            // 注入的变量类型需要是泛型类
                            throw new RuntimeException(StringUtils.format("The variable [{}] is not of type generic", field.getName()));
                        }

                        Type[] types = ((ParameterizedType) type).getActualTypeArguments();
                        @SuppressWarnings("unchecked")
                        var entityClazz = (Class<? extends IEntity<?>>) types[1];
                        IEntityCache<?, ?> entityCaches = entityCachesMap.get(entityClazz);

                        if (entityCaches == null) {
                            // entity-package需要配置到可以扫描到EntityCache注解的包
                            throw new RunException("The EntityCache object does not exist, please check that [entity-package:{}] and [entityCaches:{}] are configured in the correct position", ormConfig.getEntityPackage(), entityClazz);
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
            throw new RunException("EntityCaches that do not have [] defined", clazz.getCanonicalName());
        }
        if (!usable) {
            // Orm没有使用EntityCacheAutowired，为了节省内存提前释放了它；只有使用EntityCacheAutowired注解的Entity才能被动态获取
            throw new RunException("Orm does not use [] EntityCacheAutowired annotation, which are released in advance to save memory", clazz.getCanonicalName());
        }
        @SuppressWarnings("unchecked")
        var entityCache = (IEntityCache<?, E>) entityCachesMap.get(clazz);
        return entityCache;
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
                    .toList();
            classSet.addAll(classes);
        } else {
            var classes = scanEntityCacheAnno();
            classSet.addAll(classes);
        }

        var cacheDefMap = new HashMap<Class<? extends IEntity<?>>, EntityDef>();
        for (var clazz : classSet) {
            @SuppressWarnings("unchecked")
            var entityClass = (Class<? extends IEntity<?>>) clazz;
            var cacheDef = parserEntityDef(entityClass);
            cacheDefMap.putIfAbsent(entityClass, cacheDef);
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
            throw new RuntimeException("No Entity was scanned:" + e);
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
        // Entity需要有@Cache注解的缓存策略
        AssertionUtils.isTrue(cacheStrategyOptional.isPresent(), "No Entity[{}] @Cache policy found[{}]", clazz.getSimpleName(), cache.value());

        var cacheStrategy = cacheStrategyOptional.get();
        var cacheSize = cacheStrategy.getSize();
        var expireMillisecond = cacheStrategy.getExpireMillisecond();

        var idField = ReflectionUtils.getFieldsByAnnoInPOJOClass(clazz, Id.class)[0];
        ReflectionUtils.makeAccessible(idField);

        var persister = entityCache.persister();
        var persisterStrategyOptional = persisterStrategies.stream().filter(it -> it.getStrategy().equals(persister.value())).findFirst();
        // 实体类Entity需要有持久化策略
        AssertionUtils.isTrue(persisterStrategyOptional.isPresent(), "Entity[{}] No persistence strategy found[{}]", clazz.getSimpleName(), persister.value());

        var persisterStrategy = persisterStrategyOptional.get();
        var indexDefMap = new HashMap<String, IndexDef>();
        var fields = ReflectionUtils.getFieldsByAnnoInPOJOClass(clazz, Index.class);
        for (var field : fields) {
            var indexAnnotation = field.getAnnotation(Index.class);

            if (indexAnnotation.ttlExpireAfterSeconds() > 0) {
                var fieldType = field.getGenericType();
                if (!(fieldType == Date.class || field.getGenericType().toString().equals("java.util.List<java.util.Date>"))) {
                    // MongoDB规定TTL类型必须是Date，List<Date>的其中一种类型
                    throw new IllegalArgumentException(StringUtils.format("MongoDB TTL type:[{}] must be Date or List<Date>", field.getName()));
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
        AssertionUtils.isTrue(IEntity.class.isAssignableFrom(clazz), "The entity:[{}] annotated by the [{}] annotation does not implement the interface [{}]"
                , com.zfoo.orm.anno.EntityCache.class.getName(), clazz.getCanonicalName(), IEntity.class.getCanonicalName());
        // 实体类Entity必须被注解EntityCache标注
        AssertionUtils.notNull(clazz.getAnnotation(com.zfoo.orm.anno.EntityCache.class), "The Entity[{}] must be annotated with the annotation [{}].", clazz.getCanonicalName(), com.zfoo.orm.anno.EntityCache.class.getName());

        // 校验entity格式
        checkEntity(clazz);

        // 校验id字段和id()方法的格式，一个Entity类只能有一个@Id注解
        var idFields = ReflectionUtils.getFieldsByAnnoInPOJOClass(clazz, Id.class);
        AssertionUtils.isTrue(ArrayUtils.isNotEmpty(idFields) && idFields.length == 1
                , "The Entity[{}] must have only one Id annotation (if it is indeed marked with an Id annotation, be careful not to use the Stored Id annotation)", clazz.getSimpleName());
        var idField = idFields[0];
        // idField必须用private修饰
        AssertionUtils.isTrue(Modifier.isPrivate(idField.getModifiers()), "The id of the Entity[{}] must be private", clazz.getSimpleName());

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
            throw new RunException("orm only supports int long float double String");
        }

        ReflectionUtils.makeAccessible(idField);
        ReflectionUtils.setField(idField, entityInstance, idFiledValue);
        var idMethodOptional = Arrays.stream(ReflectionUtils.getAllMethods(clazz)).filter(it -> it.getName().equalsIgnoreCase("id"))
                .filter(it -> it.getParameterCount() <= 0)
                .findFirst();
        // 实体类Entity必须重写id()方法
        AssertionUtils.isTrue(idMethodOptional.isPresent(), "The Entity[{}] must override the id() method", clazz.getSimpleName());
        var idMethod = idMethodOptional.get();
        ReflectionUtils.makeAccessible(idMethod);
        var idMethodReturnValue = ReflectionUtils.invokeMethod(entityInstance, idMethod);
        // 实体类Entity的id字段的返回值field和id方法的返回值method必须相等
        AssertionUtils.isTrue(idFiledValue.equals(idMethodReturnValue), "The return value id [field:{}] of the Entity[{}] and the return value id [method:{}] are not equal, please check whether the id() method is implemented correctly"
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
            // 实体类Entity的gvs和svs方法要实现都实现，不实现都不实现
            AssertionUtils.isTrue(gvsMethodOptional.isEmpty() && svsMethodOptional.isEmpty()
                    , "The gvs and svs methods of the Entity[{}] should be implemented together", clazz.getSimpleName());
            return;
        }

        var gvsMethod = gvsMethodOptional.get();
        var svsMethod = svsMethodOptional.get();
        var vsValue = RandomUtils.randomLong();
        ReflectionUtils.invokeMethod(entityInstance, svsMethod, vsValue);
        var gvsReturnValue = ReflectionUtils.invokeMethod(entityInstance, gvsMethod);
        // 实体类Entity的gvs方法和svs方法定义格式不正确
        AssertionUtils.isTrue(gvsReturnValue.equals(vsValue), "The gvs and svs methods of the Entity[{}] are not correctly", clazz.getSimpleName());
    }

    private void checkEntity(Class<?> clazz) {
        // 是否为一个简单的javabean，为了防止不同层对象混用造成潜在的并发问题，特别是网络层和po层混用
        ReflectionUtils.assertIsPojoClass(clazz);
        // 不能是泛型类
        AssertionUtils.isTrue(ArrayUtils.isEmpty(clazz.getTypeParameters()), "[class:{}] can't be a generic class", clazz.getCanonicalName());
        // 必须要有一个空的构造器
        ReflectionUtils.publicEmptyConstructor(clazz);

        // 不能使用Storage的Index注解
        var storageIndexes = ReflectionUtils.getFieldsByAnnoNameInPOJOClass(clazz, "com.zfoo.storage.anno.Index");
        if (ArrayUtils.isNotEmpty(storageIndexes)) {
            // 在Orm中只能使用Orm的Index注解，不能使用Storage的Index注解，为了避免不必要的误解和增强项目的健壮性，禁止这样使用
            throw new RunException("only the Index annotation of Orm can be used, not the Index annotation of Storage, and it is forbidden to use it in order to avoid unnecessary misunderstandings and enhance the robustness of the project");
        }

        var filedList = ReflectionUtils.notStaticAndTransientFields(clazz);

        for (var field : filedList) {
            // entity必须包含属性的get和set方法
            FieldUtils.fieldToGetMethod(clazz, field);
            FieldUtils.fieldToSetMethod(clazz, field);

            // 是一个基本类型变量
            var fieldType = field.getType();
            if (isBaseType(fieldType)) {
                // do nothing
            } else if (fieldType.isArray()) {
                // 是一个数组
                Class<?> arrayClazz = fieldType.getComponentType();
                // ORM的数组类型只支持byte[]
                AssertionUtils.isTrue(arrayClazz == byte.class, "The array type of ORM[class:{}] only supports byte[]", clazz.getCanonicalName());
            } else if (Set.class.isAssignableFrom(fieldType)) {
                // 必须是Set接口类型
                AssertionUtils.isTrue(fieldType.equals(Set.class), "[class:{}] type declaration is incorrect, and it must be of the Set interface type", clazz.getCanonicalName());

                var type = field.getGenericType();
                // field必须泛型类
                AssertionUtils.isTrue(type instanceof ParameterizedType, "[class:{}] type declaration is incorrect, not a generic class[field:{}]", clazz.getCanonicalName(), field.getName());

                var types = ((ParameterizedType) type).getActualTypeArguments();
                // 必须声明Set的泛型类
                AssertionUtils.isTrue(types.length == 1, "Set type declaration in [class:{}] is incorrect, and the generic class must be declared in [field:{}]", clazz.getCanonicalName(), field.getName());
                checkSubEntity(clazz, types[0]);
            } else if (List.class.isAssignableFrom(fieldType)) {
                // 是一个List
                AssertionUtils.isTrue(fieldType.equals(List.class), "[class:{}] type declaration is incorrect, and it must be of the List interface type", clazz.getCanonicalName());

                var type = field.getGenericType();
                // field必须泛型类
                AssertionUtils.isTrue(type instanceof ParameterizedType, "[class:{}] type declaration is incorrect, not a generic class[field:{}]", clazz.getCanonicalName(), field.getName());

                // 必须声明List的泛型类
                var types = ((ParameterizedType) type).getActualTypeArguments();
                AssertionUtils.isTrue(types.length == 1, "List type declaration in [class:{}] is incorrect, and the generic class must be declared in [field:{}]", clazz.getCanonicalName(), field.getName());

                checkSubEntity(clazz, types[0]);
            } else if (Map.class.isAssignableFrom(fieldType)) {
                // 必须是Map接口类型
                if (!fieldType.equals(Map.class)) {
                    throw new RunException("[class:{}] type declaration is incorrect, and it must be a Map interface type", clazz.getCanonicalName());
                }

                var type = field.getGenericType();

                if (!(type instanceof ParameterizedType)) {
                    throw new RunException("Map type declaration in [class:{}] is incorrect, and the generic class must be declared in [field:{}]", clazz.getCanonicalName(), field.getName());
                }

                var types = ((ParameterizedType) type).getActualTypeArguments();

                if (types.length != 2) {
                    throw new RunException("Map type declaration in [class:{}] is incorrect, and the generic class must be declared in [field:{}]", clazz.getCanonicalName(), field.getName());
                }

                var keyType = types[0];
                var valueType = types[1];

                if (!String.class.isAssignableFrom((Class<?>) keyType)) {
                    throw new RunException("[class:{}] type declaration is incorrect, and the key type of the Map must be the String type", clazz.getCanonicalName());
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
                    // ORM中Map的key必须是String类型
                    throw new RunException("The key of the map in the ORM must be of the String type");
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
                // ORM不支持多维数组或集合嵌套数组类型，仅支持一维数组
                throw new RunException("[type:{}] does not support multi-dimensional arrays or nested arrays, and only supports one-dimensional arrays", type);
            } else if (clazz.equals(List.class) || clazz.equals(Set.class) || clazz.equals(Map.class)) {
                // ORM不支持集合嵌套数组类型
                throw new RunException("ORMs do not support the combination of arrays and collections with the [type:{}] type", type);
            } else {
                checkEntity(clazz);
                return;
            }
        }
        throw new RunException("[type:{}] is incorrect", type);
    }

    private boolean isBaseType(Class<?> clazz) {
        return clazz.isPrimitive() || Number.class.isAssignableFrom(clazz) || String.class.isAssignableFrom(clazz);
    }
}
