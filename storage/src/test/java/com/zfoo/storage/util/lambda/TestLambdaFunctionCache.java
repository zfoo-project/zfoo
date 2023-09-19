package com.zfoo.storage.util.lambda;

import com.zfoo.storage.resource.StudentResource;
import com.zfoo.storage.util.LambdaUtils;
import com.zfoo.storage.util.function.Func1;
import org.junit.Test;

/**
 * @author veione
 */
public class TestLambdaFunctionCache {

    @Test
    public void testFunctionCache() {
        Func1<StudentResource, Object> nameFunc = StudentResource::getName;
        System.out.println(LambdaUtils.getMethodName(nameFunc));
        System.out.println(LambdaUtils.getMethodName(nameFunc));
    }
}
