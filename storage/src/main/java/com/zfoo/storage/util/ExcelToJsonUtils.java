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

package com.zfoo.storage.util;

import com.zfoo.protocol.exception.RunException;
import com.zfoo.protocol.util.JsonUtils;
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.storage.interpreter.ResourceConfig;
import com.zfoo.storage.interpreter.ResourceConfig.Header;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 * @author meiwei666
 * @version 3.0
 */
public class ExcelToJsonUtils {
    
    public static void excelConvertJson(String inputDir, String outputDir) throws Exception{
        var listFiles = FileUtils.listFiles(new File(inputDir), new String[] { "xls", "xlsx"}, true);
        for (var file : listFiles) {
            var fileName = getFileName(file);
            var inputStream = FileUtils.openInputStream(file);
            var jsonStr = read(inputStream, fileName);
            writeJsonFile(outputDir, jsonStr, fileName);
        }
    }
    
    private static void writeJsonFile(String outDir, String jsonStr, String name) {
        System.out.println("resource: " + name + ".txt");
        PrintWriter pw = null;
        try {
            File outFile = new File(outDir, name + ".txt");
            if (!outFile.exists())
                outFile.createNewFile();
            pw = new PrintWriter(outFile, "utf-8");
            pw.write(jsonStr);
        } catch (IOException e) {
            System.err.println("error resource:" + name);
        } finally {
            if (pw != null)
                pw.close();
        }
    }
    
    private static String getFileName(File file) {
        String name = file.getName();
        int index = name.lastIndexOf(".");
        if (index <= 0) {
            return "";
        }
        return name.substring(0, index);
    }

    public static String read(InputStream inputStream, String fileName) {
        var wb = createWorkbook(inputStream, fileName);
        var resource = new ResourceConfig();
        resource.setName(fileName);
        // 默认取到第一个sheet页
        var sheet = wb.getSheetAt(0);
        //设置所有列
        var headers = getHeaders(sheet, fileName);
        resource.setHeader(headers);

        // 行数定位到有效数据行，默认是第四行为有效数据行
        var iterator = sheet.iterator();
        iterator.next();
        iterator.next();
        iterator.next();
        // 从ROW_SERVER这行开始读取数据
        List<List<String>> data = new ArrayList<>();
        while (iterator.hasNext()) {
            var row = iterator.next();
            List<String> rowData = new ArrayList<>();
            for (var header : headers) {
                var cell = row.getCell(header.getIndex());
                var content = CellUtils.getCellStringValue(cell);
                rowData.add(content);
            }
            data.add(rowData);
        }
        resource.setData(data);
        return JsonUtils.object2StringPrettyPrinter(resource);
    }
    
    // 只读取代码里写的字段
    private static List<Header> getHeaders(Sheet sheet, String fileName) {
        var iterator = sheet.iterator();
        // 获取配置表的有效列名称，默认第一行就是字段名称
        var fieldRow = iterator.next();
        if (fieldRow == null) {
            throw new RunException("无法获取资源[class:{}]的Excel文件的属性控制列", fileName);
        }
        //默认第二行字段类型
        var typeRow = iterator.next();
        if (typeRow == null) {
            throw new RunException("无法获取资源[class:{}]的Excel文件的类型控制列", fileName);
        }

        var headerList = new ArrayList<Header>();
        var cellFieldMap = new HashMap<String, Integer>();
        for (var i = 0; i < fieldRow.getLastCellNum(); i++) {
            var fieldCell = fieldRow.getCell(i);
            if (Objects.isNull(fieldCell)) {
                continue;
            }
            var typeCell = typeRow.getCell(i);
            if (Objects.isNull(typeCell)) {
                continue;
            }
            var fieldName = CellUtils.getCellStringValue(fieldCell);
            if (StringUtils.isEmpty(fieldName)) {
                continue;
            }
            var typeName = CellUtils.getCellStringValue(typeCell);
            if (StringUtils.isEmpty(typeName)) {
                continue;
            }
            var previousValue = cellFieldMap.put(fieldName, i);
            if (Objects.nonNull(previousValue)) {
                throw new RunException("资源[class:{}]的Excel文件出现重复的属性控制列[field:{}]", fileName, fieldName);
            }
            headerList.add(new Header(fieldName, typeName, i));
        }
       return headerList;
    }

    private static Workbook createWorkbook(InputStream input, String fileName) {
        try {
            return WorkbookFactory.create(input);
        } catch (IOException e) {
            throw new RunException("静态资源[{}]异常，无法读取文件", fileName);
        }
    }
 
}
