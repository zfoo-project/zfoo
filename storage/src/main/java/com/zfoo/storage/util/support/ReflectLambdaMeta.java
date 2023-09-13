package com.zfoo.storage.util.support;

import com.zfoo.protocol.util.ClassUtils;
import com.zfoo.protocol.util.StringUtils;

/**
 * Created by hcl at 2021/5/14
 */
public class ReflectLambdaMeta implements LambdaMeta {
    private final SerializedLambda lambda;

    private final ClassLoader classLoader;

    public ReflectLambdaMeta(SerializedLambda lambda, ClassLoader classLoader) {
        this.lambda = lambda;
        this.classLoader = classLoader;
    }

    @Override
    public String getImplMethodName() {
        return lambda.getImplMethodName();
    }

    @Override
    public Class<?> getInstantiatedClass() {
        String instantiatedMethodType = lambda.getInstantiatedMethodType();
        String instantiatedType = instantiatedMethodType.substring(2, instantiatedMethodType.indexOf(StringUtils.SEMICOLON)).replace(StringUtils.SLASH, StringUtils.PERIOD);
        return ClassUtils.toClassConfident(instantiatedType, this.classLoader);
    }
}