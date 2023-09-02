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

package com.zfoo.boot;

import com.zfoo.orm.OrmContext;
import com.zfoo.orm.accessor.MongodbAccessor;
import com.zfoo.orm.manager.OrmManager;
import com.zfoo.orm.model.anno.GraalvmNativeEntityCache;
import com.zfoo.orm.model.config.OrmConfig;
import com.zfoo.orm.query.MongodbQuery;
import com.zfoo.protocol.util.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aot.hint.BindingReflectionHintsRegistrar;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

import java.util.HashSet;

/**
 * @author godotg
 * @version 3.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(OrmConfig.class)
@ImportRuntimeHints(OrmAutoConfiguration.GraalvmOrmHints.class)
public class OrmAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(OrmAutoConfiguration.class);

    // OrmConfig in the specific business
    // If the OrmConfig bean is not configured in the business, then the Orm automatic assembly here will not take effect.
    @Bean
    @ConditionalOnBean(OrmConfig.class)
    public OrmManager ormManager(OrmConfig ormConfig) {
        var ormManager = new OrmManager();
        ormManager.setOrmConfig(ormConfig);
        return ormManager;
    }

    @Bean
    @ConditionalOnMissingBean
    public MongodbAccessor mongodbAccessor() {
        return new MongodbAccessor();
    }

    @Bean
    @ConditionalOnMissingBean
    public MongodbQuery mongodbQuery() {
        return new MongodbQuery();
    }


    @Bean
    @ConditionalOnMissingBean
    public OrmContext ormContext() {
        return new OrmContext();
    }


    // Register runtime hints for the token library
    public static class GraalvmOrmHints implements RuntimeHintsRegistrar {

        private final BindingReflectionHintsRegistrar bindingRegistrar = new BindingReflectionHintsRegistrar();

        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            logger.info("orm graalvm aot runtime hints register");

            var classes = new HashSet<Class<?>>();
            classes.add(OrmConfig.class);

            try {
                for (var className : ClassUtils.getAllClasses("")) {
                    try {
                        var clazz = Class.forName(className);
                        if (!clazz.isAnnotationPresent(GraalvmNativeEntityCache.class)) {
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
                logger.info("orm graalvm aot hints register serialization [{}]", clazz);
            }
        }
    }
}
