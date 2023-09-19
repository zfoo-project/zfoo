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

import com.zfoo.protocol.util.FieldUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author veione
 */
public class TestFieldUtils {

    @Test
    public void testPropertyName() {
        //前三种主要用于普通的POJO,第四种用于是record类型,方法名就是属性名
        String getName = FieldUtils.methodToProperty("getName");
        assertEquals(getName, "name");
        String setName = FieldUtils.methodToProperty("setName");
        assertEquals(setName, "name");
        String isName = FieldUtils.methodToProperty("isName");
        assertEquals(isName, "name");
        String name = FieldUtils.methodToProperty("name");
        assertEquals(name, "name");
        name = FieldUtils.methodToProperty("Name");
        assertEquals(name, "name");
    }
}
