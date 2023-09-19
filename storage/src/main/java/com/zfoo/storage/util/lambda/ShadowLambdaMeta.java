package com.zfoo.storage.util.lambda;

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

}
