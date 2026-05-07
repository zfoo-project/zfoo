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

package com.zfoo.storage.manager;

import com.zfoo.protocol.collection.CollectionUtils;
import com.zfoo.protocol.exception.ExceptionUtils;
import com.zfoo.protocol.exception.RunException;
import com.zfoo.protocol.util.*;
import com.zfoo.storage.StorageContext;
import com.zfoo.storage.anno.GraalvmNativeStorage;
import com.zfoo.storage.anno.Id;
import com.zfoo.storage.anno.Storage;
import com.zfoo.storage.anno.StorageAutowired;
import com.zfoo.storage.config.StorageConfig;
import com.zfoo.storage.interpreter.data.StorageEnum;
import com.zfoo.storage.model.IStorage;
import com.zfoo.storage.model.StorageDefinition;
import com.zfoo.storage.util.function.Func1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author godotg
 */
public class StorageManager implements IStorageManager {

    private static final Logger logger = LoggerFactory.getLogger(StorageContext.class);

    // ANT wildcards: '?' one char, '*' any chars, '**' any path
    // 1. /project/*.a	matches all .a files under /project
    // 2. /project/p?ttern	matches /project/pattern and /project/pXttern (not pttern)
    // 3. /**/example	matches any path ending with /example
    // 4. /project/**/dir/file.*	matches /project/**/dir/file.*
    // 5. /**/*.jsp	matches all .jsp files anywhere
    // 'classpath:' loads first match; 'classpath*:' loads all including JARs
    private static final String SUFFIX_PATTERN = "**/*.class";

    private StorageConfig storageConfig;

    /**
     * Storage resources that are dependency-injected in the current project
     */
    private final Map<Class<?>, IStorage<?, ?>> storageMap = new HashMap<>();

    public StorageConfig getStorageConfig() {
        return storageConfig;
    }

    public void setStorageConfig(StorageConfig storageConfig) {
        this.storageConfig = storageConfig;
    }

    @Override
    public void initBefore() {
        var resourceDefinitionMap = new HashMap<Class<?>, StorageDefinition>();

        // Get the class mapped to Excel
        var clazzSet = resourceClass();
        if (CollectionUtils.isEmpty(clazzSet)) {
            logger.warn("no any storages found, please check your config. If is in graalvm environment, make sure your storage be annotated with @GraalvmNativeStorage and can be scanned by spring @Component.");
            return;
        }

        // Scan for Excel file path based on class
        for (var resourceClazz : clazzSet) {
            var resourceFile = resource(resourceClazz);
            StorageDefinition storageDefinition = new StorageDefinition(resourceClazz, resourceFile);
            if (resourceDefinitionMap.containsKey(resourceClazz)) {
                // Resource definition already registered
                throw new RuntimeException(StringUtils.format("The resource definition of the class [{}] already exists [{}]", resourceClazz, storageDefinition));
            }
            resourceDefinitionMap.put(resourceClazz, storageDefinition);
        }

        // Validate class fields
        if (!storageConfig.isWriteable()) {
            var allRelevantClass = new HashSet<Class<?>>();
            resourceDefinitionMap.values().forEach(it -> allRelevantClass.addAll(ClassUtils.relevantClass(it.getClazz())));
            for (var clazz : allRelevantClass) {
                if (clazz.isRecord()) {
                    continue;
                }
                ReflectionUtils.assertIsPojoClass(clazz);
                ReflectionUtils.publicEmptyConstructor(clazz);
                var fieldList = ReflectionUtils.notStaticAndTransientFields(clazz);
                for (var field : fieldList) {
                    if (Modifier.isPublic(field.getModifiers())) {
                        // Static resource fields must be private; use writeable=true to allow setters
                        throw new RunException("Static resource classes cannot be modified, class:[{}] filed:{}[] cannot be modified by public, use private modified or enable writeable configuration", clazz, field.getName());
                    }

                    var setMethodName = StringUtils.EMPTY;
                    try {
                        setMethodName = FieldUtils.fieldToSetMethod(clazz, field);
                    } catch (Exception e) {
                        // No setter is correct for immutable resources
                    }
                    if (StringUtils.isNotBlank(setMethodName)) {
                        // Static resource fields must not have setters; remove the setter or enable writeable
                        throw new RunException("Static resource classes cannot be modified, [class:{}][filed:{}] cannot contain set method [{}], delete set method or enable writeable configuration", clazz, field.getName(), setMethodName);
                    }
                }
            }
        }

        try {
            for (var definition : resourceDefinitionMap.values()) {
                var clazz = definition.getClazz();
                var resource = definition.getResource();
                var fileExtName = FileUtils.fileExtName(resource.getFilename());
                var storage = AbstractStorage.parse(resource.getInputStream(), clazz, fileExtName);
                storageMap.putIfAbsent(clazz, storage);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void inject() {
        var applicationContext = StorageContext.getApplicationContext();
        var componentBeans = applicationContext.getBeansWithAnnotation(Component.class);
        for (var bean : componentBeans.values()) {

            // Prevent direct assignment being ignored when CGLIB-proxied
            var targetBean = Objects.requireNonNullElse(AopProxyUtils.getSingletonTarget(bean), bean);

            var clazz = targetBean.getClass();
            ReflectionUtils.filterFieldsInClass(clazz, field -> field.isAnnotationPresent(StorageAutowired.class), field -> {
                Type type = field.getGenericType();

                if (!(type instanceof ParameterizedType)) {
                    throw new RuntimeException(StringUtils.format("bean:[{}] type declaration is incorrect, not a generic class", targetBean.getClass().getSimpleName()));
                }

                Type[] types = ((ParameterizedType) type).getActualTypeArguments();

                // @StorageAutowired
                // Storage<Integer, ActivityResource> resources;
                Class<?> keyClazz = (Class<?>) types[0];

                Class<?> resourceClazz = (Class<?>) types[1];

                IStorage<?, ?> storage = storageMap.get(resourceClazz);

                if (storage == null) {
                    throw new RuntimeException(StringUtils.format("Static class resource:[{}] does not exist", resourceClazz.getSimpleName()));
                }

                Field[] idFields = ReflectionUtils.getFieldsByAnnoInPOJOClass(resourceClazz, Id.class);
                if (idFields.length != 1) {
                    throw new RuntimeException(StringUtils.format("Static class resource:[{}] has no @Id annotation", resourceClazz.getSimpleName()));
                }

                if (!keyClazz.getSimpleName().toLowerCase().contains(idFields[0].getType().getSimpleName().toLowerCase())) {
                    // Injected type does not match the expected generic type
                    throw new RuntimeException(StringUtils.format("Inject static class configuration storage:[{}] key:[{}] type and generic type type:[{}] do not match", resourceClazz.getSimpleName(), idFields[0].getType().getSimpleName(), keyClazz.getSimpleName()));
                }

                ReflectionUtils.makeAccessible(field);
                ReflectionUtils.setField(field, targetBean, storage);
                storage.setRecycle(false);
            });
        }
    }

    @Override
    public void initAfter() {
        if (storageConfig.isRecycle()) {
            storageMap.entrySet().stream().filter(it -> it.getValue().isRecycle()).map(it -> it.getValue()).forEach(it -> it.recycleStorage());
        } else {
            storageMap.entrySet().stream().map(it -> it.getValue()).forEach(it -> it.setRecycle(false));
        }
    }

    public <T> List<T> getList(Class<T> clazz) {
        IStorage<?, ?> storage = getStorage(clazz);
        return (List<T>) storage.getAll();
    }

    public <T, INDEX> List<T> getIndexes(Class<T> clazz, Func1<T, INDEX> func, INDEX index) {
        var storage = getStorage(clazz);
        return storage.getIndexes(func, index);
    }

    public <T, K> T get(Class<T> clazz, K id) {
        var storage = getStorage(clazz);
        return storage.get(id);
    }

    @Override
    public <K, V, T extends IStorage<K, V>> T getStorage(Class<V> clazz) {
        var storage = storageMap.get(clazz);
        if (storage == null) {
            throw new RunException("There is no [{}] defined Storage and unable to get it", clazz.getCanonicalName());
        }
        if (storage.isRecycle()) {
            // Storage not in use; released to save memory. Use @StorageAutowired or recycle=false to keep it
            logger.warn("Storage [{}] is not used, it was freed to save memory; use @StorageAutowired or turn off recycle configuration", clazz.getCanonicalName());
        }
        return (T) storage;
    }

    @Override
    public Map<Class<?>, IStorage<?, ?>> storageMap() {
        return storageMap;
    }

    @Override
    public void updateStorage(Class<?> clazz, IStorage<?, ?> storage) {
        storageMap.put(clazz, storage);
    }

    @Override
    public StorageConfig storageConfig() {
        return getStorageConfig();
    }

    private Set<Class<?>> resourceClass() {
        // In GraalVM: PathMatchingResourcePatternResolver unavailable; get resources from Spring context
        if (GraalVmUtils.isGraalVM()) {
            var applicationContext = StorageContext.getApplicationContext();
            var nativeResources = applicationContext.getBeansWithAnnotation(GraalvmNativeStorage.class);
            return nativeResources.values().stream().map(it -> it.getClass()).collect(Collectors.toSet());
        } else {
            return scanResourceAnno();
        }
    }

    private Set<Class<?>> scanResourceAnno() {
        var scanPackages = StringUtils.tokenize(storageConfig.getScanPackage(), ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);
        var result = new HashSet<Class<?>>();
        try {
            var resourcePatternResolver = new PathMatchingResourcePatternResolver();
            var metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);
            for (var scanPackage : scanPackages) {
                var packageSearchPath = "classpath*:" + scanPackage.replace(StringUtils.PERIOD, StringUtils.SLASH) + StringUtils.SLASH + SUFFIX_PATTERN;
                var resources = resourcePatternResolver.getResources(packageSearchPath);
                var resourceName = Storage.class.getName();
                for (var resource : resources) {
                    if (resource.isReadable()) {
                        var metadataReader = metadataReaderFactory.getMetadataReader(resource);
                        var annoMeta = metadataReader.getAnnotationMetadata();
                        if (annoMeta.hasAnnotation(resourceName)) {
                            ClassMetadata clazzMeta = metadataReader.getClassMetadata();
                            Class<?> resourceClazz = Class.forName(clazzMeta.getClassName());
                            result.add(resourceClazz);
                        }
                    }
                }
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Unable to read resource information", e);
        }
    }

    private Resource resource(Class<?> clazz) {
        if (GraalVmUtils.isGraalVM()) {
            var resourceLoader = new DefaultResourceLoader();
            var resource = resourceLoader.getResource(clazz.getAnnotation(GraalvmNativeStorage.class).value());
            return resource;
        } else {
            return scanResourceFile(clazz);
        }
    }

    private Resource scanResourceFile(Class<?> clazz) {
        var resourcePatternResolver = new PathMatchingResourcePatternResolver();
        var metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);

        var alias = clazz.getAnnotation(Storage.class).value();
        var fileName = StringUtils.isEmpty(alias) ? clazz.getSimpleName() : alias;

        try {
            // A class can match only one resource file; multiple matches are ambiguous
            var resourceSet = new HashSet<Resource>();
            var resourceLocations = StringUtils.tokenize(storageConfig.getResourceLocation(), ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);
            for (var resourceLocation : resourceLocations) {
                var resources = new ArrayList<Resource>();
                var packageSearchPath = StringUtils.format("{}/**/{}.*", resourceLocation, fileName);
                packageSearchPath = packageSearchPath.replaceAll("//", "/");
                try {
                    Arrays.stream(resourcePatternResolver.getResources(packageSearchPath)).filter(it -> StorageEnum.containsResourceEnum(FileUtils.fileExtName(it.getFilename()))).forEach(it -> resources.add(it));
                } catch (Exception e) {
                    // do nothing
                }

                // Wildcards cannot match root directory; retry search from root
                if (resources.isEmpty()) {
                    packageSearchPath = StringUtils.format("{}/{}.*", resourceLocation, fileName);
                    packageSearchPath = packageSearchPath.replaceAll("//", "/");
                    Arrays.stream(resourcePatternResolver.getResources(packageSearchPath)).filter(it -> StorageEnum.containsResourceEnum(FileUtils.fileExtName(it.getFilename()))).forEach(it -> resources.add(it));
                }
                resourceSet.addAll(resources);
            }

            if (CollectionUtils.isEmpty(resourceSet)) {
                throw new FileNotFoundException(fileName);
            }
            if (resourceSet.size() > 1) {
                var resourceNames = resourceSet.stream().map(it -> it.getFilename()).collect(Collectors.joining(StringUtils.COMMA));
                throw new RuntimeException(StringUtils.format("Resource class:[{}] has duplicate configuration [{}]", clazz.getSimpleName(), resourceNames));
            }

            return resourceSet.stream().findFirst().get();

        } catch (IOException e) {
            throw new RuntimeException(ExceptionUtils.getMessage(e));
        }
    }
}
