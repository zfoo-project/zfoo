package com.zfoo.monitor.util;

import com.zfoo.monitor.model.JvmMemoryVo;

import java.lang.management.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yh
 * @date 2022/10/10 下午6:42
 */
public class JvmUtils {


    public static List<JvmMemoryVo> getJvmInfo() {
        List<JvmMemoryVo> list = new ArrayList<>();

        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

        //堆内存
        MemoryUsage heapMemoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        list.add(new JvmMemoryVo("heap", heapMemoryUsage));

        List<MemoryPoolMXBean> memoryPoolMXBeans = ManagementFactory.getMemoryPoolMXBeans();
        for (MemoryPoolMXBean mxBean : memoryPoolMXBeans) {
            //获取堆内存明细
            if (MemoryType.HEAP.equals(mxBean.getType())) {
                MemoryUsage usage = mxBean.getUsage();
                list.add(new JvmMemoryVo(mxBean.getName(), usage));
            }
        }
        return list;
    }


}
