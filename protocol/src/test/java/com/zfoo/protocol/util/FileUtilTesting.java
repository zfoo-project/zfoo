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

import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author godotg
 */
@Ignore
public class FileUtilTesting {

    @Test
    public void absPathTest() {
        var absPath = FileUtils.getProAbsPath();
        System.out.println(absPath);
    }

    @Test
    public void createFile() throws IOException {
        FileUtils.createFile(FileUtils.getProAbsPath() + File.separator + "hello", "hhh");
    }

    @Test
    public void deleteFile() {
        FileUtils.deleteFile(new File(FileUtils.getProAbsPath() + File.separator + "hello"));
    }

    @Test
    public void writeFile() {
        FileUtils.writeStringToFile(new File(FileUtils.getProAbsPath() + File.separator + "test.txt"), "hello world!", true);
    }


    @Test
    public void readFile() {
        String str = FileUtils.readFileToString(new File(FileUtils.getProAbsPath() + File.separator + "test.txt"));
        System.out.println(str);
    }


    @Test
    public void getProjectPath() {
        System.out.println(FileUtils.getProAbsPath());
    }


    @Test
    public void searchFile() {
        FileUtils.searchFileInProject(new File(FileUtils.getProAbsPath()));
    }

    @Test
    public void getAllFiles() {
        List<File> list = FileUtils.getAllReadableFiles(new File(FileUtils.getProAbsPath()));
        for (File file : list) {
            System.out.println(file.getName());
        }
    }

    @Test
    public void searchFileInProject() {
        System.out.println(FileUtils.searchFileInProject("User"));
    }

}
