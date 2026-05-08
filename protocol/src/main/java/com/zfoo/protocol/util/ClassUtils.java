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
import java.net.*;
import java.util.*;
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
     * Scan class files under the specified package
     *
     * @param packageName package name（xxx.xxx）
     * @return set of fully-qualified class names under the package
     * @throws IOException if the scan fails
     */
    public static Set<String> getAllClasses(String packageName) throws IOException {
        Set<String> classSet = new HashSet<>();
        String packagePath = packageName.replaceAll(StringUtils.PERIOD_REGEX, StringUtils.SLASH);
        Enumeration<URL> resourceUrls = getDefaultClassLoader().getResources(packagePath);
        while (resourceUrls.hasMoreElements()) {
            URL packageUrl = resourceUrls.nextElement();
            // If stored as files on the server
            if (isFileProtocol(packageUrl)) {
                // Scan as file type
                File file = getFile(packageUrl);
                // Scan all files under the package as file entries and add to the collection
                findClassesInPackageByFile(packageName, file, classSet);
            } else if (isJarProtocol(packageUrl)) {
                findClassesInJarFile(packageName, packageUrl, classSet);
            }
        }
        return classSet;
    }

    /**
     * Scan classes within a JAR file
     *
     * @param packageName
     *            package name
     * @param packageUrl
     *            URL of the JAR
     * @param classSet
     *            set of fully-qualified class names
     * @throws IOException if an I/O error occurs during parsing
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
            // Not a JarURLConnection fall back to URL file parsing
            // Assume URL format is "jar:path!/entry"; protocol is arbitrary if entry format is followed
            // Handle paths both with and without a leading "file:" prefix
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
     * Get all class names (package + class) from a JAR file in xx.xx.yy format
     *
     * @param jarFile the JAR file
     * @return set of class names; empty set if parameter is null
     */
    public Set<String> findByJarFile(JarFile jarFile) {
        return findByJarFile(StringUtils.EMPTY, jarFile);
    }

    /**
     * Get all class names from a JAR file filtered by package prefix (xx.xx.yy format)
     *
     * @param packageName package name prefix (xx.xx)
     * @param jarFile the JAR file
     * @return set of class names; empty set if parameter is null
     */
    public static Set<String> findByJarFile(String packageName, JarFile jarFile) {
        Set<String> classSet = new HashSet<>();
        if (jarFile == null) {
            return classSet;
        }
        String packageBasePath = packageName.replaceAll(StringUtils.PERIOD_REGEX, StringUtils.SLASH);
        if (!"".equals(packageBasePath) && !packageBasePath.endsWith("/")) {
            // Root entry path must end with a slash for correct matching. Sun JRE omits the slash; BEA JRockit includes it.
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
     * Resolve the given JAR file URL to a JarFile object
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
     * Scan all files under the package as file entries and add to the collection
     *
     * @param packageName
     *            package name
     * @param dirOrFile
     *            locate the file or directory for the package
     * @param classSet
     *            set of fully-qualified class names
     */
    private static void findClassesInPackageByFile(String packageName, File dirOrFile,
                                                   Set<String> classSet) {
        // Return if path does not exist or is not a directory
        if (!dirOrFile.exists()) {
            return;
        }
        if (!dirOrFile.isDirectory()) {
            return;
        }
        // Get all files under the package, including subdirectories
        File[] dirFiles = dirOrFile.listFiles();
        if (dirFiles == null) {
            return;
        }

        // Iterate over all files
        for (File file : dirFiles) {
            String name = file.getName();
            // If it is a directory, scan recursively
            if (file.isDirectory()) {
                findClassesInPackageByFile(packageName + "." + name, file, classSet);
            } else {
                String filename = file.getName();
                // If it is a .class file, strip the extension
                String className = filename.substring(0, filename.length() - CLASS_FILE_EXT.length());
                className = packageName + "." + className;
                // Strip the prefix“.”
                if (className.startsWith(".")) {
                    className = className.substring(1);
                }
                classSet.add(className);
            }
        }
    }

    public static File getFile(URL url) {
        if (!FILE_PROTOCOL.equals(url.getProtocol())) {
            throw new IllegalArgumentException("The given URL cannot be resolved to an absolute file path: " + url);
        }
        try {
            return new File(toURI(url).getSchemeSpecificPart());
        } catch (Exception ex) {
            return new File(url.getFile());
        }
    }

    /**
     * Create a URI from the given URL
     * Replace spaces in URI with“%20”encoding.
     * @param url the URL to convert to a URI
     * @return URI object
     * @see java.net.URL#toURI()
     */
    public static URI toURI(URL url) {
        return toURI(url.toString());
    }

    /**
     * Create a URI from the given string
     * Replace spaces in URI with“%20”encoding.
     * @param location the string to convert to a URI
     * @return URI object
     */
    public static URI toURI(String location) {
        try {
            return new URI(location.replace(" ", "%20"));
        } catch (URISyntaxException e) {
            throw new RunException("URI configuration error");
        }
    }

    /**
     * Check if the given URL uses the file protocol
     *
     * @param url url
     * @return true if the URL uses the file protocol, false otherwise
     */
    public static boolean isFileProtocol(URL url){
        if (url == null) {
            return false;
        }
        return FILE_PROTOCOL.equals(url.getProtocol());
    }

    /**
     * Check if the given URL uses the JAR protocol
     *
     * @param url url
     * @return true if the URL uses the JAR protocol, false otherwise
     */
    public static boolean isJarProtocol(URL url){
        if (url == null) {
            return false;
        }
        String protocol = url.getProtocol();
        return JAR_PROTOCOL.equals(protocol);
    }

    /**
     * Set the 'useCaches' flag on the given connection;“useCaches”false for JNLP-based resources, unchanged for others
     *
     * @param urlConnection urlConnection
     */
    public static void useCachesIfNecessary(URLConnection urlConnection){
        if (urlConnection != null) {
            urlConnection.setUseCaches(urlConnection.getClass().getSimpleName().startsWith("JNLP"));
        }
    }

    /**
     * Read a file from the classpath
     *
     * @param filePath typically a file in resources; may also be inside a JAR
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
     * Get the absolute path of the compiled class file (*.class)
     *
     * @param clazz the class
     * @return absolute path of the corresponding class file
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
     * Return all sub-protocols related to clazz; follows the same protocol conventions
     */
    public static Set<Class<?>> relevantClass(Class<?> clazz) {
        var classSet = new HashSet<Class<?>>();
        return relevantClass(clazz, classSet);
    }


    private static Set<Class<?>> relevantClass(Class<?> clazz, Set<Class<?>> classSet) {
        if (!classSet.add(clazz)) {
            return classSet;
        }

        // Check if it is a simple JavaBean; prevents cross-layer misuse causing concurrency issues
        // Must not be a generic class
        AssertionUtils.isTrue(ArrayUtils.isEmpty(clazz.getTypeParameters()), "[class:{}] cannot be generic class", clazz.getCanonicalName());

        var filedList = ReflectionUtils.notStaticAndTransientFields(clazz);

        for (var field : filedList) {
            // Is a primitive type variable
            var fieldType = field.getType();
            if (isBaseType(fieldType)) {
                // do nothing
            } else if (fieldType.isArray()) {
                // Is an array
                Class<?> arrayClazz = fieldType.getComponentType();
                relevantClass0(arrayClazz, classSet);
            } else if (Set.class.isAssignableFrom(fieldType)) {
                AssertionUtils.isTrue(fieldType.equals(Set.class), "[class:{}] incorrect type declaration; must be the Set interface", clazz.getCanonicalName());

                var type = field.getGenericType();
                AssertionUtils.isTrue(type instanceof ParameterizedType, "[class:{}] incorrect type declaration; field [field:{}] is not generic", clazz.getCanonicalName(), field.getName());

                var types = ((ParameterizedType) type).getActualTypeArguments();
                AssertionUtils.isTrue(types.length == 1, "[class:{}] Set field [field:{}] must declare a generic type", clazz.getCanonicalName(), field.getName());

                relevantClass0(types[0], classSet);
            } else if (List.class.isAssignableFrom(fieldType)) {
                // Is a List
                AssertionUtils.isTrue(fieldType.equals(List.class), "[class:{}] incorrect type declaration; must be the List interface", clazz.getCanonicalName());

                var type = field.getGenericType();
                AssertionUtils.isTrue(type instanceof ParameterizedType, "[class:{}] incorrect type declaration; field [field:{}] is not generic", clazz.getCanonicalName(), field.getName());

                var types = ((ParameterizedType) type).getActualTypeArguments();
                AssertionUtils.isTrue(types.length == 1, "[class:{}] List field [field:{}] must declare a generic type", clazz.getCanonicalName(), field.getName());

                relevantClass0(types[0], classSet);
            } else if (Map.class.isAssignableFrom(fieldType)) {
                if (!fieldType.equals(Map.class)) {
                    throw new RunException("[class:{}] incorrect type declaration; must be the Map interface", clazz.getCanonicalName());
                }

                var type = field.getGenericType();
                if (!(type instanceof ParameterizedType)) {
                    throw new RunException("[class:{}] array field [field:{}] is not generic", clazz.getCanonicalName(), field.getName());
                }

                var types = ((ParameterizedType) type).getActualTypeArguments();
                if (types.length != 2) {
                    throw new RunException("[class:{}] array field [field:{}] must declare a generic type", clazz.getCanonicalName(), field.getName());
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
            // Generic class
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
                // Is a multi-dimensional array
                throw new RunException("Multi-dimensional or collection-nested arrays not supported for [type:{}]; only one-dimensional arrays are allowed", type);
            } else if (clazz.equals(List.class) || clazz.equals(Set.class) || clazz.equals(Map.class)) {
                throw new RunException("Combining arrays with collections is not supported for [type:{}]", type);
            } else {
                if (!classSet.add(clazz)) {
                    return;
                }
                relevantClass(clazz, classSet);
                return;
            }
        }
        throw new RunException("[type:{}] has an incorrect type", type);
    }

    public static boolean isBaseType(Class<?> clazz) {
        return clazz.isPrimitive()
                || Number.class.isAssignableFrom(clazz)
                || String.class.isAssignableFrom(clazz)
                || Boolean.class.isAssignableFrom(clazz)
                || Character.class.isAssignableFrom(clazz);
    }
}
