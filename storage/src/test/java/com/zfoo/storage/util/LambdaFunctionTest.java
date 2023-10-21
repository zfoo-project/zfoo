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

import com.zfoo.storage.resource.StudentResource;
import com.zfoo.storage.resource.TeacherResource;
import com.zfoo.storage.util.function.Func1;
import org.junit.Assert;
import org.junit.Test;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * @author veione
 */
public class LambdaFunctionTest {


    // https://blog.csdn.net/iteye_19045/article/details/119299015
    @Test
    public void testFuncSerialization() throws Exception {
        Func1<TeacherResource, String> func = TeacherResource::name;
        Object object = getObject(func);
        System.out.println(object);

        Class<? extends Serializable> clazz = func.getClass();
        Method writeReplace = clazz.getDeclaredMethod("writeReplace");
        System.out.println(writeReplace);

//        Function<StudentResource, String> func2 = StudentResource::name;
//        Method method = func2.getClass().getDeclaredMethod("writeReplace");
//        object = method.invoke(func2);
//        System.out.println(object);
    }


    @Test
    public void testFunctionCache() {
        Func1<StudentResource, Object> nameFunc1 = StudentResource::getName;
        Func1<StudentResource, Object> nameFunc2 = StudentResource::getName;
        Assert.assertNotEquals(nameFunc1, nameFunc2);
    }

    private static Object getObject(Serializable func) throws Exception {
        Method method = func.getClass().getDeclaredMethod("writeReplace");
        Object object = method.invoke(func);
        return object;
    }
}
