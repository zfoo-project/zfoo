package com.zfoo.storage.util;

import com.zfoo.protocol.util.FieldUtils;
import com.zfoo.protocol.util.ReflectionUtils;
import com.zfoo.storage.util.function.Func1;
import com.zfoo.storage.util.lambda.*;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author veione
 * @version 1.0
 */
public final class LambdaUtils {
    private static final ConcurrentReferenceHashMap<Class, LambdaMeta> CACHE = new ConcurrentReferenceHashMap<>(64, 0.75F, 16, ConcurrentReferenceHashMap.ReferenceType.SOFT);

    /**
     * 该缓存可能会在任意不定的时间被清除
     *
     * @param func 需要解析的 lambda 对象
     * @return 返回解析后的结果
     */
    public static LambdaMeta extract(Serializable func) {
        // 1. IDEA 调试模式下 lambda 表达式是一个代理
        if (func instanceof Proxy) {
            return new IdeaProxyLambdaMeta((Proxy) func);
        }
        // 2. 反射读取
        try {
            Class<? extends Serializable> clazz = func.getClass();
            Method method = clazz.getDeclaredMethod("writeReplace");
            ReflectionUtils.makeAccessible(method);
            return new ReflectLambdaMeta((java.lang.invoke.SerializedLambda) method.invoke(func));
        } catch (Throwable e) {
            // 3. 反射失败使用序列化的方式读取
            return new ShadowLambdaMeta(SerializedLambda.extract(func));
        }
    }

    /**
     * 解析lambda表达式,加了缓存。
     * 该缓存可能会在任意不定的时间被清除
     *
     * @param <T>  Lambda类型
     * @param func 需要解析的 lambda 对象（无参方法）
     * @return 返回解析后的结果
     */
    public static <T> LambdaMeta resolve(Func1<T, ?> func) {
        return _resolve(func);
    }

    /**
     * 获取lambda表达式函数（方法）名称
     *
     * @param <T>  Lambda类型
     * @param func 函数（无参方法）
     * @return 函数名称
     */
    public static <T> String getMethodName(Func1<T, ?> func) {
        return resolve(func).getImplMethodName();
    }

    /**
     * 获取lambda表达式函数(字段)名称
     *
     * @param <T>  Lambda类型
     * @param func 函数
     * @return 字段名称
     */
    public static <T> String getFieldName(Func1<T, ?> func) {
        return FieldUtils.methodToProperty(getMethodName(func));
    }

    /**
     * 解析lambda表达式,加了缓存。
     * 该缓存可能会在任意不定的时间被清除
     *
     * @param func 需要解析的 lambda 对象
     * @return 返回解析后的结果
     */
    private static LambdaMeta _resolve(Serializable func) {
        return CACHE.computeIfAbsent(func.getClass(), c -> extract(func));
    }
}
