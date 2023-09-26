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

import com.zfoo.protocol.collection.ArrayUtils;
import com.zfoo.protocol.exception.RunException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author godotg
 */
public abstract class ClassUtils {

    public final static String CLASS_FILE_EXT = ".class";

    public final static String FILE_PROTOCOL = "file";

    public final static String FILE_URL_PREFIX = "file:";

    public final static String JAR_PROTOCOL = "jar";

    public final static String JAR_URL_SEPARATOR = "!/";

    public static Class<?> forName(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 扫描指定包下的class文件
     *
     * @param packageName 包名称（xxx.xxx）
     * @return 返回指定包下的class全称集合
     * @throws IOException 假如扫描失败，则抛出该异常
     */
    public static Set<String> getAllClasses(String packageName) throws IOException {
        Set<String> classSet = new HashSet<>();
        String packagePath = packageName.replaceAll(StringUtils.PERIOD_REGEX, StringUtils.SLASH);
        Enumeration<URL> resourceUrls = getDefaultClassLoader().getResources(packagePath);
        while (resourceUrls.hasMoreElements()) {
            URL packageUrl = resourceUrls.nextElement();
            // 如果是以文件的形式保存在服务器上
            if (isFileProtocol(packageUrl)) {
                // file类型的扫描
                File file = getFile(packageUrl);
                // 以文件的方式扫描整个包下的文件 并添加到集合中
                findClassesInPackageByFile(packageName, file, classSet);
            } else if (isJarProtocol(packageUrl)) {
                findClassesInJarFile(packageName, packageUrl, classSet);
            }
        }
        return classSet;
    }

    /**
     * 扫描jar文件中的class
     *
     * @param packageName
     *            包名称
     * @param packageUrl
     *            jar的url
     * @param classSet
     *            class全路径集合
     * @throws IOException 假如解析出现io异常时，则抛出该异常
     */
    private static void findClassesInJarFile(String packageName, URL packageUrl,
                                             Set<String> classSet) throws IOException {
        URLConnection con = packageUrl.openConnection();
        JarFile jarFile = null;
        String jarFileUrl = "";
        boolean closeJarFile = true;

        if (con instanceof JarURLConnection) {
            JarURLConnection jarCon = (JarURLConnection) con;
            useCachesIfNecessary(jarCon);
            jarFile = jarCon.getJarFile();
            jarFileUrl = jarCon.getJarFileURL().toExternalForm();
            closeJarFile = !jarCon.getUseCaches();
        } else {
            //不是JarURLConnection->需要依赖于URL文件解析。
            //我们假设URL的格式为“jar:path!/entry”，只要遵循条目格式，协议就是任意的。
            //我们还将处理带和不带前导“file:”前缀的路径。
            String urlFile = packageUrl.getFile();
            try {
                int separatorIndex = urlFile.indexOf(JAR_URL_SEPARATOR);
                if (separatorIndex != -1) {
                    jarFileUrl = urlFile.substring(0, separatorIndex);
                    jarFile = getJarFile(jarFileUrl);
                } else {
                    jarFile = new JarFile(urlFile);
                    jarFileUrl = urlFile;
                }
                closeJarFile = true;
            } catch (Exception ex) {
                throw new RunException(ex);
            }
        }

        if (jarFile == null) {
            return;
        }
        try {
            classSet.addAll(findByJarFile(packageName, jarFile));
        } finally {
            if (closeJarFile) {
                jarFile.close();
            }
        }
    }

    /**
     * 获取指定jar文件中所有class名称（包名+类名称）格式为xx.xx.yy
     *
     * @param jarFile jar文件
     * @return class名称集合， 假如参数为null，返回大小为0的集合
     */
    public Set<String> findByJarFile(JarFile jarFile) {
        return findByJarFile(StringUtils.EMPTY, jarFile);
    }

    /**
     * 获取指定jar文件中所有class名称（包名+类名称） xx.xx.yy
     *
     * @param packageName 包名前缀xx.xx
     * @param jarFile jar文件
     * @return class名称集合， 假如参数为null，返回大小为0的集合
     */
    public static Set<String> findByJarFile(String packageName, JarFile jarFile) {
        Set<String> classSet = new HashSet<>();
        if (jarFile == null) {
            return classSet;
        }
        String packageBasePath = packageName.replaceAll(StringUtils.PERIOD_REGEX, StringUtils.SLASH);
        if (!"".equals(packageBasePath) && !packageBasePath.endsWith("/")) {
            // 根条目路径必须以斜杠结束，以允许正确的匹配。匹配sunjre在这里不返回斜杠，但是beajrockit返回。
            packageBasePath = packageBasePath + StringUtils.SLASH;
        }
        for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements();) {
            JarEntry entry = entries.nextElement();
            String entryPath = entry.getName();
            if (entryPath.startsWith(packageBasePath)) {
                int index = entryPath.indexOf(CLASS_FILE_EXT);
                if (index == -1) {
                    continue;
                }
                String relativePath = entryPath.substring(0, index);
                String className = relativePath.replaceAll(StringUtils.SLASH, StringUtils.PERIOD);
                classSet.add(className);
            }
        }
        return classSet;
    }

    /**
     * 将给定的jar文件URL解析为JarFile对象
     *
     * @param jarFileUrl
     */
    private static JarFile getJarFile(String jarFileUrl) throws IOException {
        if (jarFileUrl.startsWith(FILE_URL_PREFIX)) {
            try {
                return new JarFile(toURI(jarFileUrl).getSchemeSpecificPart());
            } catch (Exception ex) {
                return new JarFile(jarFileUrl.substring(FILE_URL_PREFIX.length()));
            }
        } else {
            return new JarFile(jarFileUrl);
        }
    }

    /**
     * 以文件的方式扫描整个包下的文件 并添加到集合中
     *
     * @param packageName
     *            包名称
     * @param dirOrFile
     *            查找包对应的文件或文件夹
     * @param classSet
     *            class全路径集合
     */
    private static void findClassesInPackageByFile(String packageName, File dirOrFile,
                                                   Set<String> classSet) {
        // 如果不存在或者 也不是目录就直接返回
        if (!dirOrFile.exists()) {
            return;
        }
        if (!dirOrFile.isDirectory()) {
            return;
        }
        // 如果存在 就获取包下的所有文件 包括目录
        File[] dirFiles = dirOrFile.listFiles();
        if (dirFiles == null) {
            return;
        }

        // 循环所有文件
        for (File file : dirFiles) {
            String name = file.getName();
            // 如果是目录 则继续扫描
            if (file.isDirectory()) {
                findClassesInPackageByFile(packageName + "." + name, file, classSet);
            } else {
                String filename = file.getName();
                // 如果是java类文件 去掉后面的.class 只留下类名
                String className = filename.substring(0, filename.length() - CLASS_FILE_EXT.length());
                className = packageName + "." + className;
                //去掉前缀“.”
                if (className.startsWith(".")) {
                    className = className.substring(1);
                }
                classSet.add(className);
            }
        }
    }

    public static File getFile(URL url) {
        if (!FILE_PROTOCOL.equals(url.getProtocol())) {
            throw new IllegalArgumentException("给定的URL无法解析为绝对文件路径: " + url);
        }
        try {
            return new File(toURI(url).getSchemeSpecificPart());
        } catch (Exception ex) {
            return new File(url.getFile());
        }
    }

    /**
     * 用给定的URL创建URI
     * 用“%20”编码替换URI的空格。
     * @param url 要转换为URI实例的URL
     * @return URI对象
     * @see java.net.URL#toURI()
     */
    public static URI toURI(URL url) {
        return toURI(url.toString());
    }

    /**
     * 用给定的字符串创建URI
     * 用“%20”编码替换URI的空格。
     * @param location 要转换为URI实例的字符串
     * @return URI对象
     */
    public static URI toURI(String location) {
        try {
            return new URI(location.replace(" ", "%20"));
        } catch (URISyntaxException e) {
            throw new RunException("uri配置错误");
        }
    }

    /**
     * 判断给定的URL是不是file协议
     *
     * @param url url
     * @return 假如是file协议，返回true，否则返回false
     */
    public static boolean isFileProtocol(URL url){
        if (url == null) {
            return false;
        }
        return FILE_PROTOCOL.equals(url.getProtocol());
    }

    /**
     * 判断给定的URL是不是Jar协议
     *
     * @param url url
     * @return 假如是Jar协议，返回true，否则返回false
     */
    public static boolean isJarProtocol(URL url){
        if (url == null) {
            return false;
        }
        String protocol = url.getProtocol();
        return JAR_PROTOCOL.equals(protocol);
    }

    /**
     * 在给定的连接上设置“useCaches”标志，对于基于JNLP的资源，设置false，其他资源该标志保留原样
     *
     * @param urlConnection urlConnection
     */
    public static void useCachesIfNecessary(URLConnection urlConnection){
        if (urlConnection != null) {
            urlConnection.setUseCaches(urlConnection.getClass().getSimpleName().startsWith("JNLP"));
        }
    }

    /**
     * 从类路径中读取文件
     *
     * @param filePath 一般指resources中的文件，也可以在jar中
     */
    public static InputStream getFileFromClassPath(String filePath) throws IOException {
        var classLoader = getDefaultClassLoader();
        var resource = classLoader.getResource(filePath);
        return resource.openStream();
    }

    public static String getFileFromClassPathToString(String filePath) {
        try {
            return StringUtils.bytesToString(IOUtils.toByteArray(ClassUtils.getFileFromClassPath(filePath)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
     * 返回和clazz相关的所有子协议，协议和protocol协议一致
     */
    public static Set<Class<?>> relevantClass(Class<?> clazz) {
        var classSet = new HashSet<Class<?>>();
        return relevantClass(clazz, classSet);
    }


    private static Set<Class<?>> relevantClass(Class<?> clazz, Set<Class<?>> classSet) {
        if (!classSet.add(clazz)) {
            return classSet;
        }

        // 是否为一个简单的javabean，为了防止不同层对象混用造成潜在的并发问题，特别是网络层和po层混用
        // 不能是泛型类
        AssertionUtils.isTrue(ArrayUtils.isEmpty(clazz.getTypeParameters()), "[class:{}] cannot be generic class", clazz.getCanonicalName());

        var filedList = ReflectionUtils.notStaticAndTransientFields(clazz);

        for (var field : filedList) {
            // 是一个基本类型变量
            var fieldType = field.getType();
            if (isBaseType(fieldType)) {
                // do nothing
            } else if (fieldType.isArray()) {
                // 是一个数组
                Class<?> arrayClazz = fieldType.getComponentType();
                relevantClass0(arrayClazz, classSet);
            } else if (Set.class.isAssignableFrom(fieldType)) {
                AssertionUtils.isTrue(fieldType.equals(Set.class), "[class:{}]类型声明不正确，必须是Set接口类型", clazz.getCanonicalName());

                var type = field.getGenericType();
                AssertionUtils.isTrue(type instanceof ParameterizedType, "[class:{}]类型声明不正确，不是泛型类[field:{}]", clazz.getCanonicalName(), field.getName());

                var types = ((ParameterizedType) type).getActualTypeArguments();
                AssertionUtils.isTrue(types.length == 1, "[class:{}]中Set类型声明不正确，[field:{}]必须声明泛型类", clazz.getCanonicalName(), field.getName());

                relevantClass0(types[0], classSet);
            } else if (List.class.isAssignableFrom(fieldType)) {
                // 是一个List
                AssertionUtils.isTrue(fieldType.equals(List.class), "[class:{}]类型声明不正确，必须是List接口类型", clazz.getCanonicalName());

                var type = field.getGenericType();
                AssertionUtils.isTrue(type instanceof ParameterizedType, "[class:{}]类型声明不正确，不是泛型类[field:{}]", clazz.getCanonicalName(), field.getName());

                var types = ((ParameterizedType) type).getActualTypeArguments();
                AssertionUtils.isTrue(types.length == 1, "[class:{}]中List类型声明不正确，[field:{}]必须声明泛型类", clazz.getCanonicalName(), field.getName());

                relevantClass0(types[0], classSet);
            } else if (Map.class.isAssignableFrom(fieldType)) {
                if (!fieldType.equals(Map.class)) {
                    throw new RunException("[class:{}]类型声明不正确，必须是Map接口类型", clazz.getCanonicalName());
                }

                var type = field.getGenericType();
                if (!(type instanceof ParameterizedType)) {
                    throw new RunException("[class:{}]中数组类型声明不正确，[field:{}]不是泛型类", clazz.getCanonicalName(), field.getName());
                }

                var types = ((ParameterizedType) type).getActualTypeArguments();
                if (types.length != 2) {
                    throw new RunException("[class:{}]中数组类型声明不正确，[field:{}]必须声明泛型类", clazz.getCanonicalName(), field.getName());
                }

                var keyType = types[0];
                var valueType = types[1];
                relevantClass0(keyType, classSet);
                relevantClass0(valueType, classSet);
            } else {
                relevantClass(fieldType, classSet);
            }
        }

        return classSet;
    }

    private static void relevantClass0(Type type, Set<Class<?>> classSet) {
        if (type instanceof ParameterizedType) {
            // 泛型类
            Class<?> clazz = (Class<?>) ((ParameterizedType) type).getRawType();
            if (Set.class.equals(clazz)) {
                // Set<Set<String>>
                relevantClass0(((ParameterizedType) type).getActualTypeArguments()[0], classSet);
                return;
            } else if (List.class.equals(clazz)) {
                // List<List<String>>
                relevantClass0(((ParameterizedType) type).getActualTypeArguments()[0], classSet);
                return;
            } else if (Map.class.equals(clazz)) {
                // Map<List<String>, List<String>>
                var types = ((ParameterizedType) type).getActualTypeArguments();
                var keyType = types[0];
                var valueType = types[1];
                relevantClass0(keyType, classSet);
                relevantClass0(valueType, classSet);
                return;
            }
        } else if (type instanceof Class) {
            Class<?> clazz = ((Class<?>) type);
            if (isBaseType(clazz)) {
                // do nothing
                return;
            } else if (clazz.getComponentType() != null) {
                // 是一个二维以上数组
                throw new RunException("不支持多维数组或集合嵌套数组[type:{}]类型，仅支持一维数组", type);
            } else if (clazz.equals(List.class) || clazz.equals(Set.class) || clazz.equals(Map.class)) {
                throw new RunException("不支持数组和集合联合使用[type:{}]类型", type);
            } else {
                if (!classSet.add(clazz)) {
                    return;
                }
                relevantClass(clazz, classSet);
                return;
            }
        }
        throw new RunException("[type:{}]类型不正确", type);
    }

    private static boolean isBaseType(Class<?> clazz) {
        return clazz.isPrimitive()
                || Number.class.isAssignableFrom(clazz)
                || String.class.isAssignableFrom(clazz)
                || Boolean.class.isAssignableFrom(clazz)
                || Character.class.isAssignableFrom(clazz);
    }
}
