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

import com.zfoo.event.EventContext;
import com.zfoo.event.schema.EventRegisterProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 通过@Import注解先注册下EventRegisterProcessor对象，当每个bean对象被注入到IOC容器后，都会经过EventRegisterProcessor这个后置处理器，
 * 从而在里面扫描每个bean对象，得到带有@EventReceiver注解的方法
 *
 * @author godotg
 * @version 3.0
 */
@Configuration(proxyBeanMethods = false)
@Import({EventRegisterProcessor.class})
public class EventAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public EventContext eventContext() {
        return new EventContext();
    }

}
