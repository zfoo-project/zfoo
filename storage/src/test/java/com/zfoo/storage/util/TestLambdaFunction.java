/*
 * Copyright (C) 2020 The zfoo Authors
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.zfoo.storage.util;

import com.zfoo.storage.resource.TeacherResource;
import com.zfoo.storage.util.function.Func1;
import com.zfoo.storage.util.lambda.LambdaMeta;
import org.junit.Test;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * @author veione
 */
public class TestLambdaFunction {
    //https://blog.csdn.net/iteye_19045/article/details/119299015
    @Test
    public void testFuncSerialization() throws Exception {
        Func1<TeacherResource, String> func = TeacherResource::name;
        Object object = getObject(func);
        System.out.println(object);

        Class<? extends Serializable> clazz = func.getClass();
        Method writeReplace = clazz.getDeclaredMethod("writeReplace");
        System.out.println(writeReplace);

        LambdaMeta meta = LambdaUtils.extract(func);
        System.out.println(meta);

//        Function<StudentResource, String> func2 = StudentResource::name;
//        Method method = func2.getClass().getDeclaredMethod("writeReplace");
//        object = method.invoke(func2);
//        System.out.println(object);
    }

    private static Object getObject(Serializable func) throws Exception {
        Method method = func.getClass().getDeclaredMethod("writeReplace");
        Object object = method.invoke(func);
        return object;
    }
}
