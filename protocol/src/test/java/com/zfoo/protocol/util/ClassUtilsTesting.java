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

import com.zfoo.protocol.packet.ComplexObject;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

/**
 * @author godotg
 */
@Ignore
public class ClassUtilsTesting {

    @Test
    public void getClassPath() {
        System.out.println(ClassUtils.getClassAbsPath(ClassUtilsTesting.class));
    }

    @Test
    public void getClassFromClassPath() throws IOException {
        System.out.println(new String(IOUtils.toByteArray(ClassUtils.getFileFromClassPath("csTest/CsProtocolTest.cs"))));
    }

    @Test
    public void relevantClassTest() throws IOException {
        var set = ClassUtils.relevantClass(ComplexObject.class);
        for (var clazz : set) {
            System.out.println(clazz);
        }
    }

}
