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

package com.zfoo.net.config;

import com.zfoo.net.config.model.NetConfig;
import com.zfoo.net.consumer.registry.IRegistry;
import com.zfoo.net.consumer.registry.ZookeeperRegistry;
import com.zfoo.protocol.ProtocolManager;
import com.zfoo.protocol.collection.CollectionUtils;
import com.zfoo.protocol.util.AssertionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Objects;

/**
 * @author godotg
 */
public class ConfigManager implements IConfigManager {

    private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);

    /**
     * 本地配置
     */
    private NetConfig localConfig;

    /**
     * 注册中心
     */
    private IRegistry registry;

    @Override
    public NetConfig getLocalConfig() {
        return localConfig;
    }

    public void setLocalConfig(NetConfig localConfig) {
        this.localConfig = localConfig;
    }

    /**
     * 在调用这个方法之前：localConfig也就是NetConfig虽然已经被注册到Spring容器中，但是模块id等信息依然是没有的。这需要从protocol.xml中读取覆盖才行
     */
    @Override
    public void initRegistry() {
        // 通过protocol，写入provider的module的id和version
        var providerConfig = localConfig.getProvider();
        if (providerConfig != null && CollectionUtils.isNotEmpty(providerConfig.getProviders())) {
            // 服务提供者名字Set列表
            var providerSet = new HashSet<String>();
            // 检查并且替换配置文件中的ProtocolModule
            for (var provider : providerConfig.getProviders()) {
                var protocolModule = provider.getProtocolModule();
                var providerName = provider.getProvider();
                AssertionUtils.isTrue(ProtocolManager.moduleByModuleName(protocolModule) != null, "protocol module [{}] does not exist in the protocol manager", provider);
                AssertionUtils.isTrue(providerSet.add(providerName), "provider:[{}] has duplicate provider name module [provider:{}]", provider, protocolModule);
            }
        }

        var consumerConfig = localConfig.getConsumer();
        if (Objects.nonNull(consumerConfig) && CollectionUtils.isNotEmpty(consumerConfig.getConsumers())) {
            // 服务消费者名字Set列表
            var consumerSet = new HashSet<String>();
            for (var consumerModule : consumerConfig.getConsumers()) {
                // 提供的接口实现 提供者名
                var consumer = consumerModule.getConsumer();
                AssertionUtils.isTrue(consumerSet.add(consumer), "consumer:[{}] has duplicate consumer module", consumer);
            }
        }

        // 走到这之后，NetConfig通过app.xml(读取有哪些消费者)+protocol.xml(模块号信息)完成了初始化
        // 接下来就是通过注册中心，把生产者和消费者关联起来
        registry = new ZookeeperRegistry();
        registry.start();
    }

    @Override
    public IRegistry getRegistry() {
        return registry;
    }
}
