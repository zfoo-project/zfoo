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

import com.zfoo.protocol.util.StringUtils;
import com.zfoo.scheduler.util.TimeUtils;

/**
 * @author godotg
 * @version 3.0
 */
public class SarVO implements Comparable<SarVO> {

    private String name;

    private long rxpck;

    private long txpck;

    private long rxBytes;

    private long txBytes;

    private long inErrors;

    private long outErrors;

    private long inDrops;

    private long collisions;

    private long timestamp;

    public static SarVO valueOf(String name, long rxpck, long txpck, long rxBytes, long txBytes
            , long inErrors, long outErrors, long inDrops, long collisions, long timestamp) {
        var vo = new SarVO();
        vo.name = name;
        vo.rxpck = rxpck;
        vo.txpck = txpck;
        vo.rxBytes = rxBytes;
        vo.txBytes = txBytes;
        vo.inErrors = inErrors;
        vo.outErrors = outErrors;
        vo.inDrops = inDrops;
        vo.collisions = collisions;
        vo.timestamp = timestamp;
        return vo;
    }

    public String pressure() {
        if (rxpck >= 5_0000) {
            return StringUtils.format("sar - 网卡流量[interface:{}] [rxpck:{}] [txpck:{}] [rxBytes:{}] [txBytes:{}] [inErrors:{}] [outErrors:{}] [inDrops:{}] [collisions:{}] [{}]，性能影响：危险"
                    , name, rxpck, txpck, rxBytes, txBytes, inErrors, outErrors, inDrops, collisions, TimeUtils.timeToString(timestamp));
        }
        if (inErrors > 0 || outErrors > 0 || inDrops > 0 || collisions > 0) {
            return StringUtils.format("sar - 网卡流量[interface:{}] [rxpck:{}] [txpck:{}] [rxBytes:{}] [txBytes:{}] [inErrors:{}] [outErrors:{}] [inDrops:{}] [collisions:{}] [{}]，性能影响：低"
                    , name, rxpck, txpck, rxBytes, txBytes, inErrors, outErrors, inDrops, collisions, TimeUtils.timeToString(timestamp));
        }
        return StringUtils.EMPTY;
    }

    @Override
    public int compareTo(SarVO target) {
        if (target == null) {
            return 1;
        }
        if (!this.name.equals(target.getName())) {
            return 0;
        }

        var a = this.rxpck + this.txpck;
        var b = target.getRxpck() + target.getTxpck();
        return Long.compare(a, b);
    }

    @Override
    public String toString() {
        return StringUtils.format("[name:{}][rxpck:{}][txpck:{}][rxBytes:{}][txBytes:{}][inErrors:{}][outErrors:{}][inDrops:{}][collisions:{}][time:{}]"
                , name, rxpck, txpck, rxBytes, txBytes, inErrors, outErrors, inDrops, collisions, TimeUtils.timeToString(timestamp));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getRxpck() {
        return rxpck;
    }

    public void setRxpck(long rxpck) {
        this.rxpck = rxpck;
    }

    public long getTxpck() {
        return txpck;
    }

    public void setTxpck(long txpck) {
        this.txpck = txpck;
    }

    public long getRxBytes() {
        return rxBytes;
    }

    public void setRxBytes(long rxBytes) {
        this.rxBytes = rxBytes;
    }

    public long getTxBytes() {
        return txBytes;
    }

    public void setTxBytes(long txBytes) {
        this.txBytes = txBytes;
    }

    public long getInErrors() {
        return inErrors;
    }

    public void setInErrors(long inErrors) {
        this.inErrors = inErrors;
    }

    public long getOutErrors() {
        return outErrors;
    }

    public void setOutErrors(long outErrors) {
        this.outErrors = outErrors;
    }

    public long getInDrops() {
        return inDrops;
    }

    public void setInDrops(long inDrops) {
        this.inDrops = inDrops;
    }

    public long getCollisions() {
        return collisions;
    }

    public void setCollisions(long collisions) {
        this.collisions = collisions;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
