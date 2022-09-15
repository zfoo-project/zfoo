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

package com.zfoo.net.config.model;

import com.zfoo.net.consumer.registry.RegisterVO;
import com.zfoo.protocol.generate.GenerateOperation;

import java.util.Objects;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public class NetConfig {
    private String id;
    private String protocolLocation;

    /**
     * 协议生成属性变量对应于{@link GenerateOperation}
     */
    private boolean foldProtocol;
    private String protocolPath;
    private String protocolParam;

    /**
     * 是否生成对应语言的协议
     */
    private boolean javascriptProtocol;
    private boolean typescriptProtocol;
    private boolean csharpProtocol;
    private boolean luaProtocol;
    private boolean gdscriptProtocol;
    private boolean cppProtocol;
    private boolean goProtocol;
    private boolean protobufProtocol;

    /**
     * 注册中心
     */
    private RegistryConfig registry;

    /**
     * 监控
     */
    private MonitorConfig monitor;

    /**
     * 生产者配置
     */
    private ProviderConfig provider;

    /**
     * 消费者配置
     */
    private ConsumerConfig consumer;


    public RegisterVO toLocalRegisterVO() {
        return RegisterVO.valueOf(id, provider, consumer);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProtocolLocation() {
        return protocolLocation;
    }

    public void setProtocolLocation(String protocolLocation) {
        this.protocolLocation = protocolLocation;
    }

    public boolean isFoldProtocol() {
        return foldProtocol;
    }

    public void setFoldProtocol(boolean foldProtocol) {
        this.foldProtocol = foldProtocol;
    }

    public String getProtocolPath() {
        return protocolPath;
    }

    public void setProtocolPath(String protocolPath) {
        this.protocolPath = protocolPath;
    }

    public String getProtocolParam() {
        return protocolParam;
    }

    public void setProtocolParam(String protocolParam) {
        this.protocolParam = protocolParam;
    }

    public boolean isJavascriptProtocol() {
        return javascriptProtocol;
    }

    public void setJavascriptProtocol(boolean javascriptProtocol) {
        this.javascriptProtocol = javascriptProtocol;
    }

    public boolean isCsharpProtocol() {
        return csharpProtocol;
    }

    public void setCsharpProtocol(boolean csharpProtocol) {
        this.csharpProtocol = csharpProtocol;
    }

    public boolean isLuaProtocol() {
        return luaProtocol;
    }

    public void setLuaProtocol(boolean luaProtocol) {
        this.luaProtocol = luaProtocol;
    }

    public RegistryConfig getRegistry() {
        return registry;
    }

    public void setRegistry(RegistryConfig registry) {
        this.registry = registry;
    }

    public MonitorConfig getMonitor() {
        return monitor;
    }

    public void setMonitor(MonitorConfig monitor) {
        this.monitor = monitor;
    }

    public ProviderConfig getProvider() {
        return provider;
    }

    public void setProvider(ProviderConfig provider) {
        this.provider = provider;
    }

    public ConsumerConfig getConsumer() {
        return consumer;
    }

    public void setConsumer(ConsumerConfig consumer) {
        this.consumer = consumer;
    }

    public boolean isGdscriptProtocol() {
        return gdscriptProtocol;
    }

    public void setGdscriptProtocol(boolean gdscriptProtocol) {
        this.gdscriptProtocol = gdscriptProtocol;
    }

    public boolean isProtobufProtocol() {
        return protobufProtocol;
    }

    public void setProtobufProtocol(boolean protobufProtocol) {
        this.protobufProtocol = protobufProtocol;
    }

    public boolean isCppProtocol() {
        return cppProtocol;
    }

    public void setCppProtocol(boolean cppProtocol) {
        this.cppProtocol = cppProtocol;
    }

    public boolean isTypescriptProtocol() {
        return typescriptProtocol;
    }

    public void setTypescriptProtocol(boolean typescriptProtocol) {
        this.typescriptProtocol = typescriptProtocol;
    }

    public boolean isGoProtocol() {
        return goProtocol;
    }

    public void setGoProtocol(boolean goProtocol) {
        this.goProtocol = goProtocol;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NetConfig netConfig = (NetConfig) o;
        return Objects.equals(id, netConfig.id) &&
                Objects.equals(protocolLocation, netConfig.protocolLocation) &&
                Objects.equals(registry, netConfig.registry) &&
                Objects.equals(monitor, netConfig.monitor) &&
                Objects.equals(provider, netConfig.provider) &&
                Objects.equals(consumer, netConfig.consumer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, protocolLocation, registry, monitor, provider, consumer);
    }
}
