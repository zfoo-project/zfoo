package com.zfoo.storage;

import com.zfoo.storage.resource.StudentResource;
import com.zfoo.storage.util.LambdaUtils;
import com.zfoo.storage.util.function.Func1;
import org.junit.Test;

/**
 * @author veione
 * @version 1.0.0
 */
public class TestLambdaFunctionCache {

    @Test
    public void testFunctionCache() {
        Func1<StudentResource, Object> nameFunc = StudentResource::getName;
        System.out.println(LambdaUtils.getMethodName(nameFunc));
        System.out.println(LambdaUtils.getMethodName(nameFunc));
    }
}
