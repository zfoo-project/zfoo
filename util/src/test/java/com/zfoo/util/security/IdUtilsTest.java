/*
 * Copyright (C) 2020 The zfoo Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.zfoo.util.security;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author godotg
 * @version 3.0
 */
public class IdUtilsTest {

    @Test
    public void test() {
        var atomicInteger = new AtomicInteger(Integer.MAX_VALUE);
        atomicInteger.getAndIncrement();
        Assert.assertEquals(atomicInteger.get(), Integer.MIN_VALUE);
    }

}
