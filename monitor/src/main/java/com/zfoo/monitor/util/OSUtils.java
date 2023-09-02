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

import java.io.InputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Oshi库封装的工具类，通过此工具类，可获取系统、硬件相关信息
 *
 * @author godotg
 * @version 3.0
 */
public abstract class OSUtils {

    private static final Logger logger = LoggerFactory.getLogger(OSUtils.class);

    /**
     * cpu的数量
     */
    private static final int processors = Runtime.getRuntime().availableProcessors();

    /**
     * 系统信息
     */
    private static final oshi.SystemInfo systemInfo = new oshi.SystemInfo();

    /**
     * 硬件信息
     */
    private static final HardwareAbstractionLayer hardware = systemInfo.getHardware();

    /**
     * 操作系统信息
     */
    private static final OperatingSystem os = systemInfo.getOperatingSystem();

    /**
     * 磁盘io
     */
    private static final List<HWDiskStore> hwDiskStores = hardware.getDiskStores();

    /**
     * 网络信息
     */
    private static final List<NetworkIF> networkIFs = hardware.getNetworkIFs();

    /**
     * cpu的tick次数信息
     */
    private static long[] ticks = hardware.getProcessor().getSystemCpuLoadTicks();

    public static int availableProcessors() {
        return processors;
    }

    /**
     * 将小于0的num转为百分比，舍弃的部分将会做四舍五入
     */
    public static String toPercent(double num) {
        if (num > 1) {
            throw new RuntimeException("转为百分比的num必须小于1");
        }
        var percentFormat = NumberFormat.getPercentInstance();
        // 最大小数位数
        percentFormat.setMaximumFractionDigits(2);
        // 最大整数位数
        percentFormat.setMaximumIntegerDigits(3);
        // 最小小数位数
        percentFormat.setMinimumFractionDigits(2);
        // 自动转换成百分比显示
        return percentFormat.format(num);
    }

    /**
     * 对应于Linux中的uptime命令，windows中无法统计，所以在windows返回的结果默认是-1
     */
    public static Uptime uptime() {
        var processor = hardware.getProcessor();
        var loads = processor.getSystemLoadAverage(3);
        var oneMinute = loads[0];
        var fiveMinute = loads[1];
        var fiftyMinute = loads[2];

        var cpuTicks = processor.getSystemCpuLoadTicks();
        var usage = processor.getSystemCpuLoadBetweenTicks(ticks);

        ticks = cpuTicks;
        return Uptime.valueOf(oneMinute, fiveMinute, fiftyMinute, usage, TimeUtils.now());
    }

    /**
     * 对应于Linux中的df -h命令，兼容windows
     */
    public static List<DiskFileSystem> df() {
        var fileSystems = os.getFileSystem().getFileStores();

        var nameDfMap = new HashMap<String, List<DiskFileSystem>>();
        for (var fs : fileSystems) {
            var name = fs.getName();
            var size = fs.getTotalSpace();
            var available = fs.getFreeSpace();
            var list = nameDfMap.computeIfAbsent(name, (it) -> new ArrayList<>());
            list.add(DiskFileSystem.valueOf(name, size, available, TimeUtils.now()));
        }

        var dfs = new ArrayList<DiskFileSystem>();
        for (var dfList : nameDfMap.values()) {
            if (dfList.size() == 1) {
                dfs.add(dfList.get(0));
            } else {
                for (int i = 0; i < dfList.size(); i++) {
                    var df = dfList.get(i);
                    var name = df.getName();
                    var index = i + 1;
                    df.setName(StringUtils.format("{}-{}", name, index));
                    dfs.add(df);
                }
            }
        }

        return dfs;
    }

    /**
     * 对应于Linux中的free命令，兼容windows
     */
    public static Memory free() {
        var memory = hardware.getMemory();
        var total = memory.getTotal();
        var available = memory.getAvailable();
        return Memory.valueOf(total, available, TimeUtils.now());
    }


    public static List<DiskStorage> iostat() {
        var diskStorages = new ArrayList<DiskStorage>();

        for (var ds : hwDiskStores) {
            var name = ds.getName();
            var oldTimestamp = ds.getTimeStamp();
            var oldReads = ds.getReads();
            var oldReadBytes = ds.getReadBytes();
            var oldWrites = ds.getWrites();
            var oldWriteBytes = ds.getWriteBytes();

            if (!ds.updateAttributes()) {
                throw new RuntimeException(StringUtils.format("iostat update exception [ds:{}]", ds));
            }

            var timestamp = ds.getTimeStamp();
            var timeInterval = (timestamp - oldTimestamp) / 1000D;

            var reads = (long) Math.ceil(((ds.getReads() - oldReads) / timeInterval));
            var readKBs = (long) Math.ceil(((ds.getReadBytes() - oldReadBytes) / timeInterval / IOUtils.BYTES_PER_KB));
            var writes = (long) Math.ceil(((ds.getWrites() - oldWrites) / timeInterval));
            var writeKBs = (long) Math.ceil(((ds.getWriteBytes() - oldWriteBytes) / timeInterval / IOUtils.BYTES_PER_KB));

            var diskStorage = DiskStorage.valueOf(name, reads, readKBs, writes, writeKBs);
            diskStorages.add(diskStorage);
        }

        return diskStorages;
    }

    /**
     * 对应于Linux中的sar -n DEV 1命令，兼容windows
     */
    public static List<Sar> sar() {
        var nameSarMap = new HashMap<String, List<Sar>>();
        for (var networkIF : networkIFs) {
            var name = networkIF.getDisplayName() + StringUtils.SPACE + networkIF.getName();
            var oldTimestamp = networkIF.getTimeStamp();
            var oldBytesRecv = networkIF.getBytesRecv();
            var oldBytesSent = networkIF.getBytesSent();
            var oldPacketsRecv = networkIF.getPacketsRecv();
            var oldPacketsSent = networkIF.getPacketsSent();
            var oldInErrors = networkIF.getInErrors();
            var oldOutErrors = networkIF.getOutErrors();
            var oldInDrops = networkIF.getInDrops();
            var oldCollisions = networkIF.getCollisions();

            if (!networkIF.updateAttributes()) {
                throw new RuntimeException(StringUtils.format("sar update exception [networkIF:{}]", networkIF));
            }

            var timestamp = networkIF.getTimeStamp();
            var timeInterval = (timestamp - oldTimestamp) / 1000D;
            var rxpck = (long) Math.ceil(((networkIF.getPacketsRecv() - oldPacketsRecv) / timeInterval));
            var txpck = (long) Math.ceil((networkIF.getPacketsSent() - oldPacketsSent) / timeInterval);
            var rxBytes = (long) Math.ceil((networkIF.getBytesRecv() - oldBytesRecv) / timeInterval);
            var txBytes = (long) Math.ceil((networkIF.getBytesSent() - oldBytesSent) / timeInterval);
            var inErrors = networkIF.getInErrors() - oldInErrors;
            var outErrors = networkIF.getOutErrors() - oldOutErrors;
            var inDrops = networkIF.getInDrops() - oldInDrops;
            var collisions = networkIF.getCollisions() - oldCollisions;

            var list = nameSarMap.computeIfAbsent(name, (it) -> new ArrayList<>());
            list.add(Sar.valueOf(name, rxpck, txpck, rxBytes, txBytes, inErrors, outErrors, inDrops, collisions, timestamp));
        }

        var sars = new ArrayList<Sar>();
        for (var sarList : nameSarMap.values()) {
            if (sarList.size() == 1) {
                sars.add(sarList.get(0));
            } else {
                for (int i = 0; i < sarList.size(); i++) {
                    var sar = sarList.get(i);
                    var name = sar.getName();
                    var index = i + 1;
                    sar.setName(StringUtils.format("{}-{}", name, index));
                    sars.add(sar);
                }
            }
        }

        return sars;
    }

    private static Uptime maxUptime;
    private static Map<String, DiskFileSystem> maxDfMap;
    private static Memory maxFree;
    private static Map<String, Sar> maxSarMap;

    static {
        initMonitor();
    }

    public static void initMonitor() {
        maxUptime = uptime();
        maxDfMap = new ConcurrentHashMap<>(df().stream().collect(Collectors.toMap(key -> key.getName(), value -> value)));
        maxFree = free();
        maxSarMap = new ConcurrentHashMap<>(sar().stream().collect(Collectors.toMap(key -> key.getName(), value -> value)));
    }

    public static Monitor maxMonitor() {
        var uuid = UuidUtils.getUUID();
        var monitor = Monitor.valueOf(uuid, maxUptime, new ArrayList<>(maxDfMap.values()), maxFree, new ArrayList<>(maxSarMap.values()));

        initMonitor();
        return monitor;
    }

    public static Monitor monitor() {
        var uuid = UuidUtils.getUUID();
        var uptime = uptime();
        var df = df();
        var free = free();
        var sar = sar();

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

    public static String execCommand(String command) {
        Process process = null;
        InputStream inputStream = null;
        try {
            process = new ProcessBuilder(command.split(" "))
                    .redirectErrorStream(true)
                    .start();

            //取得命令结果的输出流
            inputStream = process.getInputStream();
            var bytes = IOUtils.toByteArray(inputStream);
            var result = StringUtils.bytesToString(bytes);

            // 其他线程都等待这个线程完成
            process.waitFor();
            // 获取javac线程的退出值，0代表正常退出，非0代表异常中止
            int exitValue = process.exitValue();

            // 返回编译是否成功
            if (exitValue != 0) {
                throw new Exception("执行命令错误，返回码：" + exitValue);
            }

            return result;
        } catch (Exception e) {
            logger.error("命令执行未知异常", e);
        } finally {
            if (process != null) {
                process.destroy();
            }
            IOUtils.closeIO(inputStream);
        }

        return StringUtils.EMPTY;
    }

    public static SystemInfo os() {
        var processor = hardware.getProcessor();
        var cpuLogicCore = processor.getLogicalProcessorCount();
        var cpuName = processor.getProcessorIdentifier().getName();
        return SystemInfo.valueOf(NetUtils.getLocalhostStr(), os.toString(), os.toString(), cpuLogicCore, cpuName);
    }
}
