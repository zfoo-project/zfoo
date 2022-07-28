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

package com.zfoo.monitor.model;

import com.zfoo.monitor.util.OSUtils;
import com.zfoo.protocol.util.IOUtils;
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.scheduler.util.TimeUtils;

/**
 * @author godotg
 * @version 3.0
 */
public class DiskFileSystemVO implements Comparable<DiskFileSystemVO> {

    private String name;

    private long size;

    private long available;

    private long timestamp;

    public static DiskFileSystemVO valueOf(String name, long size, long available, long timestamp) {
        var vo = new DiskFileSystemVO();
        vo.name = name;
        vo.size = size;
        vo.available = available;
        vo.timestamp = timestamp;
        return vo;
    }

    public String pressure() {
        var usage = 1D * (size - available) / size;
        if (usage >= 0.8) {
            var tempVO = this.toGB();
            return StringUtils.format("df - 磁盘[name:{}]空间过高[size:{}GB][available:{}GB][usage:{}][{}]"
                    , name, tempVO.getSize(), tempVO.getAvailable(), OSUtils.toPercent(usage), TimeUtils.timeToString(timestamp));
        }
        return StringUtils.EMPTY;
    }

    @Override
    public int compareTo(DiskFileSystemVO target) {
        if (target == null) {
            return 1;
        }
        if (!this.name.equals(target.getName())) {
            return 0;
        }

        var a = 1D * (this.size - this.available) / this.size;
        var b = 1D * (target.getSize() - target.getAvailable()) / target.getSize();
        return Double.compare(a, b);
    }

    public DiskFileSystemVO toMB() {
        var size = this.size / IOUtils.BYTES_PER_MB;
        var available = this.available / IOUtils.BYTES_PER_MB;
        return DiskFileSystemVO.valueOf(this.name, size, available, timestamp);
    }

    public DiskFileSystemVO toGB() {
        var size = (long) Math.ceil(1D * this.size / IOUtils.BYTES_PER_GB);
        var available = (long) Math.ceil(1D * this.available / IOUtils.BYTES_PER_GB);
        return DiskFileSystemVO.valueOf(this.name, size, available, timestamp);
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
