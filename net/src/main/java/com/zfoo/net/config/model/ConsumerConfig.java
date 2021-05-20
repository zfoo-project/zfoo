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


import com.zfoo.protocol.registration.ProtocolModule;

import java.util.List;
import java.util.Objects;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public class ConsumerConfig {

    private String loadBalancer;

    private List<ProtocolModule> modules;

    public static ConsumerConfig valueOf(String loadBalancer, List<ProtocolModule> modules) {
        ConsumerConfig config = new ConsumerConfig();
        config.loadBalancer = loadBalancer;
        config.modules = modules;
        return config;
    }

    public static ConsumerConfig valueOf(List<ProtocolModule> modules) {
        ConsumerConfig config = new ConsumerConfig();
        config.modules = modules;
        return config;
    }

    public String getLoadBalancer() {
        return loadBalancer;
    }

    public void setLoadBalancer(String loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    public List<ProtocolModule> getModules() {
        return modules;
    }

    public void setModules(List<ProtocolModule> modules) {
        this.modules = modules;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ConsumerConfig that = (ConsumerConfig) o;
        return Objects.equals(modules, that.modules);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modules);
    }
}
