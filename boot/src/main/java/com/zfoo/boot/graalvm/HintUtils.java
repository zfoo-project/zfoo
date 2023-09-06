/*
 * Copyright (C) 2020 The zfoo Authors
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.zfoo.boot.graalvm;

import com.zfoo.protocol.collection.ArrayUtils;
import com.zfoo.protocol.collection.CollectionUtils;
import com.zfoo.protocol.util.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static org.springframework.aot.hint.MemberCategory.*;

/**
 * @author godotg
 */
public abstract class HintUtils {

    private static final Logger logger = LoggerFactory.getLogger(HintUtils.class);


    public static Set<Class<?>> filterAllClass(List<Class<?>> annotations, List<Class<?>> interfaces) {
        var classes = new HashSet<Class<?>>();
        var annotationNames = annotations.stream().map(it -> it.getName()).toList();
        var interfaceNames = interfaces.stream().map(it -> it.getName()).toList();


        var resourcePatternResolver = new PathMatchingResourcePatternResolver();
        var metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);
        var resources = new HashSet<Resource>();
        try {
            resources.addAll(List.of(resourcePatternResolver.getResources("classpath*:/**/*.class")));
            resources.addAll(List.of(resourcePatternResolver.getResources("classpath*:*.class")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (var resource : resources) {
            if (!resource.isReadable()) {
                continue;
            }
            Class<?> clazz = null;
            try {
                var metadataReader = metadataReaderFactory.getMetadataReader(resource);
                var clazzMeta = metadataReader.getClassMetadata();

                // check annotation
                var annoMeta = metadataReader.getAnnotationMetadata();
                if (annotationNames.stream().anyMatch(it -> annoMeta.hasAnnotation(it))) {
                    clazz = ClassUtils.forName(clazzMeta.getClassName());
                    classes.add(clazz);
                    continue;
                }

                // check interface
                if (CollectionUtils.isEmpty(interfaces)) {
                    continue;
                }
                var metaInterfaces = metadataReader.getClassMetadata().getInterfaceNames();
                if (ArrayUtils.isEmpty(metaInterfaces)) {
                    continue;
                }
                for(var metaInterface : metaInterfaces) {
                    if (interfaceNames.stream().anyMatch(it -> it.equals(metaInterface))) {
                        clazz = ClassUtils.forName(clazzMeta.getClassName());
                        classes.add(clazz);
                        break;
                    }
                }
            } catch (Throwable t) {
                // do nothing
            }
        }

        return classes;
    }

    public static void registerRelevantClasses(RuntimeHints hints, Set<Class<?>> classes) {
        var relevantClasses = classes.stream()
                .map(it -> ClassUtils.relevantClass(it))
                .flatMap(it -> it.stream())
                .distinct()
                .toList();

        registerClasses(hints, relevantClasses);
    }

    public static void registerClasses(RuntimeHints hints, List<Class<?>> classes) {
        for (var clazz : classes) {
            hints.reflection().registerType(clazz, DECLARED_FIELDS, INVOKE_PUBLIC_CONSTRUCTORS, INVOKE_PUBLIC_METHODS);
            logger.info("zfoo graalvm for spring aot hint register [{}]", clazz);
        }
    }


}
