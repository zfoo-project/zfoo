package com.zfoo.monitor.model;

public class SystemInfoVO {

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

    public static SystemInfoVO valueOf(String hostname, String version, String versionDetail, int cpuLogicCore, String cpuName) {
        var vo = new SystemInfoVO();
        vo.hostname = hostname;
        vo.version = version;
        vo.versionDetail = versionDetail;
        vo.cpuLogicCore = cpuLogicCore;
        vo.cpuName = cpuName;
        return vo;
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
