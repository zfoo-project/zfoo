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
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.storage.model.config.StorageConfig;
import com.zfoo.storage.model.vo.ResourceDef;
import com.zfoo.storage.model.vo.Storage;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public class StorageManager implements IStorageManager {

    // ANT通配符有三种, ? :匹配任何单字符; * :匹配0或者任意数量的字符; ** :匹配0或者更多的目录
    // 1. /project/*.a	匹配项目根路径下所有在project路径下的.a文件
    // 2. /project/p?ttern	匹配项目根路径下 /project/pattern 和 /app/pXttern,但是不包括/app/pttern
    // 3. /**/example	匹配项目根路径下 /project/example, /project/foow/example, 和 /example
    // 4. /project/**/dir/file.*	匹配项目根路径下/project/dir/file.jsp, /project/foow/dir/file.html,/project/foow/bar/dir/file.pdf
    // 5. /**/*.jsp	匹配项目根路径下任何的.jsp 文件
    // classpath：和classpath*： 的区别，前者只会第一个加载到的类，后者会加载所有的类，包括jar文件下的类
    private static final String SUFFIX_PATTERN = "**/*.class";

    private StorageConfig storageConfig;

    /**
     * 在当前项目被依赖注入，被使用的Storage
     */
    private Map<Class<?>, Storage<?, ?>> storageMap = new HashMap<>();

    /**
     * 全部的Storage定义
     */
    private Set<Class<?>> allStorageClassSet = new HashSet<>();

    public StorageConfig getStorageConfig() {
        return storageConfig;
    }

    public void setStorageConfig(StorageConfig storageConfig) {
        this.storageConfig = storageConfig;
    }

    @Override
    public void initBefore() {
        var resourceDefinitionMap = new HashMap<Class<?>, ResourceDef>();
        var clazzNameSet = scanResourceAnno(storageConfig.getScanPackage());
        for (var clazzName : clazzNameSet) {
            Class<?> resourceClazz;
            try {
                resourceClazz = Class.forName(clazzName);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(StringUtils.format("无法获取资源类[{}]", clazzName));
            }

            var resourceFile = scanResourceFile(resourceClazz);
            ResourceDef resourceDef = new ResourceDef(resourceClazz, resourceFile);
            if (resourceDefinitionMap.containsKey(resourceClazz)) {
                throw new RuntimeException(StringUtils.format("类的资源定义[{}]已经存在[{}]", resourceClazz, resourceDef));
            }
            resourceDefinitionMap.put(resourceClazz, resourceDef);
        }

        try {
            for (var definition : resourceDefinitionMap.values()) {
                var clazz = definition.getClazz();
                Storage<?, ?> storage = new Storage<>();
                storage.init(definition.getResource().getInputStream(), definition.getClazz());
                storageMap.putIfAbsent(clazz, storage);
                allStorageClassSet.add(clazz);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void initAfter() {
        var unusableStorageClassList = storageMap.entrySet().stream()
                .filter(it -> !it.getValue().isUsable())
                .map(it -> it.getKey())
                .collect(Collectors.toList());

        unusableStorageClassList.forEach(it -> {
            storageMap.remove(it);
        });
    }

    @Override
    public Storage<?, ?> getStorage(Class<?> clazz) {
        return storageMap.get(clazz);
    }

    @Override
    public Set<Class<?>> allStorageClassSet() {
        return allStorageClassSet;
    }

    @Override
    public Map<Class<?>, Storage<?, ?>> getStorageMap() {
        return storageMap;
    }


    @Override
    public void updateStorage(Class<?> clazz, Storage<?, ?> storage) {
        storageMap.put(clazz, storage);
    }

    private Set<String> scanResourceAnno(String scanLocation) {
        var resourcePatternResolver = new PathMatchingResourcePatternResolver();
        var metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);

        try {
            var packageSearchPath = ResourceUtils.CLASSPATH_URL_PREFIX + scanLocation.replace(StringUtils.PERIOD, StringUtils.SLASH) + StringUtils.SLASH + SUFFIX_PATTERN;
            var resources = resourcePatternResolver.getResources(packageSearchPath);
            var result = new HashSet<String>();
            var name = com.zfoo.storage.model.anno.Resource.class.getName();
            for (var resource : resources) {
                if (resource.isReadable()) {
                    var metadataReader = metadataReaderFactory.getMetadataReader(resource);
                    var annoMeta = metadataReader.getAnnotationMetadata();
                    if (annoMeta.hasAnnotation(name)) {
                        ClassMetadata clazzMeta = metadataReader.getClassMetadata();
                        result.add(clazzMeta.getClassName());
                    }
                }
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException("无法读取资源信息:" + e);
        }
    }

    private Resource scanResourceFile(Class<?> clazz) {
        var resourcePatternResolver = new PathMatchingResourcePatternResolver();
        var metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);

        try {
            var resourceList = new ArrayList<Resource>();

            var packageSearchPath = StringUtils.format("{}/**/{}.{}", storageConfig.getResourceLocation(), clazz.getSimpleName(), storageConfig.getResourceSuffix());
            packageSearchPath = packageSearchPath.replaceAll("//", "/");
            try {
                resourceList.addAll(Arrays.asList(resourcePatternResolver.getResources(packageSearchPath)));
            } catch (Exception e) {

            }

            // 通配符无法匹配根目录，所以如果找不到，再从根目录查找一遍
            if (CollectionUtils.isEmpty(resourceList)) {
                packageSearchPath = StringUtils.format("{}/{}.{}", storageConfig.getResourceLocation(), clazz.getSimpleName(), storageConfig.getResourceSuffix());
                packageSearchPath = packageSearchPath.replaceAll("//", "/");
                resourceList.addAll(Arrays.asList(resourcePatternResolver.getResources(packageSearchPath)));
            }

            if (CollectionUtils.isEmpty(resourceList)) {
                throw new RuntimeException(StringUtils.format("无法找到配置文件[{}]", clazz.getSimpleName()));
            } else if (resourceList.size() > 1) {
                throw new RuntimeException(StringUtils.format("找到重复的配置文件[{}]", clazz.getSimpleName()));
            } else {
                return resourceList.get(0);
            }

        } catch (IOException e) {
            throw new RuntimeException(ExceptionUtils.getMessage(e));
        }
    }
}
