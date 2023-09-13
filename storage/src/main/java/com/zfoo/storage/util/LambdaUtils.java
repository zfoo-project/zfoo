package com.zfoo.storage.util;

import com.zfoo.storage.util.support.IdeaProxyLambdaMeta;
import com.zfoo.storage.util.support.LambdaMeta;
import com.zfoo.storage.util.support.ReflectLambdaMeta;
import com.zfoo.storage.util.support.SerializableFunction;
import com.zfoo.storage.util.support.SerializedLambda;
import com.zfoo.storage.util.support.ShadowLambdaMeta;

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


    /**
     * 该缓存可能会在任意不定的时间被清除
     *
     * @param func 需要解析的 lambda 对象
     * @param <T>  类型，被调用的 Function 对象的目标类型
     * @return 返回解析后的结果
     */
    public static <T> LambdaMeta extract(SerializableFunction<T, ?> func) {
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
}
