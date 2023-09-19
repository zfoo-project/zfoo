package com.zfoo.storage.util.lambda;

/**
 * Lambda 信息
 * <p>
 * Created by hcl at 2021/5/14
 */
public interface LambdaMeta {

    /**
     * 获取 lambda 表达式实现方法的名称
     *
     * @return lambda 表达式对应的实现方法名称
     */
    String getImplMethodName();

}
