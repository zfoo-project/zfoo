package com.zfoo.monitor.util;

import com.zfoo.monitor.JvmMemory;

import java.lang.management.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yh
 */
public class JvmUtils {


    public static List<JvmMemory> getJvmInfo() {
        List<JvmMemory> list = new ArrayList<>();

        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

        // Heap memory
        MemoryUsage heapMemoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        list.add(new JvmMemory("heap", heapMemoryUsage));

        List<MemoryPoolMXBean> memoryPoolMXBeans = ManagementFactory.getMemoryPoolMXBeans();
        for (MemoryPoolMXBean mxBean : memoryPoolMXBeans) {
            // Get heap memory details
            if (MemoryType.HEAP.equals(mxBean.getType())) {
                MemoryUsage usage = mxBean.getUsage();
                list.add(new JvmMemory(mxBean.getName(), usage));
            }
        }
        return list;
    }


}
