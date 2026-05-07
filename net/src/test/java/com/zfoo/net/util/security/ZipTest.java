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

/**
 * @author godotg
 */
public class ZipTest {

    // ZIP compression algorithm test
    @Test
    public void test() {
        String str = "ZIP is a file compression algorithm, originally named Deflate, invented by Phil Katz." +
                "He published the format specification in December 1989. ZIP files typically use the suffix '.zip'." +
                "Currently, ZIP is one of the mainstream compression formats, competing with RAR and the open-source 7-Zip format." +
                "RAR has a higher compression ratio than ZIP, but takes much longer to compress." +
                "7-Zip (7z) is increasingly adopted due to its free compression tools.";

        byte[] bytes = str.getBytes();

        // Length before compression
        Assert.assertEquals(bytes.length, 555);

        bytes = ZipUtils.zip(bytes);
        // Length after compression
        Assert.assertEquals(bytes.length, 438);

        bytes = ZipUtils.unZip(bytes);

        Assert.assertEquals(new String(bytes), str);
    }


    @Test
    public void zipTest() {
        var sourceFiles = new String[]{"./src","./README.md"};
        ZipUtils.zip(sourceFiles, "./my-source.zip");
    }

    @Test
    public void unzipTest() {
        ZipUtils.unzip("D:\\Project\\aaa\\ai-simulator.zip", "D:\\Project\\aaa\\bbb");
    }
}
