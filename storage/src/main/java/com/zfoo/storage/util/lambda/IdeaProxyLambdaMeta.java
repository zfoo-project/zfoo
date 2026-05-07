package com.zfoo.storage.util.lambda;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandleProxies;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Executable;
import java.lang.reflect.Proxy;

/**
 * Handles Lambda metadata for expressions evaluated in IDEA's Evaluate dialog
 * <p>
 * Create by hcl at 2021/5/17
 */
public class IdeaProxyLambdaMeta implements LambdaMeta {
    private final Class<?> clazz;
    private final String name;

    public IdeaProxyLambdaMeta(Proxy func) {
        MethodHandle dmh = MethodHandleProxies.wrapperInstanceTarget(func);
        Executable executable = MethodHandles.reflectAs(Executable.class, dmh);
        clazz = executable.getDeclaringClass();
        name = executable.getName();
    }

    @Override
    public String getImplMethodName() {
        return name;
    }

    @Override
    public String toString() {
        return clazz.getSimpleName() + "::" + name;
    }

}
