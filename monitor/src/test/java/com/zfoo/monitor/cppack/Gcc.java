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

package com.zfoo.monitor.cppack;

import com.sun.jna.Platform;
import com.zfoo.protocol.util.FileUtils;
import com.zfoo.protocol.util.StringUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author godotg
 */
@Ignore
public class Gcc {

    public static final String sourcePath = "D:\\github\\cutil";
    public static final List<String> excludePaths = List.of(
            "D:\\github\\cutil\\thirdparty"
            , "D:\\github\\cutil\\test");
    public static final String mainCpp = "main.cpp";
    public static final String appName = "myapp";

    // https://blog.csdn.net/m0_37605642/article/details/127170731
    @Test
    public void test() {
        System.out.println(StringUtils.format("source = [{}]", sourcePath));

        var excludeFiles = excludePaths.stream()
                .map(it -> new File(it))
                .filter(it -> it.exists())
                .map(it -> it.getAbsolutePath())
                .toList();

        var cppFiles = FileUtils.getAllReadableFiles(new File(sourcePath))
                .stream()
                .filter(it -> it.getName().endsWith(".h") || it.getName().endsWith(".cpp"))
                .map(it -> it.getAbsolutePath())
                .filter(it -> !it.contains("cmake-build-debug"))
                .filter(it -> excludeFiles.stream().noneMatch(it0 -> it.startsWith(it0)))
                .map(it -> StringUtils.substringAfterFirst(it, sourcePath))
                .collect(Collectors.joining(StringUtils.SPACE));


        // 链接windows的网络库
        var libraryLink = new StringBuilder();
        if (Platform.isWindows()) {
            libraryLink.append("-lws2_32");
        }

        var gppCommand = StringUtils.format("g++ -std=c++20 {} -o {} {}", mainCpp, appName, libraryLink.toString());
        System.out.println(gppCommand);
    }

}
