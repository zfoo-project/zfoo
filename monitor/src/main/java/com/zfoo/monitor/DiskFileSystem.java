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
public class DiskFileSystem implements Comparable<DiskFileSystem> {

    private String name;

    private long size;

    private long available;

    private long timestamp;

    public static DiskFileSystem valueOf(String name, long size, long available, long timestamp) {
        var dfs = new DiskFileSystem();
        dfs.name = name;
        dfs.size = size;
        dfs.available = available;
        dfs.timestamp = timestamp;
        return dfs;
    }

    public String pressure() {
        var usage = usage();
        if (usage >= 0.8) {
            var temp = this.toGB();
            return StringUtils.format("df - 磁盘[name:{}]空间过高[size:{}GB][available:{}GB][usage:{}][{}]"
                    , name, temp.getSize(), temp.getAvailable(), OSUtils.toPercent(usage), TimeUtils.timeToString(timestamp));
        }
        return StringUtils.EMPTY;
    }

    public double usage() {
        return ((double) (size - available)) / size;
    }

    @Override
    public int compareTo(DiskFileSystem target) {
        if (target == null) {
            return 1;
        }

        return Double.compare(usage(), target.usage());
    }

    public DiskFileSystem toMB() {
        var size = this.size / IOUtils.BYTES_PER_MB;
        var available = this.available / IOUtils.BYTES_PER_MB;
        return DiskFileSystem.valueOf(this.name, size, available, timestamp);
    }

    public DiskFileSystem toGB() {
        var size = (long) Math.ceil(this.size / (double) IOUtils.BYTES_PER_GB);
        var available = (long) Math.ceil(this.available / (double) IOUtils.BYTES_PER_GB);
        return DiskFileSystem.valueOf(this.name, size, available, timestamp);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
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
