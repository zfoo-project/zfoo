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

import java.beans.Transient;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;


/**
 * Reflection utility class
 *
 * @author godotg
 */
public abstract class ReflectionUtils {

    /**
     * If the field matches the FieldFilter, invoke the callback
     *
     * @param field         the field
     * @param fieldFilter   field filter
     * @param fieldCallback field callback
     */
    public static void filterField(Field field, Predicate<Field> fieldFilter, Consumer<Field> fieldCallback) {
        if (fieldFilter != null && !fieldFilter.test(field)) {
            return;
        }
        fieldCallback.accept(field);
    }

    /**
     * Filter fields of clazz using filter and invoke callback on matched fields
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
        return clazz.getSuperclass().equals(Object.class) || clazz.isRecord();
    }

    public static void assertIsPojoClass(Class<?> clazz) {
        if (!isPojoClass(clazz)) {
            throw new RunException("[class:{}] is not a simple JavaBean (POJO cannot extend a class, but can implement interfaces)", clazz.getName());
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
     * Standard field names are more portable; the 'is' prefix should be avoided to keep getters/setters consistent across languages
     */
    public static void assertIsStandardFieldName(Field field) {
        var fieldName = field.getName();
        if (fieldName.startsWith("is")) {
            throw new RunException("to avoid different get or set method in different language, [field:{}] can not be started with name of 'is' in class:[{}]"
                    , field.getName(), field.getDeclaringClass().getCanonicalName());
        }
    }

    /**
     * Get fields with the specified annotation from a POJO class; only current class fields, not inherited
     *
     * @param clazz      the target class
     * @param annotation the annotation class
     * @return array, possibly empty
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
     * Get methods with the specified annotation from a class; only current class methods, not inherited
     *
     * @param clazz      the target class
     * @param annotation the annotation class
     * @return array, possibly empty
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

    public static Method getMethodByNameInPOJOClass(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            return clazz.getDeclaredMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(StringUtils.format("[class:{}] has no [method:{}] exception", clazz, methodName), e);
        }
    }


    /**
     * Attempt to get all Methods on the supplied class.
     * Searches all superclasses up to {@code Object}.
     *
     * @param clazz the class to introspect
     * @return array, possibly empty
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
    //*********************************** Class operations ***********************************

    public static <T> T newInstance(Class<T> clazz) {
        try {
            return newInstance(clazz.getDeclaredConstructor());
        } catch (NoSuchMethodException e) {
            throw new RunException("[{}] cannot be instantiated", clazz);
        }
    }

    public static <T> T newInstance(Constructor<T> constructor) {
        try {
            return constructor.newInstance();
        } catch (Exception e) {
            throw new RunException("[{}] cannot be instantiated", constructor);
        }
    }

    public static <T> T newInstance(Constructor<T> constructor, Object... initargs) {
        try {
            return constructor.newInstance(initargs);
        } catch (Exception e) {
            throw new RunException("[{}] cannot be instantiated", constructor);
        }
    }


    /**
     * Equivalent to {@link Field#get(Object)}
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
     * Equivalent to {@link Field#set(Object, Object)}
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
     * Make a private field accessible; call only when necessary
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

    // Get regular (non-static, non-synthetic) fields from class
    public static List<Field> notStaticAndTransientFields(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(it -> !Modifier.isStatic(it.getModifiers()))
                .filter(it -> !Modifier.isTransient(it.getModifiers()))
                .filter(it -> !it.isAnnotationPresent(Transient.class))
                .toList();
    }

    public static <T> Constructor<T> getConstructor(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new RunException("default constructor:[{}] not exists in class:[{}]", clazz.getCanonicalName());
        }
    }

    public static <T> Constructor<T> getConstructor(Class<T> clazz, Class[] params) {
        try {
            return clazz.getConstructor(params);
        } catch (NoSuchMethodException e) {
            throw new RunException("constructor:[{}] has no setMethod in class:[{}]", params.length, clazz.getCanonicalName());
        }
    }

    /**
     * simple convert to simple primitive type, String
     */
    public static Object convertSimple(String value, Class<?> targetType) {
        if (StringUtils.isBlank(value)) {
            return null;
        }

        if (targetType == String.class) {
            return value;
        } else if (targetType == byte.class || targetType == Byte.class) {
            return Byte.parseByte(value);
        } else if (targetType == short.class || targetType == Short.class) {
            return Short.parseShort(value);
        } else if (targetType == int.class || targetType == Integer.class) {
            return Integer.parseInt(value);
        } else if (targetType == long.class || targetType == Long.class) {
            return Long.parseLong(value);
        } else if (targetType == float.class || targetType == Float.class) {
            return Float.parseFloat(value);
        } else if (targetType == double.class || targetType == Double.class) {
            return Double.parseDouble(value);
        } else if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.parseBoolean(value);
        }
        return null;
    }

}

