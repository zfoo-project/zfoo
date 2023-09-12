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

package com.zfoo.storage.export;

import com.zfoo.protocol.util.FileUtils;
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.storage.interpreter.CsvReader;
import com.zfoo.storage.util.ExportUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * @author godotg
 */
@Ignore
public class ExportCsvTesting {

    @Test
    public void exportTest() throws Exception {
        var inputDir = "E:\\workspace\\zfoo\\storage\\src\\test\\resources\\excel";
        var outputDir = "E:\\workspace\\zfoo\\storage\\src\\test\\resources\\excel";
        ExportUtils.excel2csv(inputDir, outputDir);

        // godot4.x导入csv，莫名奇妙生成了一些translation文件，批量修改一下文件的名称
        var csvFiles = ExportUtils.scanCsvFiles(outputDir);
        csvFiles.stream().forEach(it -> it.renameTo(new File(StringUtils.format("{}.txt", it.getAbsolutePath()))));
        csvFiles.stream().forEach(it -> FileUtils.deleteFile(it));
    }

    @Test
    public void csvReadTest() throws IOException {
        var file = new File("C:\\Users\\workspace\\Desktop\\test\\StudentResource.csv");
        var list = CsvReader.readResourceDataFromCSV(FileUtils.openInputStream(file), file.getName());
        System.out.println(list);
    }

}
