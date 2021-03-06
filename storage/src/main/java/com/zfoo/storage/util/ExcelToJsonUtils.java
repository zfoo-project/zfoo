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
import com.zfoo.protocol.util.FileUtils;
import com.zfoo.protocol.util.JsonUtils;
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.storage.model.resource.ResourceData;
import com.zfoo.storage.model.resource.ResourceEnum;
import com.zfoo.storage.model.resource.ResourceHeader;
import com.zfoo.storage.model.resource.ResourceRow;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author meiwei666
 * @version 4.0
 */
public class ExcelToJsonUtils {

    public static void excelConvertJson(String inputDir, String outputDir) throws IOException {
        var listFiles = FileUtils.getAllReadableFiles(new File(inputDir))
                .stream()
                .filter(it -> ResourceEnum.isExcel(FileUtils.fileExtName(it.getName())))
                .collect(Collectors.toList());

        for (var file : listFiles) {
            var fileSimpleName = FileUtils.fileSimpleName(file.getName());
            var jsonFileName = StringUtils.format("{}.json", fileSimpleName);
            var inputStream = FileUtils.openInputStream(file);
            var resourceData = readResourceDataFromExcel(inputStream, file.getName());

            var outputFilePath = FileUtils.joinPath(outputDir, jsonFileName);
            FileUtils.deleteFile(new File(outputFilePath));
            FileUtils.writeStringToFile(new File(outputFilePath), JsonUtils.object2StringPrettyPrinter(resourceData), true);
        }
    }

    public static ResourceData readResourceDataFromExcel(InputStream inputStream, String fileName) {
        // ??????????????????????????????
        var wb = createWorkbook(inputStream, fileName);
        // ?????????????????????sheet???
        var sheet = wb.getSheetAt(0);
        //???????????????
        var headers = getHeaders(sheet, fileName);

        // ?????????????????????????????????????????????????????????????????????
        var iterator = sheet.iterator();
        iterator.next();
        iterator.next();
        iterator.next();
        // ???ROW_SERVER????????????????????????
        List<ResourceRow> rows = new ArrayList<>();
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
            rows.add(ResourceRow.valueOf(row.getRowNum() + 1, columns));
        }
        return ResourceData.valueOf(fileName, headers, rows);
    }

    // ??????????????????????????????
    private static List<ResourceHeader> getHeaders(Sheet sheet, String fileName) {
        var iterator = sheet.iterator();
        // ?????????????????????????????????????????????????????????????????????
        var fieldRow = iterator.next();
        if (fieldRow == null) {
            throw new RunException("??????????????????[class:{}]???Excel????????????????????????", fileName);
        }
        //???????????????????????????
        var typeRow = iterator.next();
        if (typeRow == null) {
            throw new RunException("??????????????????[class:{}]???Excel????????????????????????", fileName);
        }

        var headerList = new ArrayList<ResourceHeader>();
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
                throw new RunException("??????[class:{}]???Excel????????????????????????????????????[field:{}]", fileName, fieldName);
            }
            headerList.add(ResourceHeader.valueOf(fieldName, typeName, i));
        }
        return headerList;
    }

    private static Workbook createWorkbook(InputStream input, String fileName) {
        try {
            return WorkbookFactory.create(input);
        } catch (IOException e) {
            throw new RunException("????????????[{}]???????????????????????????", fileName);
        }
    }

}
