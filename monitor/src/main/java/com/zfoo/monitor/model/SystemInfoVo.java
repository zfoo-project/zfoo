package com.zfoo.monitor.model;

import com.baidu.bjf.remoting.protobuf.annotation.Protobuf;

import java.util.Date;

public class SystemInfoVo {






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
         * 内存使用率
         */
        private Double memPer;

        /**
         * core的个数(即核数)
         */
        private String cpuCoreNum;

        /**
         * cpu使用率
         */
        private Double cpuPer;

        /**
         * CPU型号信息
         */
        private String cpuXh;





        /**
         * 创建时间
         */
        private Date createTime;

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

    public Double getMemPer() {
        return memPer;
    }

    public void setMemPer(Double memPer) {
        this.memPer = memPer;
    }

    public String getCpuCoreNum() {
        return cpuCoreNum;
    }

    public void setCpuCoreNum(String cpuCoreNum) {
        this.cpuCoreNum = cpuCoreNum;
    }

    public Double getCpuPer() {
        return cpuPer;
    }

    public void setCpuPer(Double cpuPer) {
        this.cpuPer = cpuPer;
    }

    public String getCpuXh() {
        return cpuXh;
    }

    public void setCpuXh(String cpuXh) {
        this.cpuXh = cpuXh;
    }



    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
