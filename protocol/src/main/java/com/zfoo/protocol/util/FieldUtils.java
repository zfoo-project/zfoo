package com.zfoo.protocol.util;

import com.zfoo.protocol.exception.RunException;

import java.lang.reflect.Field;

/**
 * Field utility class
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
            // Java getters for booleans may use 'get' or 'is'; try both and throw only if neither exists
        }

        // If the first letter is lowercase and the second uppercase, use the name as-is. E.g., 'uName' -> 'getuName'/'setuName'.
        // If the field name starts with an uppercase letter, append it directly to get/set. E.g., 'Name' -> 'getName'/'setName'.
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
            throw new RunException("field:[{}] has no get or set method in class:[{}]", field.getName(), clazz.getCanonicalName());
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
            // Check if clazz actually has a method named methodName
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
