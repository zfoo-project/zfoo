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

package com.zfoo.monitor;

import com.zfoo.protocol.util.IOUtils;

import java.lang.management.MemoryUsage;

/**
 * @author yh
 */
public class JvmMemory {
    private String name;
    private double total;
    private double max;
    private double used;

    public JvmMemory(String name, MemoryUsage usage) {
        this.name = name;
        this.total = usage.getCommitted() / (double) IOUtils.BYTES_PER_MB;
        this.max = usage.getMax() / (double) IOUtils.BYTES_PER_MB;
        this.used = usage.getUsed() / (double) IOUtils.BYTES_PER_MB;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double getUsed() {
        return used;
    }

    public void setUsed(double used) {
        this.used = used;
    }

    @Override
    public String toString() {
        return "JvmMemory{" +
                "name='" + name + '\'' +
                ", total=" + total +
                ", max=" + max +
                ", used=" + used +
                '}';
    }
}
