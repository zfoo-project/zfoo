package com.zfoo.storage.util;

import com.zfoo.storage.util.function.Func1;
import com.zfoo.storage.util.support.IdeaProxyLambdaMeta;
import com.zfoo.storage.util.support.LambdaMeta;
import com.zfoo.storage.util.support.ReflectLambdaMeta;
import com.zfoo.storage.util.support.SerializedLambda;
import com.zfoo.storage.util.support.ShadowLambdaMeta;

import java.io.Serializable;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.AccessController;

/**
 * @author veione
 * @version 1.0
 * @date 2023/9/12
 */
public final class LambdaUtils {
    private static final SimpleCache<Class, LambdaMeta> FUNC_CACHE = new SimpleCache<>(64);

    /**
     * 该缓存可能会在任意不定的时间被清除
     *
     * @param func 需要解析的 lambda 对象
     * @param <T>  类型，被调用的 Function 对象的目标类型
     * @return 返回解析后的结果
     */
    public static <T> LambdaMeta extract(Serializable func) {
        // 1. IDEA 调试模式下 lambda 表达式是一个代理
        if (func instanceof Proxy) {
            return new IdeaProxyLambdaMeta((Proxy) func);
        }
        // 2. 反射读取
        try {
            Method method = func.getClass().getDeclaredMethod("writeReplace");
            return new ReflectLambdaMeta((SerializedLambda) setAccessible(method).invoke(func), func.getClass().getClassLoader());
        } catch (Throwable e) {
            // 3. 反射失败使用序列化的方式读取
            return new ShadowLambdaMeta(SerializedLambda.extract(func));
        }
    }

    /**
     * 设置可访问对象的可访问权限为 true
     *
     * @param object 可访问的对象
     * @param <T>    类型
     * @return 返回设置后的对象
     */
    public static <T extends AccessibleObject> T setAccessible(T object) {
        return AccessController.doPrivileged(new SetAccessibleAction<>(object));
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
        return PropertyNamer.methodToProperty(getMethodName(func));
    }

    /**
     * 解析lambda表达式,加了缓存。
     * 该缓存可能会在任意不定的时间被清除
     *
     * @param func 需要解析的 lambda 对象
     * @return 返回解析后的结果
     */
    private static <T> LambdaMeta _resolve(Serializable func) {
        return FUNC_CACHE.get(func.getClass(), () -> extract(func));
    }
}
