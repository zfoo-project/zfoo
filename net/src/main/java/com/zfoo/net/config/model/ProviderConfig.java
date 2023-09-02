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

import com.zfoo.net.core.HostAndPort;
import com.zfoo.net.util.NetUtils;
import com.zfoo.protocol.util.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * @author godotg
 * @version 3.0
 */
public class ProviderConfig {

    public static final int DEFAULT_PORT = 12400;

    private String thread;

    private String address;

    private List<ProviderModule> providers;

    public static ProviderConfig valueOf(String address, List<ProviderModule> modules) {
        ProviderConfig config = new ProviderConfig();
        config.address = address;
        config.providers = modules;
        return config;
    }

    public HostAndPort localHostAndPortOrDefault() {
        if (StringUtils.isBlank(address)) {
            var defaultHostAndPort = HostAndPort.valueOf(NetUtils.getLocalhostStr(), NetUtils.getAvailablePort(ProviderConfig.DEFAULT_PORT));
            this.address = defaultHostAndPort.toHostAndPortStr();
            return defaultHostAndPort;
        }
        return HostAndPort.valueOf(address);
    }

    public String getThread() {
        return thread;
    }

    public void setThread(String thread) {
        this.thread = thread;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<ProviderModule> getProviders() {
        return providers;
    }

    public void setProviders(List<ProviderModule> providers) {
        this.providers = providers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProviderConfig that = (ProviderConfig) o;
        return Objects.equals(address, that.address) && Objects.equals(providers, that.providers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, providers);
    }
}
