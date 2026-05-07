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

package com.zfoo.monitor;

import com.zfoo.monitor.util.JvmUtils;
import com.zfoo.monitor.util.MonitorUtils;
import com.zfoo.monitor.util.OSUtils;
import com.zfoo.protocol.util.JsonUtils;
import com.zfoo.protocol.util.ThreadUtils;
import org.junit.Assert;
import org.junit.Test;
import oshi.SystemInfo;

/**
 * @author godotg
 */
public class ApplicationTest {

    /**
     * Emulates Linux's 'uptime' command for monitoring CPU load; returns -1 on Windows
     */
    @Test
    public void uptimeTest() {
        var vo = OSUtils.uptime();
        System.out.println(JsonUtils.object2String(vo));
        System.out.println(vo.pressure());
    }

    /**
     * Emulates Linux's 'df' command for monitoring disk usage
     */
    @Test
    public void dfTest() {
        var df = OSUtils.df();
        df.forEach(it -> {
            System.out.println(JsonUtils.object2String(it.toGB()));
            System.out.println(it.pressure());
        });
    }

    /**
     * Emulates Linux's 'free' command for monitoring memory usage
     */
    @Test
    public void freeTest() {
        var free = OSUtils.free();
        System.out.println(JsonUtils.object2String(free.toGB()));
        System.out.println(free.pressure());
    }

    /**
     * Emulates Linux's 'sar' command for monitoring network I/O
     */
    @Test
    public void sarTest() {
        var sar = OSUtils.sar();
        sar.forEach(it -> {
            System.out.println(JsonUtils.object2String(it));
            System.out.println(it.pressure());
        });
    }

    /**
     * Test CPU tick size
     */
    @Test
    public void cpuTest() {
        var systemInfo = new SystemInfo();
        var hardware = systemInfo.getHardware();
        var os = systemInfo.getOperatingSystem();

        for (int i = 0; i < 5; i++) {
            var oldTicks = hardware.getProcessor().getSystemCpuLoadTicks();
            ThreadUtils.sleep(1000);
            var usage = hardware.getProcessor().getSystemCpuLoadBetweenTicks(oldTicks);
            System.out.println(usage);
        }
    }


    @Test
    public void toPercentTest() {
        var num = 0.123456D;
        var str = OSUtils.toPercent(num);
        Assert.assertEquals(str, "12.35%");
    }

    @Test
    public void monitorTest() {
        var monitor = MonitorUtils.monitor();
        System.out.println(monitor);
        ThreadUtils.sleep(1000);
        monitor = MonitorUtils.monitor();
        System.out.println(monitor);
        monitor = MonitorUtils.maxMonitor();
        System.out.println(monitor);
    }


    @Test
    public void JvmTest() {
        JvmUtils.getJvmInfo().forEach(a -> {
            System.out.println(a.toString());
        });
    }

    @Test
    public void osTest() {
        var os = OSUtils.os();
        System.out.println(JsonUtils.object2StringPrettyPrinter(os));
    }

}
