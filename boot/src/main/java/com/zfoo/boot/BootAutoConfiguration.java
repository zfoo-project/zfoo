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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zfoo.boot.graalvm.GraalvmProtocolHints;
import com.zfoo.boot.graalvm.GraalvmStorageHints;
import com.zfoo.protocol.util.GraalVmUtils;
import com.zfoo.protocol.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

/**
 * @author godotg
 */
@Configuration(proxyBeanMethods = false)
@ImportRuntimeHints(GraalvmProtocolHints.class)
public class BootAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(BootAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper getObjectMapper() {
        logger.info("Jackson auto config successfully!");
        logger.info("GraalVM environment [{}]", GraalVmUtils.isGraalVM());
        return JsonUtils.MAPPER;
    }

}
