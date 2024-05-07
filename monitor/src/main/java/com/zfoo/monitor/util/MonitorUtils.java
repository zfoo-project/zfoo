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

package com.zfoo.monitor.util;

import com.zfoo.monitor.*;
import com.zfoo.net.util.NetUtils;
import com.zfoo.protocol.exception.RunException;
import com.zfoo.protocol.util.FileUtils;
import com.zfoo.protocol.util.IOUtils;
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.protocol.util.UuidUtils;
import com.zfoo.scheduler.util.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.software.os.OperatingSystem;

import java.io.File;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 监控工具类
 *
 * @author godotg
 */
public abstract class MonitorUtils {

    private static final Logger logger = LoggerFactory.getLogger(MonitorUtils.class);

    private static Uptime maxUptime;
    private static Map<String, DiskFileSystem> maxDfMap;
    private static Memory maxFree;
    private static Map<String, Sar> maxSarMap;

    static {
        initMonitor();
    }

    public static void initMonitor() {
        maxUptime = OSUtils.uptime();
        maxDfMap = new ConcurrentHashMap<>(OSUtils.df().stream().collect(Collectors.toMap(key -> key.getName(), value -> value)));
        maxFree = OSUtils.free();
        maxSarMap = new ConcurrentHashMap<>(OSUtils.sar().stream().collect(Collectors.toMap(key -> key.getName(), value -> value)));
    }

    public static Monitor maxMonitor() {
        var uuid = UuidUtils.getUUID();
        var monitor = Monitor.valueOf(uuid, maxUptime, new ArrayList<>(maxDfMap.values()), maxFree, new ArrayList<>(maxSarMap.values()));

        initMonitor();
        return monitor;
    }

    public static Monitor monitor() {
        var uuid = UuidUtils.getUUID();
        var uptime = OSUtils.uptime();
        var df = OSUtils.df();
        var free = OSUtils.free();
        var sar = OSUtils.sar();

        if (uptime.compareTo(maxUptime) > 0) {
            maxUptime = uptime;
        }

        for (var fileSystem : df) {
            var maxFileSystem = maxDfMap.get(fileSystem.getName());
            if (maxFileSystem != null && fileSystem.compareTo(maxFileSystem) > 0) {
                maxDfMap.put(fileSystem.getName(), fileSystem);
            }
        }

        if (free.compareTo(maxFree) > 0) {
            maxFree = free;
        }

        for (var networkIF : sar) {
            var maxNetworkIF = maxSarMap.get(networkIF.getName());
            if (maxNetworkIF != null && networkIF.compareTo(maxNetworkIF) > 0) {
                maxSarMap.put(maxNetworkIF.getName(), networkIF);
            }
        }

        return Monitor.valueOf(uuid, uptime, df, free, sar);
    }

}
