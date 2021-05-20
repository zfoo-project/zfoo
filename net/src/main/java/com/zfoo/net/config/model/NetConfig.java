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
    private String protocolParam;
    private boolean generateJsProtocol;
    private boolean generateCsProtocol;
    private boolean generateLuaProtocol;

    private RegistryConfig registryConfig;
    private MonitorConfig monitorConfig;
    private HostConfig hostConfig;

    private ProviderConfig providerConfig;
    private ConsumerConfig consumerConfig;


    public RegisterVO toLocalRegisterVO() {
        return RegisterVO.valueOf(id, providerConfig, consumerConfig);
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

    public String getProtocolParam() {
        return protocolParam;
    }

    public void setProtocolParam(String protocolParam) {
        this.protocolParam = protocolParam;
    }

    public boolean isGenerateJsProtocol() {
        return generateJsProtocol;
    }

    public void setGenerateJsProtocol(boolean generateJsProtocol) {
        this.generateJsProtocol = generateJsProtocol;
    }

    public boolean isGenerateCsProtocol() {
        return generateCsProtocol;
    }

    public void setGenerateCsProtocol(boolean generateCsProtocol) {
        this.generateCsProtocol = generateCsProtocol;
    }

    public boolean isGenerateLuaProtocol() {
        return generateLuaProtocol;
    }

    public void setGenerateLuaProtocol(boolean generateLuaProtocol) {
        this.generateLuaProtocol = generateLuaProtocol;
    }

    public RegistryConfig getRegistryConfig() {
        return registryConfig;
    }

    public void setRegistryConfig(RegistryConfig registryConfig) {
        this.registryConfig = registryConfig;
    }

    public MonitorConfig getMonitorConfig() {
        return monitorConfig;
    }

    public void setMonitorConfig(MonitorConfig monitorConfig) {
        this.monitorConfig = monitorConfig;
    }

    public HostConfig getHostConfig() {
        return hostConfig;
    }

    public void setHostConfig(HostConfig hostConfig) {
        this.hostConfig = hostConfig;
    }

    public ProviderConfig getProviderConfig() {
        return providerConfig;
    }

    public void setProviderConfig(ProviderConfig providerConfig) {
        this.providerConfig = providerConfig;
    }

    public ConsumerConfig getConsumerConfig() {
        return consumerConfig;
    }

    public void setConsumerConfig(ConsumerConfig consumerConfig) {
        this.consumerConfig = consumerConfig;
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
        return generateJsProtocol == netConfig.generateJsProtocol &&
                Objects.equals(id, netConfig.id) &&
                Objects.equals(protocolLocation, netConfig.protocolLocation) &&
                Objects.equals(registryConfig, netConfig.registryConfig) &&
                Objects.equals(monitorConfig, netConfig.monitorConfig) &&
                Objects.equals(hostConfig, netConfig.hostConfig) &&
                Objects.equals(providerConfig, netConfig.providerConfig) &&
                Objects.equals(consumerConfig, netConfig.consumerConfig);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, protocolLocation, generateJsProtocol, registryConfig, monitorConfig, hostConfig, providerConfig, consumerConfig);
    }
}
