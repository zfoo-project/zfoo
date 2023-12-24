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

import com.zfoo.monitor.util.OSUtils;
import com.zfoo.protocol.util.FileUtils;
import com.zfoo.protocol.util.StringUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

/**
 * @author godotg
 */
@Ignore
public class Gcc {

    public static final String mainCpp = "main";

    @Test
    public void test() {
        FileUtils.createDirectory("bin");

        FileUtils.deleteFile(new File((StringUtils.format("bin/{}.exe", mainCpp))));

        var proPath = new File(FileUtils.getProAbsPath()).getAbsolutePath();
        System.out.println(StringUtils.format("proPath = [{}]", proPath));


        var files = FileUtils.getAllReadableFiles(new File(proPath))
                .stream()
                .filter(it -> it.getName().endsWith(".h") || it.getName().endsWith(".cpp"))
                .map(it -> StringUtils.substringAfterFirst(it.getAbsolutePath(), proPath))
                .map(it -> {
                    if (it.startsWith("/")) {
                        return StringUtils.substringAfterFirst(it, "/");
                    }
                    if (it.startsWith("\\")) {
                        return StringUtils.substringAfterFirst(it, "\\");
                    }
                    return it;
                })
                .filter(it -> it.contains("/") || it.contains("\\"))
                .filter(it -> !it.startsWith("cmake-build-debug"))
                .toArray();

        var cppFiles = StringUtils.joinWith(StringUtils.SPACE, files);
        var gppCommand = StringUtils.format("g++ -I ./ {}.cpp {} -o bin/{}", mainCpp, cppFiles, mainCpp);

        System.out.println(StringUtils.format("[{}]", gppCommand));
        System.out.println("------------------------------------------------------");

        var result = OSUtils.execCommand(gppCommand);
        System.out.println(result);
        System.out.println("------------------------------------------------------");

        result = OSUtils.execCommand(StringUtils.format("bin/{}.exe", mainCpp));
        System.out.println(result);

        System.exit(0);
    }

}
