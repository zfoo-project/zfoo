package com.zfoo.hotswap.util;

import com.zfoo.protocol.collection.ArrayUtils;
import com.zfoo.protocol.util.IOUtils;
import com.zfoo.protocol.util.StringUtils;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.bytecode.ClassFile;
import javassist.util.HotSwapAgent;
import net.bytebuddy.agent.ByteBuddyAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.instrument.ClassDefinition;

/**
 * Hotswap java class
 * <p>
 * EN: Prefer using simple Javassist for hot updates, followed by Byte Buddy hot updates
 * Prefer Javassist for hot-swapping (simpler); Byte Buddy uses more complex ASM. Spring Boot web projects prefer Byte Buddy for hot-swapping
 *
 * @author godotg
 */
public abstract class HotSwapUtils {

    private static final Logger logger = LoggerFactory.getLogger(HotSwapUtils.class);

    /**
     * need to add JVM startup parameters: -Djdk.attach.allowAttachSelf=true
     * <p>
     * JVM startup parameter: since JDK 11, the JVM does not allow self-attachment by default. Add -Djdk.attach.allowAttachSelf=true to enable it
     * <p>
     * note: JAVA_HOME environment variable must be configured; unknown errors may occur without it
     * note: Using -cp or -Djava.ext.dirs may cause unknown hot-swap errors; recommend packaging with maven-shade-plugin or spring-boot-maven-plugin
     * note: Using -cp may occasionally cause java.lang.NoClassDefFoundError, especially in Lambda expressions and static Enum classes
     *
     * @param bytes bytecode from a .class file
     */
    public static synchronized void hotswapClass(byte[] bytes) {
        if (ArrayUtils.isEmpty(bytes)) {
            return;
        }

        var clazzName = readClassName(bytes);

        Class<?> clazz = null;
        try {
            clazz = Class.forName(clazzName);
        } catch (ClassNotFoundException e) {
            logger.error("The class:[{}] could not be found in the current project and ignore this hot update", clazzName);
            return;
        }

        hotswapClassByJavassist(clazz, bytes);
    }

    private static String readClassName(byte[] bytes) {
        ByteArrayInputStream byteArrayInputStream = null;
        DataInputStream dataInputStream = null;
        try {
            byteArrayInputStream = new ByteArrayInputStream(bytes);
            dataInputStream = new DataInputStream(byteArrayInputStream);
            var classFile = new ClassFile(dataInputStream);
            return classFile.getName().replaceAll(StringUtils.SLASH, StringUtils.PERIOD);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeIO(dataInputStream, byteArrayInputStream);
        }
    }

    private static void hotswapClassByJavassist(Class<?> clazz, byte[] bytes) {
        ByteArrayInputStream byteArrayInputStream = null;
        CtClass ctClass = null;
        try {
            byteArrayInputStream = new ByteArrayInputStream(bytes);
            ctClass = ClassPool.getDefault().makeClass(byteArrayInputStream);
            // Javassist hot update
            HotSwapAgent.redefine(clazz, ctClass);
            logger.info("Javassist hot update [{}] succeeded", clazz);
        } catch (Throwable t) {
            logger.info("Unable to use the Javassist hot update, start using ByteBuddy as hotswap", t);
            hotswapClassByByteBuddy(clazz, bytes);
        } finally {
            IOUtils.closeIO(byteArrayInputStream);
            if (ctClass != null) {
                ctClass.defrost();
                ctClass.detach();
            }
        }
    }

    private static void hotswapClassByByteBuddy(Class<?> clazz, byte[] bytes) {
        try {
            // Byte Buddy hot update
            var instrumentation = ByteBuddyAgent.install();
            instrumentation.redefineClasses(new ClassDefinition(clazz, bytes));
            logger.info("ByteBuddy hot update class:[{}] succeeded", clazz.getName());
        } catch (Throwable t) {
            logger.error("ByteBuddy hot update class:[{}] failed", clazz.getName());
        }
    }

}
