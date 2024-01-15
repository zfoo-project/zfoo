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

import com.zfoo.protocol.util.StringUtils;

import java.util.Objects;

/**
 * @author godotg
 */
public class ProviderModule {

    /**
     * 模块id和模块名
     */
    private String protocolModule;

    /**
     * 提供者名字
     */
    private String provider;


    public ProviderModule() {
    }

    public ProviderModule(String protocolModule, String provider) {
        this.protocolModule = protocolModule;
        this.provider = provider;
    }

    public String getProtocolModule() {
        return protocolModule;
    }

    public void setProtocolModule(String protocolModule) {
        this.protocolModule = protocolModule;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProviderModule that = (ProviderModule) o;
        return Objects.equals(protocolModule, that.protocolModule) && Objects.equals(provider, that.provider);
    }

    @Override
    public int hashCode() {
        return Objects.hash(protocolModule, provider);
    }

    @Override
    public String toString() {
        return StringUtils.format("[{}-{}]", protocolModule, provider);
    }
}
