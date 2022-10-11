package com.zfoo.monitor.model;

import java.lang.management.MemoryUsage;

/**
 * @author yh
 * @date 2022/10/10 下午6:44
 */

public class JvmMemoryVo {
    private String name;
    private double total;
    private double max;
    private double used;

    @Override
    public String toString() {
        return "JvmMemoryVo{" +
                "name='" + name + '\'' +
                ", total=" + total +
                ", max=" + max +
                ", used=" + used +
                '}';
    }

    public JvmMemoryVo(String name, MemoryUsage usage) {
        this.name=name;
        this.total=bitTomb(usage.getCommitted());
        this.max=bitTomb(usage.getMax());
        this.used=bitTomb(usage.getUsed());

    }
    private static double bitTomb(long val){
        return (double) (val/1024/1024);
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
}
