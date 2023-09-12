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
 * @author godotg
 */
public abstract class HotSwapUtils {

    private static final Logger logger = LoggerFactory.getLogger(HotSwapUtils.class);

    /**
     * 热更新java文件
     * JVM的启动参数，jdk11过后默认JVM不允许连接自己，需要加上JVM启动参数: -Djdk.attach.allowAttachSelf=true
     * <p>
     * 优先使用简单的Javassist做热更新，因为Byte Buddy使用了更为复杂的ASM，spring boot web项目中会优先使用Byte Buddy热更新
     *
     * @param bytes .class结尾的字节码文件
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
            logger.info("ByteBuddy hot update [{}] succeeded", clazz);
        } catch (Throwable t) {
            logger.error("ByteBuddy hot update [{}] failed", clazz);
        }
    }

}
