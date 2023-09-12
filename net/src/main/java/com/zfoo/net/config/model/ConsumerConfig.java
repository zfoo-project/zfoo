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


import java.util.List;
import java.util.Objects;

/**
 * @author godotg
 */
public class ConsumerConfig {

    private List<ConsumerModule> consumers;

    public static ConsumerConfig valueOf(List<ConsumerModule> modules) {
        ConsumerConfig config = new ConsumerConfig();
        config.consumers = modules;
        return config;
    }

    public List<ConsumerModule> getConsumers() {
        return consumers;
    }

    public void setConsumers(List<ConsumerModule> consumers) {
        this.consumers = consumers;
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
        return Objects.equals(consumers, that.consumers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(consumers);
    }
}
