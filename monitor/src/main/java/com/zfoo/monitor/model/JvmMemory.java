package com.zfoo.monitor.model;

import com.zfoo.protocol.util.IOUtils;

import java.lang.management.MemoryUsage;

/**
 * @author yh
 * @date 2022/10/10 下午6:44
 */

public class JvmMemory {
    private String name;
    private double total;
    private double max;
    private double used;

    public JvmMemory(String name, MemoryUsage usage) {
        this.name = name;
        this.total = usage.getCommitted() / (double) IOUtils.BYTES_PER_MB;
        this.max = usage.getMax() / (double) IOUtils.BYTES_PER_MB;
        this.used = usage.getUsed() / (double) IOUtils.BYTES_PER_MB;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double getUsed() {
        return used;
    }

    public void setUsed(double used) {
        this.used = used;
    }

    @Override
    public String toString() {
        return "JvmMemory{" +
                "name='" + name + '\'' +
                ", total=" + total +
                ", max=" + max +
                ", used=" + used +
                '}';
    }
}
