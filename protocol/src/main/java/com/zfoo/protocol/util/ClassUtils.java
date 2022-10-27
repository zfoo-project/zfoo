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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author godotg
 * @version 3.0
 */
public abstract class ClassUtils {

    /**
     * 从类路径中读取文件
     *
     * @param filePath 一般指resources中的文件，也可以在jar中
     */
    public static InputStream getFileFromClassPath(String filePath) throws IOException {
        return getDefaultClassLoader().getResource(filePath).openStream();
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
        ReflectionUtils.assertIsPojoClass(clazz);
        // 不能是泛型类
        AssertionUtils.isTrue(ArrayUtils.isEmpty(clazz.getTypeParameters()), "[class:{}]不能是泛型类", clazz.getCanonicalName());
        // 必须要有一个空的构造器
        ReflectionUtils.publicEmptyConstructor(clazz);

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
