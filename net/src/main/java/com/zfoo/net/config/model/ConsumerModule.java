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

package com.zfoo.net.config.model;

import com.zfoo.protocol.registration.ProtocolModule;

import java.util.Objects;

/**
 * @author godotg
 */
public class ConsumerModule {

    private ProtocolModule protocolModule;

    // 负载均衡方式
    private String loadBalancer;

    // 消费哪个provider
    private String consumer;

    public ConsumerModule(ProtocolModule protocolModule, String loadBalancer, String consumer) {
        this.protocolModule = protocolModule;
        this.consumer = consumer;
        this.loadBalancer = loadBalancer;
    }

    public ConsumerModule(String protocolModule, String loadBalancer, String consumer) {
        this.protocolModule = new ProtocolModule((byte) 0, protocolModule);
        this.consumer = consumer;
        this.loadBalancer = loadBalancer;
    }
    
    public boolean matchProvider(ProviderModule providerModule) {
        return Objects.equals(protocolModule.getName(), providerModule.getProtocolModule().getName()) && Objects.equals(consumer, providerModule.getProvider());
    }

    public String getConsumer() {
        return consumer;
    }

    public void setConsumer(String consumer) {
        this.consumer = consumer;
    }

    public String getLoadBalancer() {
        return loadBalancer;
    }

    public void setLoadBalancer(String loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    public ProtocolModule getProtocolModule() {
        return protocolModule;
    }

    public void setProtocolModule(ProtocolModule protocolModule) {
        this.protocolModule = protocolModule;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ConsumerModule that = (ConsumerModule) o;
        return Objects.equals(protocolModule, that.protocolModule) && Objects.equals(consumer, that.consumer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(protocolModule, loadBalancer, consumer);
    }
}
