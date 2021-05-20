/*
 * Copyright (C) 2020 The zfoo Authors
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.zfoo.protocol.util;

import com.zfoo.protocol.collection.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public abstract class ClassUtils {

    public static final String FILE_URL_PROTOCOL = "file";

    public static final String JAR_URL_PROTOCOL = "jar";

    public static final String CLASS_SUFFIX = ".class";

    public static final String JAR_SUFFIX = ".jar";

    public static final String ZIP_SUFFIX = ".zip";

    public static final char FILE_SEPARATOR = '/';

    /*
     find the location of the class come from
     1.FileTest.class.getResource("")
     得到的是当前类FileTest.class文件的URI目录。不包括自己！
     如：file:/D:/java/eclipse32/workspace/jbpmtest3/bin/com/test/
     2.FileTest.class.getResource("/")
     　　得到的是当前的classpath的绝对URI路径。
     　　如：file:/D:/java/eclipse32/workspace/jbpmtest3/bin/
     3.Thread.currentThread().getContextClassLoader().getResource("")
     得到的也是当前ClassPath的绝对URI路径。
     　　 如：file:/D:/java/eclipse32/workspace/jbpmtest3/bin/
     4.FileTest.class.getClassLoader().getResource("")
     　　得到的也是当前ClassPath的绝对URI路径。
     　　 如：file:/D:/java/eclipse32/workspace/jbpmtest3/bin/
     5.ClassLoader.getSystemResource("")
     　   得到的也是当前ClassPath的绝对URI路径。
     　　 如：file:/D:/java/eclipse32/workspace/jbpmtest3/bin/
     　我推荐使用Thread.currentThread().getContextClassLoader().getResource("")来得到当前的classpath的绝对路径的URI表示法。
     */

    public static String classLocation(final Class<?> cls) {
        AssertionUtils.notNull(cls);
        URL result = null;
        String clsAsResource = cls.getName().replace(StringUtils.PERIOD, StringUtils.SLASH).concat(CLASS_SUFFIX);
        ProtectionDomain pd = cls.getProtectionDomain();
        if (pd != null) {
            CodeSource cs = pd.getCodeSource();
            if (cs != null) {
                result = cs.getLocation();
            }
            if (result != null) {
                if (FILE_URL_PROTOCOL.equals(result.getProtocol())) {
                    try {
                        // "!/"为分隔符，分割jar包，和jar包里的文件
                        if (result.toExternalForm().endsWith(JAR_SUFFIX) || result.toExternalForm().endsWith(ZIP_SUFFIX)) {
                            result = new URL(JAR_URL_PROTOCOL + StringUtils.COLON + result.toExternalForm() + "!/" + clsAsResource);
                        } else if (new File(result.getFile()).isDirectory()) {
                            result = new URL(result, clsAsResource);
                        }
                    } catch (MalformedURLException ignore) {
                    }
                }
            }
        }
        if (result == null) {
            final ClassLoader clsLoader = cls.getClassLoader();
            result = clsLoader != null ? clsLoader.getResource(clsAsResource) : ClassLoader.getSystemResource(clsAsResource);
        }
        return result.toString();
    }


    /**
     * 获取指定包下的所有类，只能搜索当前项目路径和maven项目路径
     *
     * @param packageName 形如"org.hotswap"，不能带有斜线/，以为java格式为主
     * @return 当前项目下的所有Java类
     * @throws Exception 异常
     */
    public static Set<Class<?>> getAllClasses(String packageName) throws Exception {
        Set<Class<?>> classSet = new LinkedHashSet<>();
        // 定义一个枚举的集合并进行循环来处理这个目录下的things,当前的classpath的绝对路径的URI表示法。
        Enumeration<URL> urlEnumeration = Thread.currentThread().getContextClassLoader().getResources(packageName.replace(StringUtils.PERIOD, StringUtils.SLASH));
        while (urlEnumeration.hasMoreElements()) {
            // 获取下一个元素，如果是jar：//得到的结果大概是：jar:file:/C:/Users/ibm/.m2/repository/junit/junit/4.12/junit-4.12.jar!/org/junit
            URL url = urlEnumeration.nextElement();
            String protocol = url.getProtocol();
            System.out.println(url);
            if (StringUtils.isBlank(protocol)) {
                continue;
            }
            // file（不打包成jar运行），jar（打包成jar运行）
            if (protocol.equals(FILE_URL_PROTOCOL)) {
                // 获取包的物理路径
                String filePath = URLDecoder.decode(url.getFile(), StringUtils.DEFAULT_CHARSET);
                List<File> fileList = FileUtils.getAllReadableFiles(new File(filePath));
                for (File file : fileList) {
                    String fileName = file.getName();
                    // 不是.class文件和包含美元符号的内部类或者匿名内部类不算
                    if (!fileName.endsWith(CLASS_SUFFIX) || fileName.contains(StringUtils.DOLLAR)) {
                        continue;
                    }
                    // 如果是java类文件，则去掉后面的.class 只留下类名
                    String className = StringUtils.substringBeforeLast(fileName, CLASS_SUFFIX);
                    if (!StringUtils.isBlank(packageName)) {
                        String a = StringUtils.substringAfterFirst(file.getAbsolutePath(), FileUtils.getProAbsPath() + File.separator);
                        a = a.replaceAll(StringUtils.BACK_SLASH + File.separator, StringUtils.PERIOD);
                        String b = StringUtils.substringBeforeFirst(a, packageName);
                        String c = StringUtils.substringAfterFirst(a, b);
                        className = StringUtils.substringBeforeLast(c, CLASS_SUFFIX);
                    }
                    classSet.add(Thread.currentThread().getContextClassLoader().loadClass(className));
                }
            } else if (protocol.equals(JAR_URL_PROTOCOL)) {
                JarFile jarFile = ((JarURLConnection) url.openConnection()).getJarFile();
                if (jarFile == null) {
                    continue;
                }
                //得到该jar文件下面的类实体
                Enumeration<JarEntry> jarEntryEnumeration = jarFile.entries();
                while (jarEntryEnumeration.hasMoreElements()) {
                    JarEntry entry = jarEntryEnumeration.nextElement();
                    String jarEntryName = entry.getName();
                    //这里我们需要过滤不是class文件和不在basePack包名下的类
                    if (!jarEntryName.contains(CLASS_SUFFIX) || jarEntryName.contains(StringUtils.DOLLAR)) {
                        continue;
                    }
                    String className = StringUtils.substringBeforeLast(jarEntryName, CLASS_SUFFIX).replaceAll(StringUtils.SLASH, StringUtils.PERIOD);
                    if (!className.startsWith(packageName)) {
                        continue;
                    }
                    classSet.add(Thread.currentThread().getContextClassLoader().loadClass(className));
                }
            } else {
                throw new IllegalStateException(StringUtils.format("不合法的协议文件[protocol:{}]", protocol));
            }
        }
        return classSet;
    }


    public static List<Class<?>> getClasses(String packageName, Predicate<Class<?>> classFilter) {
        Set<Class<?>> allClasses = null;
        try {
            allClasses = getAllClasses(packageName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (CollectionUtils.isEmpty(allClasses)) {
            return new ArrayList<>();
        }

        List<Class<?>> list = new ArrayList<>();

        if (classFilter == null) {
            list.addAll(allClasses);
            return list;
        }

        for (Class<?> clazz : allClasses) {
            if (classFilter.test(clazz)) {
                list.add(clazz);
            }
        }

        return list;
    }


    /**
     * 获取编译过后的类文件(*.class)的绝对路径
     *
     * @param clazz 类Class
     * @return 对应类的绝对路径
     */
    public static String getClassAbsPath(Class<?> clazz) {
        File file = new File(clazz.getResource("").getPath());
        return file.getAbsolutePath();
    }


    public static ClassLoader getDefaultClassLoader() {
        var classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader != null) {
            return classLoader;
        }

        // No thread context class loader -> use class loader of this class.
        classLoader = ClassUtils.class.getClassLoader();
        if (classLoader != null) {
            return classLoader;
        }

        // getClassLoader() returning null indicates the bootstrap ClassLoader
        classLoader = ClassLoader.getSystemClassLoader();
        return classLoader;
    }


    /**
     * 从类路径中读取文件
     *
     * @param filePath 一般指resources中的文件，也可以在jar中
     */
    public static InputStream getFileFromClassPath(String filePath) throws IOException {
//        ClassUtils.getDefaultClassLoader().getResourceAsStream(filePath)
        return ClassUtils.getDefaultClassLoader().getResource(filePath).openStream();
    }

}
