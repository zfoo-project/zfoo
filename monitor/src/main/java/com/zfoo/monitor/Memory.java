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

import com.zfoo.monitor.util.OSUtils;
import com.zfoo.protocol.util.IOUtils;
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.scheduler.util.TimeUtils;

/**
 * @author godotg
 */
public class Memory implements Comparable<Memory> {

    private long total;

    private long available;

    private long timestamp;

    public static Memory valueOf(long total, long available, long timestamp) {
        var memory = new Memory();
        memory.total = total;
        memory.available = available;
        memory.timestamp = timestamp;
        return memory;
    }

    public String pressure() {
        var usage = 1D * (total - available) / total;
        if (usage >= 0.8) {
            var temp = this.toGB();
            return StringUtils.format("free - 内存占用过高[total:{}GB][available:{}GB][usage:{}][{}]"
                    , temp.getTotal(), temp.getAvailable(), OSUtils.toPercent(usage), TimeUtils.timeToString(timestamp));
        }
        return StringUtils.EMPTY;
    }

    public double usage() {
        return ((double) (total - available)) / total;
    }

    @Override
    public int compareTo(Memory target) {
        if (target == null) {
            return 1;
        }

        return Double.compare(usage(), target.usage());
    }

    public Memory toMB() {
        var total = this.total / IOUtils.BYTES_PER_MB;
        var available = this.available / IOUtils.BYTES_PER_MB;
        return Memory.valueOf(total, available, timestamp);
    }

    public Memory toGB() {
        var total = (long) Math.ceil(this.total / (double) IOUtils.BYTES_PER_GB);
        var available = (long) Math.ceil(this.available / (double) IOUtils.BYTES_PER_GB);
        return Memory.valueOf(total, available, timestamp);
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getAvailable() {
        return available;
    }

    public void setAvailable(long available) {
        this.available = available;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
