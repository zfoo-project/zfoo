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

package com.zfoo.net.util.security;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.DigestUtils;

/**
 * @author godotg
 * @version 3.0
 */
public class MD5UtilsTest {

    @Test
    public void md5Test() {
        var str = "qwerasdfzxcv1234;:'";
        Assert.assertEquals(MD5Utils.bytesToMD5(str.getBytes()).toLowerCase(), DigestUtils.md5DigestAsHex(str.getBytes()));
    }

}
