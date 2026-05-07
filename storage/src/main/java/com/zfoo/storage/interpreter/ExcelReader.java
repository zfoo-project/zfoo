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

import java.io.InputStream;
import java.util.*;

/**
 * @author meiwei666
 */
public abstract class ExcelReader {

    public static StorageData readResourceDataFromExcel(InputStream inputStream, String resourceClassName) {
        // Only read fields declared in the code
        var wb = createWorkbook(inputStream, resourceClassName);
        // Read the first worksheet by default
        var sheet = wb.getSheetAt(0);
        var iterator = sheet.iterator();
        // Set all columns
        var excelHeaders = getHeaders(iterator, resourceClassName);

        var rows = new ArrayList<List<String>>();
        while (iterator.hasNext()) {
            var row = iterator.next();

            var idCell = row.getCell(0);
            if (StringUtils.isBlank(CellUtils.getCellStringValue(idCell))) {
                continue;
            }

            var columns = new ArrayList<String>();
            for (var header : excelHeaders) {
                var cell = row.getCell(header.getIndex());
                var content = CellUtils.getCellStringValue(cell);
                columns.add(content);
            }
            rows.add(columns);
        }

        // Handle column offset caused by empty cells in Excel
        var headers = new ArrayList<StorageHeader>();
        for (var i = 0; i < excelHeaders.size(); i++) {
            var excelHeader = excelHeaders.get(i);
            headers.add(StorageHeader.valueOf(excelHeader.getName(), excelHeader.getType(), i));
        }
        return StorageData.valueOf(resourceClassName, headers, rows);
    }

    private static List<StorageHeader> getHeaders(Iterator<Row> iterator, String resourceClassName) {
        // Get valid column names from the resource table; row 1 is the field names by default
        var fieldRow = iterator.next();
        if (fieldRow == null) {
            throw new RunException("Failed to get attribute control column from excel file of resource [class:{}]", resourceClassName);
        }
        // Row 2: field types (default)
        var typeRow = iterator.next();
        if (typeRow == null) {
            throw new RunException("Failed to get type control column from excel file of resource [class:{}]", resourceClassName);
        }
        // Row 3: descriptions (optional)
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
        } catch (Exception e) {
            throw new RunException("Static excel resource [{}] is abnormal, and the file cannot be read", fileName, e);
        }
    }
}
