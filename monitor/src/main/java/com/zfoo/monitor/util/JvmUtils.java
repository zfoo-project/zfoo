package com.zfoo.monitor.util;

import com.sun.jdi.ThreadReference;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.spi.AttachProvider;
import com.zfoo.monitor.model.JvmMemoryVo;

import java.io.IOException;
import java.lang.management.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author yh
 * @date 2022/10/10 下午6:42
 */
public class JvmUtils {


    public static List<JvmMemoryVo> getJvmInfo(){
        List<JvmMemoryVo> list=new ArrayList<>();

        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

        //堆内存
        MemoryUsage heapMemoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        list.add(new JvmMemoryVo("heap",heapMemoryUsage));

        List<MemoryPoolMXBean> memoryPoolMXBeans = ManagementFactory.getMemoryPoolMXBeans();
        for (MemoryPoolMXBean mxBean : memoryPoolMXBeans) {
            //获取堆内存明细
            if (MemoryType.HEAP.equals(mxBean.getType())){
                MemoryUsage usage = mxBean.getUsage();
                list.add(new JvmMemoryVo(mxBean.getName(), usage));
            }
        }
        return list;
    }


}
