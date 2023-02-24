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

public class SystemInfo {

    /**
     * host名称
     */
    private String hostname;

    /**
     * 系统版本信息
     */
    private String version;

    /**
     * 系统版本详细信息
     */
    private String versionDetail;

    /**
     * core的逻辑核心个数
     */
    private int cpuLogicCore;

    /**
     * CPU型号信息
     */
    private String cpuName;

    public static SystemInfo valueOf(String hostname, String version, String versionDetail, int cpuLogicCore, String cpuName) {
        var systemInfo = new SystemInfo();
        systemInfo.hostname = hostname;
        systemInfo.version = version;
        systemInfo.versionDetail = versionDetail;
        systemInfo.cpuLogicCore = cpuLogicCore;
        systemInfo.cpuName = cpuName;
        return systemInfo;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersionDetail() {
        return versionDetail;
    }

    public void setVersionDetail(String versionDetail) {
        this.versionDetail = versionDetail;
    }

    public int getCpuLogicCore() {
        return cpuLogicCore;
    }

    public void setCpuLogicCore(int cpuLogicCore) {
        this.cpuLogicCore = cpuLogicCore;
    }

    public String getCpuName() {
        return cpuName;
    }

    public void setCpuName(String cpuName) {
        this.cpuName = cpuName;
    }
}
