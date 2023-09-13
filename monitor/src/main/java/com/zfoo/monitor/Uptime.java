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
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.scheduler.util.TimeUtils;

/**
 * @author godotg
 */
public class Uptime implements Comparable<Uptime> {

    private double oneMinute;

    private double fiveMinute;

    private double fiftyMinute;

    private double usage;

    private long timestamp;

    public static Uptime valueOf(double oneMinute, double fiveMinute, double fiftyMinute, double usage, long timestamp) {
        var uptime = new Uptime();
        uptime.oneMinute = oneMinute;
        uptime.fiveMinute = fiveMinute;
        uptime.fiftyMinute = fiftyMinute;
        uptime.usage = usage;
        uptime.timestamp = timestamp;
        return uptime;
    }

    public String pressure() {
        var processors = OSUtils.availableProcessors();
        var one = oneMinute / processors;
        var five = fiveMinute / processors;
        var fifty = fiftyMinute / processors;

        if (usage >= 0.8) {
            return StringUtils.format("uptime - cpu负载[{}][{}][{}][usage:{}][{}]过大，性能影响：危险"
                    , oneMinute, fiveMinute, fiftyMinute, OSUtils.toPercent(usage), TimeUtils.timeToString(timestamp));
        }

        if (one > 5 || five > 5 || fifty > 5) {
            return StringUtils.format("uptime - cpu负载[{}][{}][{}][usage:{}][{}]过大，性能影响：警告"
                    , oneMinute, fiveMinute, fiftyMinute, OSUtils.toPercent(usage), TimeUtils.timeToString(timestamp));
        }

        if (one > 4 || five > 4 || fifty > 4) {
            return StringUtils.format("uptime - cpu负载[{}][{}][{}][usage:{}][{}]，性能影响：高"
                    , oneMinute, fiveMinute, fiftyMinute, OSUtils.toPercent(usage), TimeUtils.timeToString(timestamp));
        }

        if (one > 3 || five > 3 || fifty > 3) {
            return StringUtils.format("uptime - cpu负载[{}][{}][{}][usage:{}][{}]，性能影响：中，考虑优化"
                    , oneMinute, fiveMinute, fiftyMinute, OSUtils.toPercent(usage), TimeUtils.timeToString(timestamp));
        }


        return StringUtils.EMPTY;
    }

    @Override
    public int compareTo(Uptime target) {
        if (target == null) {
            return 1;
        }

        return Double.compare(this.usage, target.getUsage());
    }

    public double getOneMinute() {
        return oneMinute;
    }

    public double getFiveMinute() {
        return fiveMinute;
    }

    public double getFiftyMinute() {
        return fiftyMinute;
    }

    public double getUsage() {
        return usage;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
