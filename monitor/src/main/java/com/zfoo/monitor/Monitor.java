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
import com.zfoo.net.util.NetUtils;
import com.zfoo.protocol.collection.CollectionUtils;
import com.zfoo.protocol.util.FileUtils;
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.scheduler.util.TimeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author godotg
 */
public class Monitor {

    private String uuid;

    private Uptime uptime;

    private List<DiskFileSystem> df;

    private Memory free;

    private List<Sar> sar;

    public static Monitor valueOf(String uuid, Uptime uptime, List<DiskFileSystem> df, Memory free, List<Sar> sar) {
        var monitor = new Monitor();
        monitor.uuid = uuid;
        monitor.uptime = uptime;
        monitor.df = df;
        monitor.free = free;
        monitor.sar = sar;
        return monitor;
    }

    public List<String> toPressures() {
        var messages = new ArrayList<String>();

        var uptimeMessage = uptime.pressure();
        if (StringUtils.isNotBlank(uptimeMessage)) {
            messages.add(uptimeMessage);
        }

        for (var fileSystem : df) {
            var dfMessage = fileSystem.pressure();
            if (StringUtils.isNotBlank(dfMessage)) {
                messages.add(dfMessage);
            }
        }


        var freeMessage = free.pressure();
        if (StringUtils.isNotBlank(freeMessage)) {
            messages.add(freeMessage);
        }

        for (var networkIF : sar) {
            var sarMessage = networkIF.pressure();
            if (StringUtils.isNotBlank(sarMessage)) {
                messages.add(sarMessage);
            }
        }
        return messages;
    }

    @Override
    public String toString() {
        var builder = new StringBuilder();
        builder.append(StringUtils.format("Server Monitor [ip:{}] [uuid:{}]", NetUtils.getLocalhostStr(), uuid));
        builder.append(FileUtils.LS);
        builder.append(StringUtils.format("1.cpu: [{}] [{}] [{}] [usage:{}] [{}]"
                , uptime.getOneMinute(), uptime.getFiveMinute(), uptime.getFiftyMinute(), OSUtils.toPercent(uptime.getUsage()), TimeUtils.timeToString(uptime.getTimestamp())));
        builder.append(FileUtils.LS);
        builder.append(StringUtils.format("2.memory: [total:{}GB] [available:{}GB] [usage:{}] [{}]"
                , free.toGB().getTotal(), free.toGB().getAvailable(), OSUtils.toPercent(1D * (free.getTotal() - free.getAvailable()) / free.getTotal()), TimeUtils.timeToString(free.getTimestamp())));
        builder.append(FileUtils.LS);
        builder.append("3.disk: ");
        builder.append(FileUtils.LS);
        df.forEach(it -> {
            builder.append(StringUtils.format("    [disk:{}] [size:{}GB] [available:{}GB] [usage:{}] [{}]"
                    , it.getName(), it.toGB().getSize(), it.toGB().getAvailable(), OSUtils.toPercent(1D * (it.getSize() - it.getAvailable()) / it.getSize()), TimeUtils.timeToString(it.getTimestamp())));
            builder.append(FileUtils.LS);
        });
        builder.append("4.network:");
        builder.append(FileUtils.LS);
        sar.forEach(it -> {
            builder.append(StringUtils.format("    [interface:{}] [rxpck:{}] [txpck:{}] [rxBytes:{}] [txBytes:{}] [inErrors:{}] [outErrors:{}] [inDrops:{}] [collisions:{}] [{}]"
                    , it.getName(), it.getRxpck(), it.getTxpck(), it.getRxBytes(), it.getTxBytes(), it.getInErrors(), it.getOutErrors(), it.getInDrops(), it.getCollisions(), TimeUtils.timeToString(it.getTimestamp())));
            builder.append(FileUtils.LS);
        });
        var pressures = toPressures();
        if (CollectionUtils.isNotEmpty(pressures)) {
            builder.append("summary of errors:");
            builder.append(FileUtils.LS);
            pressures.forEach(it -> builder.append(StringUtils.format("    {}", it)).append(FileUtils.LS));
        }

        return builder.toString();
    }

    public String toSimpleString() {
        var builder = new StringBuilder();
        builder.append(StringUtils.format("**1.cpu: [usage:{}] [{}]**"
                , OSUtils.toPercent(uptime.getUsage()), TimeUtils.timeToString(uptime.getTimestamp())));
        builder.append(FileUtils.LS);
        builder.append(StringUtils.format("**2.memory: [usage:{}] [{}]**"
                , OSUtils.toPercent(1D * (free.getTotal() - free.getAvailable()) / free.getTotal()), TimeUtils.timeToString(free.getTimestamp())));
        builder.append(FileUtils.LS);
        builder.append("**3.disk:**");
        builder.append(FileUtils.LS);
        df.stream().forEach(it -> {
            builder.append(StringUtils.format("> [disk:{}] [usage:{}] [{}]"
                    , it.getName(), OSUtils.toPercent(1D * (it.getSize() - it.getAvailable()) / it.getSize()), TimeUtils.timeToString(it.getTimestamp())));
            builder.append(FileUtils.LS);
        });
        builder.append(FileUtils.LS);
        builder.append("**4.network:**");
        builder.append(FileUtils.LS);
        sar.stream().forEach(it -> {
            builder.append(StringUtils.format("> [interface:{}] [rxpck:{}] [txpck:{}] [{}]"
                    , it.getName(), it.getRxpck(), it.getTxpck(), TimeUtils.timeToString(it.getTimestamp())));
            builder.append(FileUtils.LS);
        });
        builder.append(FileUtils.LS);
        return builder.toString();
    }

    public String getUuid() {
        return uuid;
    }

    public Uptime getUptime() {
        return uptime;
    }

    public List<DiskFileSystem> getDf() {
        return df;
    }

    public Memory getFree() {
        return free;
    }

    public List<Sar> getSar() {
        return sar;
    }
}
