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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aot.hint.RuntimeHints;

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

    /**
     * 与set有关的所有内部类包括泛型
     */
    public static Set<Class<?>> filterAllClass(Predicate<Class<?>> predicate) {
        try {
            var classes = new HashSet<Class<?>>();
            for (var className : ClassUtils.getAllClasses("")) {
                Class<?> clazz = null;
                try {
                    clazz = Class.forName(className);
                } catch (Throwable t) {
                    // do nothing
                }

                if (clazz == null) {
                    continue;
                }

                if (predicate.test(clazz)) {
                    classes.add(clazz);
                }
            }
            return classes;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
