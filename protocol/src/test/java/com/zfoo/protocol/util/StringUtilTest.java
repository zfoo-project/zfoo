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
import org.junit.Test;

/**
 * @author godotg
 */
public class StringUtilTest {

    @Test
    public void formatTest() {
        String str = StringUtils.format("this is {} for {}", "a", "b");
        Assert.assertEquals("this is a for b", str);
    }

    @Test
    public void isEmpty() {
        Assert.assertFalse(StringUtils.isEmpty("  "));
    }

    @Test
    public void capitalize() {
        String str = "hello world!";
        Assert.assertEquals(StringUtils.capitalize(str), "Hello world!");
    }

    @Test
    public void unCapitalize() {
        String str = "Hello world!";
        Assert.assertEquals(StringUtils.uncapitalize(str), "hello world!");
    }

}
