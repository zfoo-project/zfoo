package com.zfoo.storage.util.support;

import com.zfoo.protocol.util.StringUtils;
import com.zfoo.protocol.util.ClassUtils;

/**
 * 基于 {@link SerializedLambda} 创建的元信息
 * <p>
 * Create by hcl at 2021/7/7
 */
public class ShadowLambdaMeta implements LambdaMeta {
    private final SerializedLambda lambda;

    public ShadowLambdaMeta(SerializedLambda lambda) {
        this.lambda = lambda;
    }

    @Override
    public String getImplMethodName() {
        return lambda.getImplMethodName();
    }

    @Override
    public Class<?> getInstantiatedClass() {
        String instantiatedMethodType = lambda.getInstantiatedMethodType();
        String instantiatedType = instantiatedMethodType.substring(2, instantiatedMethodType.indexOf(StringUtils.SEMICOLON)).replace(StringUtils.SLASH, StringUtils.PERIOD);
        return ClassUtils.toClassConfident(instantiatedType, lambda.getCapturingClass().getClassLoader());
    }

}
