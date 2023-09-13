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
import com.zfoo.storage.interpreter.data.StorageData;
import com.zfoo.storage.interpreter.data.StorageHeader;
import com.zfoo.storage.util.CellUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @author meiwei666
 */
public abstract class ExcelReader {

    public static StorageData readResourceDataFromExcel(InputStream inputStream, String resourceClassName) {
        // 只读取代码里写的字段
        var wb = createWorkbook(inputStream, resourceClassName);
        // 默认取到第一个sheet页
        var sheet = wb.getSheetAt(0);
        var iterator = sheet.iterator();
        //设置所有列
        var headers = getHeaders(iterator, resourceClassName);

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
        return StorageData.valueOf(resourceClassName, headers, rows);
    }

    private static List<StorageHeader> getHeaders(Iterator<Row> iterator, String resourceClassName) {
        // 获取配置表的有效列名称，默认第一行就是字段名称
        var fieldRow = iterator.next();
        if (fieldRow == null) {
            throw new RunException("Failed to get attribute control column from excel file of resource [class:{}]", resourceClassName);
        }
        // 默认第二行字段类型
        var typeRow = iterator.next();
        if (typeRow == null) {
            throw new RunException("Failed to get type control column from excel file of resource [class:{}]", resourceClassName);
        }
        // 默认第三行为描述，需要的时候再使用
        var desRow = iterator.next();
        var headerList = new ArrayList<StorageHeader>();
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
            var excelFieldName = CellUtils.getCellStringValue(fieldCell);
            if (StringUtils.isEmpty(excelFieldName)) {
                continue;
            }
            var typeName = CellUtils.getCellStringValue(typeCell);
            if (StringUtils.isEmpty(typeName)) {
                continue;
            }
            var previousValue = cellFieldMap.put(excelFieldName, i);
            if (Objects.nonNull(previousValue)) {
                throw new RunException("There are duplicate attribute control columns [field:{}] in the Excel file of the resource [class:{}]", excelFieldName,resourceClassName);
            }
            headerList.add(StorageHeader.valueOf(excelFieldName, typeName, i));
        }
        return headerList;
    }

    private static Workbook createWorkbook(InputStream input, String fileName) {
        try {
            return WorkbookFactory.create(input);
        } catch (IOException e) {
            throw new RunException("Static excel resource [{}] is abnormal, and the file cannot be read", fileName, e);
        }
    }
}
