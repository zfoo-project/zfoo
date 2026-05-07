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

import com.zfoo.net.NetContext;
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
     * Local network configuration for this node.
     */
    private NetConfig localConfig;

    /**
     * Service registry used for provider registration and discovery.
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
     * Before this method is called, localConfig (NetConfig) has been registered in the Spring container
     * but module IDs and versions are not yet populated — they must be read from protocol.xml.
     */
    @Override
    public void initRegistry() {
        // Populate provider module IDs and versions from the protocol configuration
        var providerConfig = localConfig.getProvider();
        if (providerConfig != null && CollectionUtils.isNotEmpty(providerConfig.getProviders())) {
            // Set of declared provider names used for duplicate detection
            var providerSet = new HashSet<String>();
            // Validate and cross-reference each ProtocolModule entry in the config
            for (var provider : providerConfig.getProviders()) {
                var protocolModule = provider.getProtocolModule();
                var providerName = provider.getProvider();
                AssertionUtils.isTrue(ProtocolManager.moduleByModuleName(protocolModule) != null, "protocol module [{}] does not exist in the protocol manager", provider);
                AssertionUtils.isTrue(providerSet.add(providerName), "provider:[{}] has duplicate provider name module [provider:{}]", provider, protocolModule);
            }
        }

        var consumerConfig = localConfig.getConsumer();
        if (Objects.nonNull(consumerConfig) && CollectionUtils.isNotEmpty(consumerConfig.getConsumers())) {
            // Set of declared consumer names used for duplicate detection
            var consumerSet = new HashSet<String>();
            for (var consumerModule : consumerConfig.getConsumers()) {
                // The provider name that this consumer subscribes to
                var consumer = consumerModule.getConsumer();
                AssertionUtils.isTrue(consumerSet.add(consumer), "consumer:[{}] has duplicate consumer module", consumer);
            }
        }

        // At this point, NetConfig is fully initialised via app.xml (consumers) + protocol.xml (module IDs)
        // The registry now links providers and consumers together
        try {
            var registryConfig = NetContext.getConfigManager().getLocalConfig().getRegistry();
            if (registryConfig != null) {
                String driverClassName = registryConfig.getDriverClassName();
                if (driverClassName == null || driverClassName.isBlank()){
                    registry = new ZookeeperRegistry();
                } else {
                    registry = (IRegistry) Class.forName(driverClassName).getDeclaredConstructor().newInstance();
                }
            } else {
                registry = new ZookeeperRegistry();
            }
            registry.start();
        } catch (Exception e) {
            throw new RuntimeException("registry instance err", e);
        }
    }

    @Override
    public IRegistry getRegistry() {
        return registry;
    }
}
