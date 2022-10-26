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

import com.zfoo.boot.resource.StudentResource;
import com.zfoo.protocol.ProtocolManager;
import com.zfoo.protocol.generate.GenerateOperation;
import com.zfoo.protocol.serializer.CodeLanguage;
import com.zfoo.protocol.util.JsonUtils;
import com.zfoo.storage.StorageContext;
import com.zfoo.storage.model.config.StorageConfig;
import com.zfoo.storage.model.vo.Storage;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * @author godotg
 * @version 3.0
 */
@Ignore
@SpringBootApplication(exclude = {
        // 排除MongoDB自动配置
        MongoAutoConfiguration.class
})
public class StorageTest {

    private static final Logger logger = LoggerFactory.getLogger(StorageTest.class);

    @Bean
    public StorageConfig storageConfig(Environment environment) {
        var config = new StorageConfig();
        config.setScanPackage("com.zfoo.boot");
        config.setResourceLocation("classpath:/excel");
        config.setWriteable(true);
        config.setRecycle(false);
        return config;
    }

    @Test
    public void startApplication() {
        var springBoot = new SpringApplicationBuilder();
        springBoot.sources(StorageTest.class).web(WebApplicationType.NONE).run();

        var protocols = StorageContext.getStorageManager().storageMap().keySet();

        var operation = new GenerateOperation();
        operation.getGenerateLanguages().add(CodeLanguage.GdScript);
        ProtocolManager.initProtocolAuto(protocols, operation);

        var storage = (Storage<Integer, StudentResource>) StorageContext.getStorageManager().getStorage(StudentResource.class);
        for (StudentResource resource : storage.getAll()) {
            logger.info(JsonUtils.object2String(resource));
        }

    }

}
