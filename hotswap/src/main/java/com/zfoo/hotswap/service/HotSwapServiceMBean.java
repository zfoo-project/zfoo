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

// After Java 11, ClassFile is no longer publicly visible
//import com.sun.tools.classfile.ClassFile;

/**
 * JMX (Java Management Extensions) is a Java 5 feature providing a simple, standard way to manage applications, devices, and services.
 * JMX defines design patterns, APIs, and services for monitoring applications and networks within the Java environment.
 * With JMX, resources are managed by MBeans (Managed Beans) registered with a central MBean server.
 * JMX gives Java developers a flexible way to monitor Java code, create intelligent agents, build distributed management middleware, and integrate into management/monitoring systems.
 * <p>
 * Per the JMX spec, an MBean interface exposes readable/writable attributes and defined methods callable by management applications.
 * Implementation class name must be the interface name without the 'MBean' suffix (e.g., 'Hello' for 'HelloMBean'). Non-compliance causes registration errors.
 *
 * @author godotg
 */

@MXBean
public class HotSwapServiceMBean implements IHotSwapServiceMBean {

    private static final Logger logger = LoggerFactory.getLogger(HotSwapServiceMBean.class);

    // The hot-swap agent and hot-swap files must be in the same directory
    private static final String HOT_SWAP_SCRIPT = "hotscript";
    private static final String HOT_SWAP_AGENT = HOT_SWAP_SCRIPT + "/hotswap-2.0.jar";

    public static final String CLASS_SUFFIX = ".class";
    public static final String JAVA_SUFFIX = ".java";

    private static final HotSwapServiceMBean HOT_SWAP_SERVICE = new HotSwapServiceMBean();

    private HotSwapServiceMBean() {
        registerMBean();
    }

    // Connect via jconsole or remotely via RMI to the MBean server for management
    private void registerMBean() {
        // Register monitoring
        try {
            // Get MBeanServer; if none exists, ManagementFactory.createMBeanServer() is called automatically
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            // Create ObjectName from package + class name
            ObjectName objectName = new ObjectName(this.getClass().getPackage().getName() + ":type=" + this.getClass().getSimpleName());
            // Register MBean with the MBean server
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
        // Hot-swap class files; continue any operations not completed in the previous pass
        try {
            hotSwapClass(absolutePath);
        } catch (Exception e) {
            logger.error("Hot-swap class file error: [exception:{}]", e);
        }
    }

    @Override
    public void logAllUpdateClassFileInfo() {
        int count = 0;
        for (Map.Entry<String, ClassFileDef> entry : HotSwapManager.getInstance().getClassFileDefMap().entrySet()) {
            logger.info("[{}]. Updated class: [{}], modified time: [{}]", ++count, entry.getKey(), entry.getValue().getLastModifyTime());
        }
    }


    private void hotSwapClass(String absolutePath) throws Exception {
        long start = System.currentTimeMillis();

        ByteArrayOutputStream baos = null;
        DataOutputStream dos = null;
        try {
            // Classes to update in this pass
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
                logger.debug("No files to update in this pass");
                return;
            }

            // Validate all class files to be updated in this pass
            Map<String, ClassFileDef> classFileDefMap = HotSwapContext.getHotSwapManager().getClassFileDefMap();
            for (Map.Entry<String, ClassFileDef> entry : updateClassMap.entrySet()) {// Read all bytecode to be hot-swapped in this pass
                ClassFileDef classFileDef = entry.getValue();
                // Files from the previous hot-swap
                ClassFileDef lastClassFileDef = classFileDefMap.get(classFileDef.getClassName());
                if (lastClassFileDef != null && !classFileDef.getClassName().equals(lastClassFileDef.getClassName())) {
                    logger.error("Hot-swap failed: class names inconsistent between passes [{}]-->[{}]"
                            , classFileDef.getClassName(), lastClassFileDef.getClassName());
                    return;
                }
            }


            // Create temporary update stream
            baos = new ByteArrayOutputStream(1024);
            dos = new DataOutputStream(baos);
            // Write the number of classes to hot-swap first
            dos.writeInt(updateClassMap.size());
            for (ClassFileDef classFileDef : updateClassMap.values()) {
                dos.writeUTF(classFileDef.getClassName()); // Write class name
                dos.writeInt(classFileDef.getData().length); // Write bytecode length
                dos.write(classFileDef.getData()); // Write bytecode
            }
            dos.flush();
            // Set state to ready for update
            HotSwapManager.updateBytes = baos.toByteArray();
            HotSwapManager.exception = null;

            // Apply update
            loadHotSwapAgent();

            if (HotSwapManager.exception != null) {
                for (ClassFileDef classFileDef : updateClassMap.values()) {
                    logger.error("Hot-swap failed for class [{}]", classFileDef.getClassName());
                }
                logger.error("Hot-swap failed: ", HotSwapManager.exception);
                return;
            }

            for (ClassFileDef classFileDef : updateClassMap.values()) {
                logger.info("Hot-swap successful for class [{}]", classFileDef.getClassName());
            }
            long end = TimeUtils.currentTimeMillis();
            logger.info("Hot-swap total time: [{}]ms", end - start);

            // Update successful; save update record
            classFileDefMap.putAll(updateClassMap);

            // Delete all updated .java and .class files
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
            // Get the PID of the JVM running this class
            String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
            // Attach to the PID to establish a communication pipe and load the agent
            vm = VirtualMachine.attach(pid);
            vm.loadAgent(HOT_SWAP_AGENT);
        } catch (AgentInitializationException | AgentLoadException | AttachNotSupportedException | IOException e) {
            logger.error("Hot-swap: failed to attach VirtualMachine", e);
            throw e;
        } finally {
            try {
                if (vm != null) {
                    vm.detach();
                }
            } catch (IOException e) {
                logger.error("Hot-swap: failed to detach VM", e);
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
