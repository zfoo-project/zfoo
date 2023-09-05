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

import com.zfoo.boot.graalvm.GraalvmNetHints;
import com.zfoo.net.NetContext;
import com.zfoo.net.config.ConfigManager;
import com.zfoo.net.config.model.NetConfig;
import com.zfoo.net.consumer.Consumer;
import com.zfoo.net.packet.PacketService;
import com.zfoo.net.router.Router;
import com.zfoo.net.session.SessionManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

/**
 * @author godotg
 * @version 3.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(NetConfig.class)
@ImportRuntimeHints(GraalvmNetHints.class)
public class NetAutoConfiguration {

    @Bean
    @ConditionalOnBean(NetConfig.class)
    public ConfigManager configManager(NetConfig netConfig) {
        var configManager = new ConfigManager();
        configManager.setLocalConfig(netConfig);
        return configManager;
    }

    @Bean
    @ConditionalOnMissingBean
    public PacketService packetService() {
        return new PacketService();
    }

    @Bean
    @ConditionalOnMissingBean
    public Router router() {
        return new Router();
    }

    @Bean
    @ConditionalOnMissingBean
    public Consumer consumer() {
        return new Consumer();
    }

    @Bean
    @ConditionalOnMissingBean
    public SessionManager sessionManager() {
        return new SessionManager();
    }

    @Bean
    @ConditionalOnMissingBean
    public NetContext netContext() {
        return new NetContext();
    }

}
