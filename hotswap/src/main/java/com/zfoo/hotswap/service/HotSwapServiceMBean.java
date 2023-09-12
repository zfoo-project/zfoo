package com.zfoo.hotswap.service;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.zfoo.hotswap.HotSwapContext;
import com.zfoo.hotswap.manager.HotSwapManager;
import com.zfoo.hotswap.model.ClassFileDef;
import com.zfoo.protocol.util.FileUtils;
import com.zfoo.protocol.util.IOUtils;
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.scheduler.util.TimeUtils;
import javassist.bytecode.ClassFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServer;
import javax.management.MXBean;
import javax.management.ObjectName;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//java11过后ClassFile对外不可见
//import com.sun.tools.classfile.ClassFile;

/**
 * JMX（JAVA Management Extensions）技术是java5的新特性，它提供一种简单，标准的方式去管理应用程序，设备，服务等资源。
 * JMS定义了一些设计模式，api和一些服务来进行应用程序和网络的监控，这些都是基于java语言环境的。
 * 使用JMS技术，资源被一种叫做MBeans（Managed Beans）监控，这些MBean都在一个核心对象管理server上注册。
 * JMS给java开发者提供了自由的方式去监控java代码，创建智能java agents,实现分布式管理的中间件和管理者，并且能够快速整合这些方案到的管理和监控系统。
 * <p>
 * 根据JMX描述，MBean接口包括一些可读或者可写的属性，还有一些定义好的方法，这些方法能够被MBean管理应用程序调用。
 * 实现类，类名必须为接口sufixMBean的前缀。也就是Hello。如果不按这个命名注册MBean就会有问题。
 *
 * @author godotg
 */

@MXBean
public class HotSwapServiceMBean implements IHotSwapServiceMBean {

    private static final Logger logger = LoggerFactory.getLogger(HotSwapServiceMBean.class);

    // 热更新的代理和热更新的文件要放在同一个目录
    private static final String HOT_SWAP_SCRIPT = "hotscript";
    private static final String HOT_SWAP_AGENT = HOT_SWAP_SCRIPT + "/hotswap-2.0.jar";

    public static final String CLASS_SUFFIX = ".class";
    public static final String JAVA_SUFFIX = ".java";

    private static final HotSwapServiceMBean HOT_SWAP_SERVICE = new HotSwapServiceMBean();

    private HotSwapServiceMBean() {
        registerMBean();
    }

    // 使用jconsole去连接，当然也可以使用RMI进行远程连接MBean server，来进行管理和执行操作。
    private void registerMBean() {
        //注册监控
        try {
            //获取MBeanServer  如果没有MBean server存在那么下面会自动调用ManagementFactory.createMBeanServer()
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            // 包名加 类名 创建一个ObjectName
            ObjectName objectName = new ObjectName(this.getClass().getPackage().getName() + ":type=" + this.getClass().getSimpleName());
            // 在MBean server上注册MBean
            mbs.registerMBean(this, objectName);
        } catch (Exception e) {
            logger.error("MBean error", e);
        }
    }

    public static HotSwapServiceMBean getSingleInstance() {
        return HOT_SWAP_SERVICE;
    }

    @Override
    public void hotSwapByRelativePath(String relativePath) {
        hotSwapByAbsolutePath(FileUtils.getProAbsPath() + File.separator + relativePath);
    }

    @Override
    public synchronized void hotSwapByAbsolutePath(String absolutePath) {
        // 热更新class文件，上一步没有完成的操作本次继续执行
        try {
            hotSwapClass(absolutePath);
        } catch (Exception e) {
            logger.error("热更新class文件异常：[exception:{}]", e);
        }
    }

    @Override
    public void logAllUpdateClassFileInfo() {
        int count = 0;
        for (Map.Entry<String, ClassFileDef> entry : HotSwapManager.getInstance().getClassFileDefMap().entrySet()) {
            logger.info("[{}].更新的类名称:[{}]，更改的时间:[{}]", ++count, entry.getKey(), entry.getValue().getLastModifyTime());
        }
    }


    private void hotSwapClass(String absolutePath) throws Exception {
        long start = System.currentTimeMillis();

        ByteArrayOutputStream baos = null;
        DataOutputStream dos = null;
        try {
            // 本次需要更新的Class
            Map<String, ClassFileDef> updateClassMap = new HashMap<>();
            List<File> fileList = FileUtils.getAllReadableFiles(new File(absolutePath));
            for (File file : fileList) {
                if (!file.getName().endsWith(CLASS_SUFFIX)) {
                    continue;
                }
                String path = file.getAbsolutePath();
                long lastModifiedTime = file.lastModified();
                byte[] data = FileUtils.readFileToByteArray(file);
                String className = readClassName(data);
                ClassFileDef classFileDef = new ClassFileDef(className, path, lastModifiedTime, data);
                updateClassMap.put(classFileDef.getClassName(), classFileDef);
            }

            if (updateClassMap.isEmpty()) {
                logger.debug("本次更新没有如何文件");
                return;
            }

            // 验证本次更新的所有class文件是否合法
            Map<String, ClassFileDef> classFileDefMap = HotSwapContext.getHotSwapManager().getClassFileDefMap();
            for (Map.Entry<String, ClassFileDef> entry : updateClassMap.entrySet()) {// 读取所有本次需要热更新的字节码
                ClassFileDef classFileDef = entry.getValue();
                // 上一次热更新的文件
                ClassFileDef lastClassFileDef = classFileDefMap.get(classFileDef.getClassName());
                if (lastClassFileDef != null && !classFileDef.getClassName().equals(lastClassFileDef.getClassName())) {
                    logger.error("本次热更新失败，两次热更新文件的类名称不一致，转换失败:[{}]-->[{}]"
                            , classFileDef.getClassName(), lastClassFileDef.getClassName());
                    return;
                }
            }


            // 创建临时更新流
            baos = new ByteArrayOutputStream(1024);
            dos = new DataOutputStream(baos);
            // 先写需要热更新类的数量
            dos.writeInt(updateClassMap.size());
            for (ClassFileDef classFileDef : updateClassMap.values()) {
                dos.writeUTF(classFileDef.getClassName());// 写类名称
                dos.writeInt(classFileDef.getData().length);// 字节码的长度
                dos.write(classFileDef.getData());// 字节码
            }
            dos.flush();
            //设置状态，准备更新
            HotSwapManager.updateBytes = baos.toByteArray();
            HotSwapManager.exception = null;

            //更新
            loadHotSwapAgent();

            if (HotSwapManager.exception != null) {
                for (ClassFileDef classFileDef : updateClassMap.values()) {
                    logger.error("类[{}]热更新失败", classFileDef.getClassName());
                }
                logger.error("热更新失败原因：", HotSwapManager.exception);
                return;
            }

            for (ClassFileDef classFileDef : updateClassMap.values()) {
                logger.info("类[{}]热更新成功", classFileDef.getClassName());
            }
            long end = TimeUtils.currentTimeMillis();
            logger.info("本次热更新总耗时:[{}]毫秒", end - start);

            // 更新成功，保存更新记录
            classFileDefMap.putAll(updateClassMap);

            // 删除所有被更新过的java和class文件
            for (ClassFileDef def : updateClassMap.values()) {
                FileUtils.deleteFile(new File(def.getPath()));
            }
        } finally {
            IOUtils.closeIO(dos, baos);
        }
    }

    private void loadHotSwapAgent() throws Exception {
        VirtualMachine vm = null;
        try {
            // 获取运行当前这个类的jvm的pid
            String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
            // attach到这个pid建立通信管道，让jvm加载agent
            vm = VirtualMachine.attach(pid);
            vm.loadAgent(HOT_SWAP_AGENT);
        } catch (AgentInitializationException | AgentLoadException | AttachNotSupportedException | IOException e) {
            logger.error("热更新开启VirtualMachine失败", e);
            throw e;
        } finally {
            try {
                if (vm != null) {
                    vm.detach();
                }
            } catch (IOException e) {
                logger.error("热更新关闭vm失败", e);
            }
        }
    }


    private String readClassName(byte[] bytes) {
        ByteArrayInputStream byteArrayInputStream = null;
        DataInputStream dataInputStream = null;
        try {
            dataInputStream = new DataInputStream(new ByteArrayInputStream(bytes));
            var classFile = new ClassFile(dataInputStream);
            return classFile.getName().replaceAll(StringUtils.SLASH, StringUtils.PERIOD);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeIO(dataInputStream, byteArrayInputStream);
        }
    }
}
