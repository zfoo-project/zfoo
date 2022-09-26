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
import com.zfoo.protocol.exception.UnknownException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Predicate;


/**
 * 反射工具类
 *
 * @author godotg
 * @version 3.0
 */
public abstract class ReflectionUtils {

    /**
     * 如果field符合FieldFilter过滤条件，则执行回调方法
     *
     * @param field         属性
     * @param fieldFilter   属性过滤器
     * @param fieldCallback 属性回调方法
     */
    public static void filterField(Field field, Predicate<Field> fieldFilter, Consumer<Field> fieldCallback) {
        if (fieldFilter != null && !fieldFilter.test(field)) {
            return;
        }
        fieldCallback.accept(field);
    }

    /**
     * 将clazz通过filter过滤，过滤后的field执行callback方法
     * <p>
     * Invoke the given callback on all fields in the target class, going up the
     * class hierarchy to get all declared fields.
     * </p>
     *
     * @param clazz         the target class to analyze
     * @param fieldCallback the callback to invoke for each field
     * @param fieldFilter   the matches that determines the fields to apply the callback to
     */
    public static void filterFieldsInClass(Class<?> clazz, Predicate<Field> fieldFilter, Consumer<Field> fieldCallback) {
        // Keep backing up the inheritance hierarchy.
        Class<?> targetClass = clazz;
        do {
            var fields = targetClass.getDeclaredFields();
            for (var field : fields) {
                ReflectionUtils.filterField(field, fieldFilter, fieldCallback);
            }
            targetClass = targetClass.getSuperclass();
        } while (targetClass != null && targetClass != Object.class);
    }

    public static boolean isPojoClass(Class<?> clazz) {
        return clazz.getSuperclass().equals(Object.class);
    }

    public static void assertIsPojoClass(Class<?> clazz) {
        if (!isPojoClass(clazz)) {
            throw new RunException("[class:{}]不是简单的javabean（POJO类不能继承别的类，但是可以继承其它接口）", clazz.getName());
        }
    }

    public static Constructor<?> publicEmptyConstructor(Class<?> clazz) {
        Constructor<?> constructor;

        try {
            constructor = clazz.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new UnknownException(e, "[class:{}] should have exactly one public zero-argument constructor", clazz.getCanonicalName());
        }

        if (!Modifier.isPublic(constructor.getModifiers())) {
            throw new UnknownException("[class:{}] should have exactly one public zero-argument constructor", clazz.getCanonicalName());
        }

        return constructor;
    }

    /**
     * 标准的属性名称更加通用，前缀不能是is，否则属性名称在不同语言很难去统一get和set方法
     */
    public static void assertIsStandardFieldName(Field field) {
        var fieldName = field.getName();
        if (fieldName.startsWith("is")) {
            throw new RunException("to avoid different get or set method in different language, [field:{}] can not be started with name of 'is' in class:[{}]"
                    , field.getName(), field.getDeclaringClass().getCanonicalName());
        }
    }

    /**
     * 从一个指定的POJO的Class中获得具有指定注解的Field，只获取子类的Field，不获取父类的Field
     *
     * @param clazz      指定的Class
     * @param annotation 指定注解的Class
     * @return 数组，可能长度为0
     */
    public static Field[] getFieldsByAnnoInPOJOClass(Class<?> clazz, Class<? extends Annotation> annotation) {
        var list = new ArrayList<Field>();
        var fields = clazz.getDeclaredFields();
        for (var field : fields) {
            if (field.isAnnotationPresent(annotation)) {
                list.add(field);
            }
        }
        return ArrayUtils.listToArray(list, Field.class);
    }

    public static Field[] getFieldsByAnnoNameInPOJOClass(Class<?> clazz, String annotationName) {
        var list = new ArrayList<Field>();
        var fields = clazz.getDeclaredFields();
        for (var field : fields) {
            var annotations = field.getAnnotations();
            if (ArrayUtils.isEmpty(annotations)) {
                continue;
            }

            for (var annotation : annotations) {
                if (annotation.annotationType().getName().equals(annotationName)) {
                    list.add(field);
                }
            }
        }
        return ArrayUtils.listToArray(list, Field.class);
    }

    public static Field getFieldByNameInPOJOClass(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException(StringUtils.format("[class:{}] has no [field:{}] exception", clazz, fieldName), e);
        }
    }

    /**
     * 从一个Class中获得具有指定注解的Method，只获取子类的Method，不获取父类的Method
     *
     * @param clazz      指定的Class
     * @param annotation 指定注解的Class
     * @return 数组，可能长度为0
     */
    public static Method[] getMethodsByAnnoInPOJOClass(Class<?> clazz, Class<? extends Annotation> annotation) {
        var list = new ArrayList<Method>();
        var methods = clazz.getDeclaredMethods();
        for (var method : methods) {
            if (method.isAnnotationPresent(annotation)) {
                list.add(method);
            }
        }
        return ArrayUtils.listToArray(list, Method.class);
    }

    public static Method[] getMethodsByNameInPOJOClass(Class<?> clazz, String methodName) {
        var list = new ArrayList<Method>();
        var methods = clazz.getDeclaredMethods();
        for (var method : methods) {
            if (method.getName().equalsIgnoreCase(methodName)) {
                list.add(method);
            }
        }
        return ArrayUtils.listToArray(list, Method.class);
    }

    /**
     * Attempt to get all Methods on the supplied class.
     * Searches all superclasses up to {@code Object}.
     *
     * @param clazz the class to introspect
     * @return 数组，可能长度为0
     */
    public static Method[] getAllMethods(Class<?> clazz) {
        AssertionUtils.notNull(clazz, "Class must not be null");
        var list = new ArrayList<Method>();
        var superClazz = clazz;
        while (superClazz != null) {
            var methods = superClazz.getDeclaredMethods();
            Collections.addAll(list, methods);
            superClazz = superClazz.getSuperclass();
        }
        return ArrayUtils.listToArray(list, Method.class);
    }
    //*************************************操作Class*********************************

    public static <T> T newInstance(Class<T> clazz) {
        try {
            return newInstance(clazz.getDeclaredConstructor());
        } catch (NoSuchMethodException e) {
            throw new RunException("[{}]无法被实例化", clazz);
        }
    }

    public static <T> T newInstance(Constructor<T> constructor) {
        try {
            return constructor.newInstance();
        } catch (Exception e) {
            throw new RunException("[{}]无法被实例化", constructor);
        }
    }


    /**
     * 等于{@link Field#get(Object)}
     * <p>
     * In accordance with {@link Field#get(Object)}
     * semantics, the returned value is automatically wrapped if the underlying field
     * has a primitive type.
     * </p>
     *
     * @param field  the field to get
     * @param target the target object from which to get the field
     * @return the field's current value
     */
    public static Object getField(Field field, Object target) {
        try {
            return field.get(target);
        } catch (Exception e) {
            throw new IllegalStateException("Unexpected reflection exception - " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * 等于{@link Field#set(Object, Object)}
     * <p>
     * In accordance with {@link Field#set(Object, Object)} semantics, the new value
     * is automatically unwrapped if the underlying field has a primitive type.
     * </p>
     *
     * @param field  the field to set
     * @param target the target object on which to set the field
     * @param value  the value to set; may be {@code null}
     */
    public static void setField(Field field, Object target, Object value) {
        try {
            field.set(target, value);
        } catch (Exception e) {
            throw new IllegalStateException("Unexpected reflection exception - " + e.getClass().getName() + ": " + e.getMessage());
        }
    }


    /**
     * Invoke the specified {@link Method} against the supplied target object with the
     * supplied arguments. The target object can be {@code null} when invoking a
     * static {@link Method}.
     *
     * @param target the target object to invoke the method on
     * @param method the method to invoke
     * @param args   the invocation arguments (may be {@code null})
     * @return the invocation result, if any
     */
    public static Object invokeMethod(Object target, Method method, Object... args) {
        try {
            return method.invoke(target, args);
        } catch (Exception e) {
            throw new IllegalStateException("Unexpected reflection exception - " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * 让私有变量可访问，在必要的情况下调用
     * <p>
     * Make the given field accessible, explicitly setting it accessible if necessary.
     * </p>
     *
     * @param field the field to make accessible
     * @see Field#setAccessible
     */
    public static void makeAccessible(Field field) {
        if ((!Modifier.isPublic(field.getModifiers()) || !Modifier.isPublic(field.getDeclaringClass().getModifiers()) || Modifier.isFinal(field.getModifiers()))) {
            field.setAccessible(true);
        }
    }

    /**
     * Make the given method accessible
     *
     * @param method the method to make accessible
     * @see Method#setAccessible
     */
    public static void makeAccessible(Method method) {
        if ((!Modifier.isPublic(method.getModifiers()) || !Modifier.isPublic(method.getDeclaringClass().getModifiers()))) {
            method.setAccessible(true);
        }
    }

    /**
     * Make the given constructor accessible, explicitly setting it accessible
     * if necessary. The {@code setAccessible(true)} method is only called
     * when actually necessary, to avoid unnecessary conflicts with a JVM
     * SecurityManager (if active).
     *
     * @param constructor the constructor to make accessible
     * @see Constructor#setAccessible
     */
    public static void makeAccessible(Constructor<?> constructor) {
        if ((!Modifier.isPublic(constructor.getModifiers()) || !Modifier.isPublic(constructor.getDeclaringClass().getModifiers()))) {
            constructor.setAccessible(true);
        }
    }


    public static String fieldToGetMethod(Class<?> clazz, Field field) {
        var fieldName = field.getName();

        assertIsStandardFieldName(field);

        var methodName = "get" + StringUtils.capitalize(fieldName);

        try {
            clazz.getDeclaredMethod(methodName, null);
            return methodName;
        } catch (NoSuchMethodException e) {
            // java的get方法对boolean值有可能对应get或者is，所以尝试获取两种不同的get方法，当两种都获取不到才抛异常
        }

        // 如果属性名的第一个字母是小写且第二个字母大写，那么该属性名直接用作 getter/setter。例如属性名为uName，对应的方法是getuName/setuName。
        // 如果属性名以大写字母开头，属性名直接用作 getter/setter 方法中 get/set 的后部分。例如属性名为Name，对应的方法是getName/setName。
        methodName = "get" + fieldName;
        try {
            clazz.getDeclaredMethod(methodName, null);
            return methodName;
        } catch (NoSuchMethodException e) {
        }

        methodName = "is" + StringUtils.capitalize(fieldName);
        try {
            clazz.getDeclaredMethod(methodName, null);
            return methodName;
        } catch (NoSuchMethodException e) {
            throw new RunException("field:[{}] has no getMethod or isMethod in class:[{}]", field.getName(), clazz.getCanonicalName());
        }
    }

    public static String fieldToSetMethod(Class<?> clazz, Field field) {
        var fieldName = field.getName();

        assertIsStandardFieldName(field);

        var methodName = "set" + StringUtils.capitalize(fieldName);
        try {
            clazz.getDeclaredMethod(methodName, field.getType());
            return methodName;
        } catch (NoSuchMethodException e) {
        }

        methodName = "set" + fieldName;
        try {
            clazz.getDeclaredMethod(methodName, field.getType());
            return methodName;
        } catch (NoSuchMethodException e) {
            throw new RunException("field:[{}] has no setMethod in class:[{}]", field.getName(), clazz.getCanonicalName());
        }
    }

}

