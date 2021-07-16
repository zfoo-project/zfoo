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
 *
 */

package com.zfoo.protocol.util;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Set;

/**
 * @author jaysunxiao
 * @version 3.0
 */
@Ignore
public class ClassUtilTest {

    // ClassUtilTest
    @Test
    public void classLocation() {
        String str = ClassUtils.classLocation(Integer.class);
        Assert.assertEquals("jrt:/java.base/java/lang/Integer.class", str);
    }

    @Test
    public void getAllClasses() throws Exception {
        System.out.println(StringUtils.MULTIPLE_HYPHENS);
        System.out.println("某个包下的所有类查找测试：");
        Set<Class<?>> set = ClassUtils.getAllClasses("com.zfoo");
        for (Class<?> clazz : set) {
            System.out.println(clazz.getName());
        }
        System.out.println(StringUtils.MULTIPLE_HYPHENS);
    }

    @Test
    public void getClassPath() {
        System.out.println(ClassUtils.getClassAbsPath(ClassUtilTest.class));
    }


    @Test
    public void getClassFromClassPath() throws IOException {
        System.out.println(new String(IOUtils.toByteArray(ClassUtils.getFileFromClassPath("com.zfoo.util.ClassUtilsTest"))));
    }

}
