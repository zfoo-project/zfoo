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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author godotg
 * @version 3.0
 */
public abstract class ClassUtils {

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
        return getDefaultClassLoader().getResource(filePath).openStream();
    }

}
