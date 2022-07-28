package com.zfoo.hotswap.agent;

import com.zfoo.protocol.util.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;

/**
 * 使用 Instrumentation，开发者可以构建一个独立于应用程序的代理程序（Agent），用来监测和协助运行在 JVM 上的程序，
 * 甚至能够替换和修改某些类的定义。有了这样的功能，开发者就可以实现更为灵活的运行时虚拟机监控和 Java 类操作了，
 * 这样的特性实际上提供了一种虚拟机级别支持的 AOP 实现方式，使得开发者无需对 JDK 做任何升级和改动，就可以实现某些 AOP 的功能了。
 *
 * @author godotg
 */
public class HotSwapAgent {

    private static final String HOT_SWAP_MANAGER = "com.zfoo.hotswap.manager.HotSwapManager";

    private static final String UPDATE_BYTES_FIELD = "updateBytes";

    private static final String EXCEPTION = "exception";


    /*
     Java SE6开始，提供了在应用程序的VM启动后在动态添加代理的方式，即agentmain方式。 与Permain类似，agent方式同样需要提供一个agent jar，并且这个jar需要满足：
     在manifest中指定Agent-Class属性，值为代理类全路径
     代理类需要提供public static void agentmain(String args, Instrumentation inst)或public static void agentmain(String args)方法。并且再二者同时存在时以前者优先。args和inst和premain中的一致。
     不过如此设计的再运行时进行代理有个问题——如何在应用程序启动之后再开启代理程序呢？ JDK6中提供了Java Tools API，其中Attach API可以满足这个需求。
     Attach API中的VirtualMachine代表一个运行中的VM。其提供了loadAgent()方法，可以在运行时动态加载一个代理jar。
     */
    public static void agentmain(String args, Instrumentation inst) {
        Class<?> clazz = null;
        DataInputStream dis = null;
        try {
            clazz = Class.forName(HOT_SWAP_MANAGER);
            byte[] byteArray = (byte[]) clazz.getField(UPDATE_BYTES_FIELD).get(null);
            dis = new DataInputStream(new ByteArrayInputStream(byteArray));
            int len = dis.readInt();
            ClassDefinition[] classDefs = new ClassDefinition[len];
            for (int i = 0; i < len; i++) {
                //类名
                String className = dis.readUTF();
                //类字节码
                byte[] classBytes = new byte[dis.readInt()];
                dis.readFully(classBytes);
                classDefs[i] = new ClassDefinition(Class.forName(className), classBytes);
            }
            //开始更新
            inst.redefineClasses(classDefs);
            //更新成功
        } catch (Exception e) {
            if (clazz != null) {
                try {
                    clazz.getField(EXCEPTION).set(null, e);
                } catch (Exception e1) {
                    throw new RuntimeException(e1);
                }
            }
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeIO(dis);
        }
    }

}
