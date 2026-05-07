package com.zfoo.hotswap.agent;

import com.zfoo.protocol.util.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;

/**
 * Using Instrumentation, developers can build an agent program independent of the application to monitor and assist programs running on the JVM,
 * and even replace or modify class definitions. This enables flexible runtime JVM monitoring and Java class manipulation,
 * effectively providing a JVM-level AOP mechanism without any JDK changes or upgrades.
 *
 * @author godotg
 */
public class HotSwapAgent {

    private static final String HOT_SWAP_MANAGER = "com.zfoo.hotswap.manager.HotSwapManager";

    private static final String UPDATE_BYTES_FIELD = "updateBytes";

    private static final String EXCEPTION = "exception";


    /*
     Since Java SE 6, agents can be attached dynamically after the JVM starts via the agentmain method. Like premain, it requires an agent JAR that satisfies:
     - Manifest must specify the Agent-Class attribute with the fully-qualified agent class name
     - The agent class must provide agentmain(String args, Instrumentation inst) or agentmain(String args); the former takes priority if both exist. args and inst are the same as in premain.
     However, this raises a question: how to attach an agent after the application has started? JDK6's Java Tools API provides the Attach API to solve this.
     VirtualMachine in the Attach API represents a running JVM and provides loadAgent() to dynamically load an agent JAR at runtime.
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
                // Class name
                String className = dis.readUTF();
                // Class bytecode
                byte[] classBytes = new byte[dis.readInt()];
                dis.readFully(classBytes);
                classDefs[i] = new ClassDefinition(Class.forName(className), classBytes);
            }
            // Start update
            inst.redefineClasses(classDefs);
            // Update successful
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
