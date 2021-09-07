/*
 * Copyright (C) 2020 The zfoo Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.zfoo.net.config.manager;

import com.zfoo.net.config.model.NetConfig;
import com.zfoo.net.consumer.balancer.AbstractConsumerLoadBalancer;
import com.zfoo.net.consumer.registry.IRegistry;
import com.zfoo.net.consumer.registry.ZookeeperRegistry;
import com.zfoo.protocol.ProtocolManager;
import com.zfoo.protocol.collection.CollectionUtils;
import com.zfoo.protocol.registration.ProtocolModule;
import com.zfoo.protocol.util.AssertionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Objects;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public class ConfigManager implements IConfigManager {

    private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);

    /**
     * 本地配置
     */
    private NetConfig localConfig;

    private AbstractConsumerLoadBalancer consumerLoadBalancer;

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

    @Override
    public AbstractConsumerLoadBalancer consumerLoadBalancer() {
        return consumerLoadBalancer;
    }

    @Override
    public void initRegistry() {
        // 通过protocol，写入provider的module的id和version
        var providerConfig = localConfig.getProvider();
        if (Objects.nonNull(providerConfig) && CollectionUtils.isNotEmpty(providerConfig.getModules())) {
            var providerModules = new ArrayList<ProtocolModule>(providerConfig.getModules().size());
            for (var providerModule : providerConfig.getModules()) {
                var module = ProtocolManager.moduleByModuleName(providerModule.getName());
                AssertionUtils.isTrue(module != null, "服务提供者[name:{}]在协议文件中不存在", providerModule.getName());
                providerModules.add(module);
            }
            providerConfig.setModules(providerModules);
        }

        // 通过protocol，写入consumer的module的id和version
        var consumerConfig = localConfig.getConsumer();
        if (Objects.nonNull(consumerConfig) && CollectionUtils.isNotEmpty(consumerConfig.getModules())) {
            var consumerModules = new ArrayList<ProtocolModule>(consumerConfig.getModules().size());
            for (var providerModule : consumerConfig.getModules()) {
                var module = ProtocolManager.moduleByModuleName(providerModule.getName());
                AssertionUtils.isTrue(module != null, "消费者[name:{}]在协议文件中不存在", providerModule.getName());
                consumerModules.add(module);
            }
            consumerConfig.setModules(consumerModules);
            consumerLoadBalancer = AbstractConsumerLoadBalancer.valueOf(consumerConfig.getLoadBalancer());
        }

        registry = new ZookeeperRegistry();
        registry.start();
    }

    @Override
    public IRegistry getRegistry() {
        return registry;
    }

}
