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
public class MemoryVO implements Comparable<MemoryVO> {

    private long total;

    private long available;

    private long timestamp;

    public static MemoryVO valueOf(long total, long available, long timestamp) {
        var vo = new MemoryVO();
        vo.total = total;
        vo.available = available;
        vo.timestamp = timestamp;
        return vo;
    }

    public String pressure() {
        var usage = 1D * (total - available) / total;
        if (usage >= 0.8) {
            var tempVO = this.toGB();
            return StringUtils.format("free - 内存占用过高[total:{}GB][available:{}GB][usage:{}][{}]"
                    , tempVO.getTotal(), tempVO.getAvailable(), OSUtils.toPercent(usage), TimeUtils.timeToString(timestamp));
        }
        return StringUtils.EMPTY;
    }

    @Override
    public int compareTo(MemoryVO target) {
        if (target == null) {
            return 1;
        }

        var a = 1D * (this.total - this.available) / this.total;
        var b = 1D * (target.getTotal() - target.getAvailable()) / target.getTotal();
        return Double.compare(a, b);
    }

    public MemoryVO toMB() {
        var total = this.total / IOUtils.BYTES_PER_MB;
        var available = this.available / IOUtils.BYTES_PER_MB;
        return MemoryVO.valueOf(total, available, timestamp);
    }

    public MemoryVO toGB() {
        var total = (long) Math.ceil(1D * this.total / IOUtils.BYTES_PER_GB);
        var available = (long) Math.ceil(1D * this.available / IOUtils.BYTES_PER_GB);
        return MemoryVO.valueOf(total, available, timestamp);
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
