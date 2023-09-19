package com.zfoo.storage.util.lambda;

/**
 * Created by hcl at 2021/5/14
 */
public class ReflectLambdaMeta implements LambdaMeta {
    private final java.lang.invoke.SerializedLambda lambda;

    public ReflectLambdaMeta(java.lang.invoke.SerializedLambda lambda) {
        this.lambda = lambda;
    }

    @Override
    public String getImplMethodName() {
        return lambda.getImplMethodName();
    }
}