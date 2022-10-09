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
import com.zfoo.orm.manager.OrmManager;
import com.zfoo.orm.accessor.MongodbAccessor;
import com.zfoo.orm.model.config.OrmConfig;
import com.zfoo.orm.query.MongodbQuery;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author godotg
 * @version 3.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(OrmConfig.class)
public class OrmAutoConfiguration {

    // OrmConfig的装配在于具体的业务游戏中，业务游戏中不配置OrmConfig这个Bean，那么这里的Orm自动装配也不会生效。
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

}
