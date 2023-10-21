package com.zfoo.protocol.util;

import com.zfoo.protocol.exception.RunException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;

/**
 * 属性工具类
 *
 * @author veione
 */
public abstract class FieldUtils {

    public static String fieldToGetMethod(Class<?> clazz, Field field) {
        var fieldName = field.getName();
        ReflectionUtils.assertIsStandardFieldName(field);

        var methodName = "get" + StringUtils.capitalize(fieldName);

        if (clazz.isRecord()) {
            methodName = fieldName;
        }

        try {
            clazz.getDeclaredMethod(methodName);
            return methodName;
        } catch (NoSuchMethodException e) {
            // java的get方法对boolean值有可能对应get或者is，所以尝试获取两种不同的get方法，当两种都获取不到才抛异常
        }

        // 如果属性名的第一个字母是小写且第二个字母大写，那么该属性名直接用作 getter/setter。例如属性名为uName，对应的方法是getuName/setuName。
        // 如果属性名以大写字母开头，属性名直接用作 getter/setter 方法中 get/set 的后部分。例如属性名为Name，对应的方法是getName/setName。
        methodName = "get" + fieldName;
        try {
            clazz.getDeclaredMethod(methodName);
            return methodName;
        } catch (NoSuchMethodException e) {
        }

        methodName = "is" + StringUtils.capitalize(fieldName);
        try {
            clazz.getDeclaredMethod(methodName);
            return methodName;
        } catch (NoSuchMethodException e) {
            throw new RunException("field:[{}] has no getMethod or isMethod in class:[{}]", field.getName(), clazz.getCanonicalName());
        }
    }

    public static String fieldToSetMethod(Class<?> clazz, Field field) {
        var fieldName = field.getName();

        ReflectionUtils.assertIsStandardFieldName(field);

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

    public static String getMethodToField(Class<?> clazz, String methodName) {
        try {
            // 查看clazz时候真的有methodName方法
            clazz.getDeclaredMethod(methodName);
        } catch (NoSuchMethodException e) {
            throw new RunException("clazz:[{}] has no getMethod:[{}]", clazz.getSimpleName(), methodName);
        }

        var fieldName = methodName;
        if (clazz.isRecord()) {
            try {
                clazz.getDeclaredField(fieldName);
                return fieldName;
            } catch (NoSuchFieldException e) {
                throw new RunException("record clazz:[{}] has no field:[{}]", clazz.getSimpleName(), fieldName);
            }
        }

        // get method
        fieldName = StringUtils.substringAfterFirst(methodName, "get");
        try {
            clazz.getDeclaredField(fieldName);
            return fieldName;
        } catch (NoSuchFieldException e) {
        }

        fieldName = StringUtils.uncapitalize(fieldName);
        try {
            clazz.getDeclaredField(fieldName);
            return fieldName;
        } catch (NoSuchFieldException e) {
        }

        // is method
        fieldName = StringUtils.substringAfterFirst(methodName, "is");
        try {
            clazz.getDeclaredField(fieldName);
            return fieldName;
        } catch (NoSuchFieldException e) {
        }

        fieldName = StringUtils.uncapitalize(fieldName);
        try {
            clazz.getDeclaredField(fieldName);
            return fieldName;
        } catch (NoSuchFieldException e) {
            throw new RunException("clazz:[{}] has no field for getMethod:[{}]", clazz.getSimpleName(), methodName);
        }
    }

}
