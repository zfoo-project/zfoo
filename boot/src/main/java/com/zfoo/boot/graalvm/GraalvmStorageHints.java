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

import com.zfoo.protocol.util.ClassUtils;
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.storage.anno.GraalvmNativeStorage;
import com.zfoo.storage.config.StorageConfig;
import com.zfoo.storage.interpreter.data.StorageData;
import com.zfoo.storage.interpreter.data.StorageEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aot.hint.BindingReflectionHintsRegistrar;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

import java.util.HashSet;

/**
 * Register runtime hints for the token library
 *
 * @author godotg
 */
public class GraalvmStorageHints implements RuntimeHintsRegistrar {

    private static final Logger logger = LoggerFactory.getLogger(GraalvmStorageHints.class);

    private final BindingReflectionHintsRegistrar bindingRegistrar = new BindingReflectionHintsRegistrar();

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        logger.info("storage graalvm aot runtime hints register");

        var classes = new HashSet<Class<?>>();
        classes.add(StorageData.class);
        classes.add(StorageConfig.class);

        try {
            for (var className : ClassUtils.getAllClasses("")) {
                try {
                    var clazz = Class.forName(className);
                    if (!clazz.isAnnotationPresent(GraalvmNativeStorage.class)) {
                        continue;
                    }
                    classes.add(clazz);
                    classes.addAll(ClassUtils.relevantClass(clazz));
                } catch (Throwable t) {
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        for (var clazz : classes) {
            this.bindingRegistrar.registerReflectionHints(hints.reflection(), clazz);
            logger.info("storage graalvm aot hints register serialization [{}]", clazz);
        }

        for (var resource : StorageEnum.values()) {
            var include = StringUtils.format("*.{}", resource.getType());
            hints.resources().registerPattern(include);
            logger.info("storage graalvm aot hints register resources [{}]", include);
        }
    }
}
