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
import com.zfoo.orm.codec.MapCodecProvider;
import com.zfoo.orm.config.CacheStrategy;
import com.zfoo.orm.config.OrmConfig;
import com.zfoo.orm.config.PersisterStrategy;
import com.zfoo.orm.convention.AnnotationConvention;
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
import org.bson.codecs.pojo.Conventions;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
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


/**
 * @author godotg
 */
public class OrmManager implements IOrmManager {

    private static final Logger logger = LoggerFactory.getLogger(OrmManager.class);

    private OrmConfig ormConfig;

    private MongoClient mongoClient;
    private MongoDatabase mongodbDatabase;

    /**
     * All Entity definitions; key=class, value=whether the Entity is used with caching in the current project
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

    @SuppressWarnings("unchecked")
    @Override
    public void initBefore() {
        var entityDefMap = entityClass();

        for (var entry : entityDefMap.entrySet()) {
            var entityClass = entry.getKey();
            var entityDef = entry.getValue();
            @SuppressWarnings("rawtypes")
            var entityCaches = new EntityCache(entityClass, entityDef);
            entityCachesMap.put(entityClass, entityCaches);
            allEntityCachesUsableMap.put(entityClass, false);
        }

        var pojoCodecProvider = PojoCodecProvider.builder()
                .automatic(true)
                .conventions(List.of(Conventions.ANNOTATION_CONVENTION, AnnotationConvention.INSTANCE))
                .register(new MapCodecProvider())
                .build();
        var codecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(pojoCodecProvider));

        var mongoBuilder = MongoClientSettings
                .builder()
                .codecRegistry(codecRegistry);

        // Set database host/address
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

        // Set database credentials
        if (StringUtils.isNotBlank(hostConfig.getUser()) && StringUtils.isNotBlank(hostConfig.getPassword())) {
            var authSource = StringUtils.isEmpty(hostConfig.getAuthSource())
                    ? "admin"
                    : hostConfig.getAuthSource();
            mongoBuilder.credential(MongoCredential.createCredential(hostConfig.getUser(), authSource, hostConfig.getPassword().toCharArray()));
        }

        // Set connection pool size
        var maxConnection = Runtime.getRuntime().availableProcessors() * 2 + 1;
        mongoBuilder.applyToConnectionPoolSettings(builder -> builder.maxSize(maxConnection).minSize(1));

        mongoClient = MongoClients.create(mongoBuilder.build());
        mongodbDatabase = mongoClient.getDatabase(hostConfig.getDatabase());

        // Create indexes
        for (var entry : entityDefMap.entrySet()) {
            var entityClass = entry.getKey();
            var entityDef = entry.getValue();
            var indexDefMap = entityDef.getIndexDefMap();
            if (CollectionUtils.isNotEmpty(indexDefMap)) {
                var collection = mongodbDatabase.getCollection(collectionName(entityClass), entityClass);
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
                        var isUnique = index.isUnique();
                        var isAscending = index.isAscending();

                        var indexOptions = new IndexOptions();
                        indexOptions.unique(isUnique);
                        String indexName = isAscending
                                ? collection.createIndex(Indexes.ascending(fieldName), indexOptions)
                                : collection.createIndex(Indexes.descending(fieldName), indexOptions);
                        logger.info("Database:[{}] collection:[{}] auto created field index:[{}][{}] ascending:[{}] unique:[{}]"
                                , hostConfig.getDatabase(), entityClass.getSimpleName(), fieldName, indexName, isAscending, isUnique);
                    }
                }
            }

            var indexTextDefMap = entityDef.getIndexTextDefMap();
            if (CollectionUtils.isNotEmpty(indexTextDefMap)) {
                AssertionUtils.isTrue(indexTextDefMap.size() == 1
                        , StringUtils.format("A collection can have only one text index [{}]", JsonUtils.object2String(indexTextDefMap.keySet())));
                var collection = mongodbDatabase.getCollection(collectionName(entityClass), entityClass);
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
                        String indexName = collection.createIndex(Indexes.text(fieldName));
                        logger.info("Database:[{}] collection:[{}] auto created text index:[{}][{}]", hostConfig.getDatabase(), entityClass.getSimpleName(), fieldName, indexName);
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
            // Prevent direct assignment from being ignored when the class is CGLIB-proxied
            var targetBean = Objects.requireNonNullElse(AopProxyUtils.getSingletonTarget(bean), bean);
            ReflectionUtils.filterFieldsInClass(targetBean.getClass()
                    , field -> field.isAnnotationPresent(EntityCacheAutowired.class)
                    , field -> {
                        Type type = field.getGenericType();

                        if (!(type instanceof ParameterizedType)) {
                            // Injected field must be a generic type
                            throw new RuntimeException(StringUtils.format("The variable [{}] is not of type generic", field.getName()));
                        }

                        Type[] types = ((ParameterizedType) type).getActualTypeArguments();
                        @SuppressWarnings("unchecked")
                        var entityClazz = (Class<? extends IEntity<?>>) types[1];
                        IEntityCache<?, ?> entityCaches = entityCachesMap.get(entityClazz);

                        if (entityCaches == null) {
                            // entity-package must be configured to scan packages where @EntityCache annotations are used
                            throw new RunException("The EntityCache object does not exist, please check that [entity-package:{}] and [entityCaches:{}] are configured in the correct position", ormConfig.getEntityPackage(), entityClazz);
                        }

                        ReflectionUtils.makeAccessible(field);
                        ReflectionUtils.setField(field, targetBean, entityCaches);
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
    public <PK extends Comparable<PK>, E extends IEntity<PK>> IEntityCache<PK, E> getEntityCaches(Class<E> clazz) {
        var usable = allEntityCachesUsableMap.get(clazz);
        if (usable == null) {
            throw new RunException("EntityCaches that do not have [] defined", clazz.getCanonicalName());
        }
        if (!usable) {
            // Orm does not use EntityCacheAutowired here to save memory; only entities annotated with @EntityCacheAutowired can be retrieved dynamically
            throw new RunException("Orm does not use [] EntityCacheAutowired annotation, which are released in advance to save memory", clazz.getCanonicalName());
        }
        @SuppressWarnings("unchecked")
        var entityCache = (IEntityCache<PK, E>) entityCachesMap.get(clazz);
        return entityCache;
    }

    @Override
    public Collection<IEntityCache<?, ?>> getAllEntityCaches() {
        return Collections.unmodifiableCollection(entityCachesMap.values());
    }

    private String collectionName(Class<? extends IEntity<?>> entityClazz) {
        var collectionName = collectionNameMap.get(entityClazz);
        if (collectionName == null) {
            collectionName = StringUtils.substringBeforeLast(StringUtils.uncapitalize(entityClazz.getSimpleName()), "Entity");
            collectionNameMap.put(entityClazz, collectionName);
        }
        return collectionName;
    }

    @Override
    public <PK extends Comparable<PK>, E extends IEntity<PK>> MongoCollection<E> getCollection(Class<E> entityClazz) {
        return mongodbDatabase.getCollection(collectionName(entityClazz), entityClazz);
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

        var unsafeList = cacheDefMap.entrySet()
                .stream()
                .filter(it -> !it.getValue().isThreadSafe())
                .filter(it -> it.getKey().isAnnotationPresent(com.zfoo.orm.anno.EntityCache.class))
                .map(it -> it.getKey().getSimpleName())
                .toList();
        if (CollectionUtils.isNotEmpty(unsafeList)) {
            logger.info("In order to improve performance, highly recommend using CopyOnWriteArrayList or CopyOnWriteHashMap in @EntityCache:[{}]"
                    , StringUtils.joinWith(", ", unsafeList.toArray()));
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
            String annoEntityCacheName = com.zfoo.orm.anno.EntityCache.class.getName();
            String interfaceEntityName = IEntity.class.getName();
            for (Resource resource : resources) {
                if (resource.isReadable()) {
                    MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                    AnnotationMetadata annoMeta = metadataReader.getAnnotationMetadata();
                    ClassMetadata classMeta = metadataReader.getClassMetadata();

                    if (annoMeta.hasAnnotation(annoEntityCacheName)) {
                        result.add(Class.forName(classMeta.getClassName()));
                    }

                    List<String> interfaces = ArrayUtils.toList(classMeta.getInterfaceNames());
                    if (interfaces.stream().anyMatch(it -> it.equals(interfaceEntityName))) {
                        result.add(Class.forName(classMeta.getClassName()));
                    }
                }
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("No Entity was scanned:" + e);
        }
    }


    public EntityDef parserEntityDef(Class<? extends IEntity<?>> clazz) {
        var hasUnsafeCollection = hasUnsafeCollection(clazz);

        // Validate the id field format
        checkIdField(clazz);
        // Validate the version field format
        checkVersionField(clazz);

        var cacheStrategies = ormConfig.getCaches();
        var persisterStrategies = ormConfig.getPersisters();

        var cacheStrategy = CacheStrategy.DEFAULT;
        var persisterStrategy = PersisterStrategy.DEFAULT;
        // If the Entity is annotated with @EntityCache, use the specified cache/persist strategy; otherwise use the default
        if (clazz.isAnnotationPresent(com.zfoo.orm.anno.EntityCache.class)) {
            var entityCache = clazz.getAnnotation(com.zfoo.orm.anno.EntityCache.class);
            var cache = entityCache.cache().value();
            var cacheStrategyOptional = cacheStrategies.stream().filter(it -> it.getStrategy().equals(cache)).findFirst();
            // Entity must have a @Cache annotation for cache strategy
            AssertionUtils.isTrue(cacheStrategyOptional.isPresent(), "No Entity[{}] @Cache policy found[{}]", clazz.getSimpleName(), cache);
            cacheStrategy = cacheStrategyOptional.get();

            var persister = entityCache.persister().value();
            var persisterStrategyOptional = persisterStrategies.stream().filter(it -> it.getStrategy().equals(persister)).findFirst();
            // Entity must have a persistence strategy defined
            AssertionUtils.isTrue(persisterStrategyOptional.isPresent(), "Entity[{}] No persistence strategy found[{}]", clazz.getSimpleName(), persister);
            persisterStrategy = persisterStrategyOptional.get();
        } else {
            cacheStrategy = cacheStrategies.stream()
                    .filter(it -> it.getStrategy().equals(CacheStrategy.DEFAULT.getStrategy()))
                    .findFirst()
                    .orElse(CacheStrategy.DEFAULT);
            persisterStrategy = persisterStrategies.stream()
                    .filter(it -> it.getStrategy().equals(PersisterStrategy.DEFAULT.getStrategy()))
                    .findFirst()
                    .orElse(PersisterStrategy.DEFAULT);
        }

        var idField = ReflectionUtils.getFieldsByAnnoInPOJOClass(clazz, Id.class)[0];
        ReflectionUtils.makeAccessible(idField);

        var indexDefMap = new HashMap<String, IndexDef>();
        var fields = ReflectionUtils.getFieldsByAnnoInPOJOClass(clazz, Index.class);
        for (var field : fields) {
            var indexAnnotation = field.getAnnotation(Index.class);

            IndexDef indexDef = new IndexDef(field, indexAnnotation.ascending(), indexAnnotation.unique());
            indexDefMap.put(field.getName(), indexDef);
        }

        var indexTextDefMap = new HashMap<String, IndexTextDef>();
        fields = ReflectionUtils.getFieldsByAnnoInPOJOClass(clazz, IndexText.class);
        for (var field : fields) {
            IndexTextDef indexTextDef = new IndexTextDef(field, field.getAnnotation(IndexText.class));
            indexTextDefMap.put(field.getName(), indexTextDef);
        }

        return EntityDef.valueOf(!hasUnsafeCollection, cacheStrategy.getSize(), cacheStrategy.getExpireMillisecond(), persisterStrategy, indexDefMap, indexTextDefMap);
    }

    private void checkIdField(Class<?> clazz) {
        // Check if the class implements IEntity
        AssertionUtils.isTrue(IEntity.class.isAssignableFrom(clazz), "The entity:[{}] annotated by the [{}] annotation does not implement the interface [{}]"
                , com.zfoo.orm.anno.EntityCache.class.getName(), clazz.getCanonicalName(), IEntity.class.getCanonicalName());

        // Validate the id field and id() method; an Entity class may only have one @Id annotation
        var idFields = ReflectionUtils.getFieldsByAnnoInPOJOClass(clazz, Id.class);
        AssertionUtils.isTrue(ArrayUtils.isNotEmpty(idFields) && idFields.length == 1
                , "The Entity[{}] must have only one @Id annotation (if it is indeed marked with an Id annotation, be careful not to use the Stored Id annotation)", clazz.getSimpleName());
        var idField = idFields[0];
        var idFieldType = idField.getType();
        // idField must be declared private
        AssertionUtils.isTrue(Modifier.isPrivate(idField.getModifiers()), "The id of the Entity[{}] must be private", clazz.getSimpleName());

        // The return type of the id getter must match the id field type; setId is already validated in hasUnsafeCollection
        var getIdMethod = ReflectionUtils.getMethodByNameInPOJOClass(clazz, FieldUtils.fieldToGetMethod(clazz, idField));
        var returnTypeOfGetIdMethod = getIdMethod.getReturnType();
        AssertionUtils.isTrue(returnTypeOfGetIdMethod.equals(idFieldType), "[{}] getIdMethod:[{}] return type:[{}] must be equal with type id:[{}]"
                , clazz.getSimpleName(), getIdMethod.getName(),returnTypeOfGetIdMethod.getName(), idFieldType.getName());

        // Assign a random value to the id field and call id(), verify they match to avoid bugs
        var entityInstance = ReflectionUtils.newInstance(clazz);
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
        } else if (idFieldType.equals(ObjectId.class)) {
            idFiledValue = new ObjectId();
        } else {
            throw new RunException("orm id field only supports int long float double String ObjectId");
        }

        ReflectionUtils.makeAccessible(idField);
        ReflectionUtils.setField(idField, entityInstance, idFiledValue);
        var idMethodOptional = Arrays.stream(ReflectionUtils.getAllMethods(clazz)).filter(it -> it.getName().equalsIgnoreCase("id"))
                .filter(it -> it.getParameterCount() <= 0)
                .findFirst();
        // Entity class must override the id() method
        AssertionUtils.isTrue(idMethodOptional.isPresent(), "The Entity[{}] must override the id() method", clazz.getSimpleName());
        var idMethod = idMethodOptional.get();
        ReflectionUtils.makeAccessible(idMethod);
        var idMethodReturnValue = ReflectionUtils.invokeMethod(entityInstance, idMethod);
        // The id field value and id() method return value must be equal
        AssertionUtils.isTrue(idFiledValue.equals(idMethodReturnValue), "The return value id [field:{}] of the Entity[{}] and the return value id [method:{}] are not equal, please check whether the id() method is implemented correctly"
                , clazz.getSimpleName(), idFiledValue, idMethodReturnValue);
    }

    public void checkVersionField(Class<?> clazz) {
        // @Version annotated field must be of type long
        var versionFields = ReflectionUtils.getFieldsByAnnoInPOJOClass(clazz, Version.class);
        if (ArrayUtils.isNotEmpty(versionFields)) {
            AssertionUtils.isTrue(versionFields.length == 1, "The Entity[{}] must have only one @Version annotation", clazz.getSimpleName());
            var versionField = versionFields[0];
            // idField must be declared private
            AssertionUtils.isTrue(Modifier.isPrivate(versionField.getModifiers()), "The version of the Entity[{}] must be private", clazz.getSimpleName());
            AssertionUtils.isTrue(versionField.getType().equals(long.class), "The version type of the Entity[{}] must be long", clazz.getSimpleName());
        }
    }

    private static final Set<Class<?>> unsafeCollections = Set.of(List.class, ArrayList.class, LinkedList.class
            , Set.class, HashSet.class, TreeSet.class,
            Map.class, HashMap.class, LinkedHashMap.class, TreeMap.class);

    private boolean hasUnsafeCollection(Class<?> clazz) {
        // Verify the class is a simple JavaBean to prevent concurrency issues from mixing layers (e.g. network layer and persistence layer)
        ReflectionUtils.assertIsPojoClass(clazz);
        // Must not be a generic class
        AssertionUtils.isTrue(ArrayUtils.isEmpty(clazz.getTypeParameters()), "[class:{}] can't be a generic class", clazz.getCanonicalName());
        // Must have a no-arg constructor
        if (!clazz.isRecord()) {
            ReflectionUtils.publicEmptyConstructor(clazz);
        }

        // Must not use the Storage module's @Index annotation
        var storageIndexes = ReflectionUtils.getFieldsByAnnoNameInPOJOClass(clazz, "com.zfoo.storage.anno.Index");
        if (ArrayUtils.isNotEmpty(storageIndexes)) {
            // Only Orm's own @Index annotation is allowed in Orm; using Storage's @Index is forbidden to avoid confusion and ensure robustness
            throw new RunException("only the Index annotation of Orm can be used, not the Index annotation of Storage, and it is forbidden to use it in order to avoid unnecessary misunderstandings and enhance the robustness of the project");
        }

        var filedList = ReflectionUtils.notStaticAndTransientFields(clazz);

        var hasUnsafeCollection = false;

        for (var field : filedList) {

            // Entity must have getters and setters for all fields
            FieldUtils.fieldToGetMethod(clazz, field);
            if (!clazz.isRecord()) {
                FieldUtils.fieldToSetMethod(clazz, field);
            }

            // Is a primitive type variable
            var fieldType = field.getType();
            if (isBaseType(fieldType)) {
                // do nothing
            } else if (fieldType.isArray()) {
                // Is an array
                Class<?> arrayClazz = fieldType.getComponentType();
                // ORM only supports byte[] arrays
                AssertionUtils.isTrue(arrayClazz == byte.class, "The array type of ORM[class:{}] only supports byte[]", clazz.getCanonicalName());
            } else if (Set.class.isAssignableFrom(fieldType)) {
                // Is a Set
                hasUnsafeCollection |= unsafeCollections.contains(fieldType);
                var type = field.getGenericType();
                // Field must declare a generic type
                AssertionUtils.isTrue(type instanceof ParameterizedType, "[class:{}] type declaration is incorrect, not a generic class[field:{}]", clazz.getCanonicalName(), field.getName());

                var types = ((ParameterizedType) type).getActualTypeArguments();
                // Must declare the generic type for Set
                AssertionUtils.isTrue(types.length == 1, "Set type declaration in [class:{}] is incorrect, and the generic class must be declared in [field:{}]", clazz.getCanonicalName(), field.getName());
                hasUnsafeCollectionInner(clazz, types[0]);
            } else if (List.class.isAssignableFrom(fieldType)) {
                // Is a List
                hasUnsafeCollection |= unsafeCollections.contains(fieldType);
                var type = field.getGenericType();
                // Field must declare a generic type
                AssertionUtils.isTrue(type instanceof ParameterizedType, "[class:{}] type declaration is incorrect, not a generic class[field:{}]", clazz.getCanonicalName(), field.getName());

                // Must declare the generic type for List
                var types = ((ParameterizedType) type).getActualTypeArguments();
                AssertionUtils.isTrue(types.length == 1, "List type declaration in [class:{}] is incorrect, and the generic class must be declared in [field:{}]", clazz.getCanonicalName(), field.getName());

                hasUnsafeCollectionInner(clazz, types[0]);
            } else if (Map.class.isAssignableFrom(fieldType)) {
                // Is a Map type
                hasUnsafeCollection |= unsafeCollections.contains(fieldType);

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

                if (!ClassUtils.isBaseType((Class<?>) keyType)) {
                    throw new RunException("[class:{}] type declaration is incorrect, and the key type of the Map must be the Base type", clazz.getCanonicalName());
                }
                hasUnsafeCollection |= hasUnsafeCollectionInner(clazz, valueType);
            } else if (ObjectId.class.isAssignableFrom(fieldType)) {
                // do nothing
            } else {
                hasUnsafeCollection |= hasUnsafeCollection(fieldType);
            }

        }
        return hasUnsafeCollection;
    }


    private boolean hasUnsafeCollectionInner(Class<?> currentEntityClass, Type type) {
        if (type instanceof ParameterizedType) {
            // Generic type
            Class<?> clazz = (Class<?>) ((ParameterizedType) type).getRawType();
            if (Set.class.isAssignableFrom(clazz)) {
                // Set<Set<String>>
                return unsafeCollections.contains(clazz) | hasUnsafeCollectionInner(currentEntityClass, ((ParameterizedType) type).getActualTypeArguments()[0]);
            } else if (List.class.isAssignableFrom(clazz)) {
                // List<List<String>>
                return unsafeCollections.contains(clazz) | hasUnsafeCollectionInner(currentEntityClass, ((ParameterizedType) type).getActualTypeArguments()[0]);
            } else if (Map.class.isAssignableFrom(clazz)) {
                // Map<List<String>, List<String>>
                var types = ((ParameterizedType) type).getActualTypeArguments();
                var keyType = types[0];
                var valueType = types[1];
                if (!ClassUtils.isBaseType((Class<?>) keyType)) {
                    throw new RunException("The key of the map in class:[{}] must be of the Base type", currentEntityClass.getSimpleName());
                }
                return unsafeCollections.contains(clazz) | hasUnsafeCollectionInner(currentEntityClass, valueType);
            }
        } else if (type instanceof Class) {
            Class<?> clazz = ((Class<?>) type);
            if (isBaseType(clazz)) {
                // do nothing
                return false;
            } else if (clazz.getComponentType() != null) {
                // ORM does not support multi-dimensional arrays or collections nested with arrays; only 1D arrays are supported
                throw new RunException("class:[{}] type:[{}] does not support multi-dimensional arrays or nested arrays, and only supports one-dimensional arrays"
                        , currentEntityClass.getSimpleName(), clazz.getSimpleName());
            } else if (clazz.equals(List.class) || clazz.equals(Set.class) || clazz.equals(Map.class)) {
                // ORM does not support collections containing array types
                throw new RunException("orm do not support the combination of arrays and collections with the class:[{}] type:[{}]", currentEntityClass.getSimpleName(), clazz.getSimpleName());
            } else if (clazz.equals(currentEntityClass)) {
                return false;
            } else {
                return hasUnsafeCollection(clazz);
            }
        }
        throw new RunException("class:[{}] type:[{}] is incorrect", currentEntityClass.getSimpleName(), type);
    }

    private boolean isBaseType(Class<?> clazz) {
        return clazz.isPrimitive() || Number.class.isAssignableFrom(clazz) || String.class.isAssignableFrom(clazz);
    }
}
