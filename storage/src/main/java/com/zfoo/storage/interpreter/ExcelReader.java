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

package com.zfoo.storage.interpreter;

import com.zfoo.protocol.exception.RunException;
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.storage.model.resource.ResourceData;
import com.zfoo.storage.model.resource.ResourceHeader;
import com.zfoo.storage.util.CellUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @author meiwei666
 * @version 4.0
 */
public abstract class ExcelReader {

    public static ResourceData readResourceDataFromExcel(InputStream inputStream, String fileName) {
        // 只读取代码里写的字段
        var wb = createWorkbook(inputStream, fileName);
        // 默认取到第一个sheet页
        var sheet = wb.getSheetAt(0);
        var iterator = sheet.iterator();
        //设置所有列
        var headers = getHeaders(iterator, fileName);

        var rows = new ArrayList<List<String>>();
        while (iterator.hasNext()) {
            var row = iterator.next();

            var idCell = row.getCell(0);
            if (StringUtils.isBlank(CellUtils.getCellStringValue(idCell))) {
                continue;
            }

            var columns = new ArrayList<String>();
            for (var header : headers) {
                var cell = row.getCell(header.getIndex());
                var content = CellUtils.getCellStringValue(cell);
                columns.add(content);
            }
            rows.add(columns);
        }
        return ResourceData.valueOf(fileName, headers, rows);
    }

    private static List<ResourceHeader> getHeaders(Iterator<Row> iterator, String fileName) {
        // 获取配置表的有效列名称，默认第一行就是字段名称
        var fieldRow = iterator.next();
        if (fieldRow == null) {
            throw new RunException("无法获取[{}]文件的属性控制列", fileName);
        }
        var headerList = new ArrayList<ResourceHeader>();
        var cellFieldMap = new HashMap<String, Integer>();
        for (var i = 0; i < fieldRow.getLastCellNum(); i++) {
            var fieldCell = fieldRow.getCell(i);
            if (Objects.isNull(fieldCell)) {
                continue;
            }
            var fieldName = CellUtils.getCellStringValue(fieldCell);
            if (StringUtils.isEmpty(fieldName)) {
                continue;
            }
            var previousValue = cellFieldMap.put(fieldName, i);
            if (Objects.nonNull(previousValue)) {
                throw new RunException("[{}]文件出现重复的属性控制列[field:{}]", fileName, fieldName);
            }
            headerList.add(ResourceHeader.valueOf(fieldName, i));
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
