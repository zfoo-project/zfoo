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
         * core的个数(即核数)
         */
        private String cpuCoreNum;



        /**
         * CPU型号信息
         */
        private String cpuXh;







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



    public String getCpuCoreNum() {
        return cpuCoreNum;
    }

    public void setCpuCoreNum(String cpuCoreNum) {
        this.cpuCoreNum = cpuCoreNum;
    }



    public String getCpuXh() {
        return cpuXh;
    }

    public void setCpuXh(String cpuXh) {
        this.cpuXh = cpuXh;
    }




}
