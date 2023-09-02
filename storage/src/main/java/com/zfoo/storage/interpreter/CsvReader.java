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
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author meiwei666
 * @version 3.0
 */
public abstract class CsvReader {

    public static StorageData readResourceDataFromCSV(InputStream input, String fileName) {
        var records = parseCsv(input, fileName);
        var iterator = records.iterator();
        var headers = getHeaders(iterator, fileName);
        var rows = new ArrayList<List<String>>();
        while (iterator.hasNext()) {
            var record = iterator.next();
            var data = new ArrayList<String>();
            for (var header : headers) {
                var value = record.get(header.getIndex());
                if (StringUtils.isBlank(value)) {
                    value = StringUtils.EMPTY;
                }
                data.add(value);
            }
            rows.add(data);
        }
        return StorageData.valueOf(fileName, headers, rows);
    }


    /**
     * 构建配置表消息头
     */
    private static List<StorageHeader> getHeaders(Iterator<CSVRecord> iterator, String fileName) {
        // 获取配置表的有效列名称，默认第一行就是字段名称
        var fieldRow = iterator.next();
        if (fieldRow == null) {
            throw new RunException("Failed to get attribute control column from csv file of resource [class:{}]", fileName);
        }
        //默认第二行字段类型
        var typeRow = iterator.next();
        if (typeRow == null) {
            throw new RunException("Failed to get type control column from csv file of resource [class:{}]", fileName);
        }
        // 默认第三行为描述，需要的时候再使用
        var descRow = iterator.next();

        var headers = new ArrayList<StorageHeader>();
        for (var i = 0; i < fieldRow.size(); i++) {
            var fieldName = fieldRow.get(i);
            if (fieldName == null) {
                throw new RunException("The column name of {} cannot be empty, and column {} has no configured name", fileName, i + 1);
            }
            var filedType = typeRow.get(i);
            if (filedType == null) {
                throw new RunException("The column type of {} cannot be empty, and column {} has no configured type", fileName, i + 1);
            }
            headers.add(StorageHeader.valueOf(fieldName, filedType, i));
        }
        return headers;
    }

    private static CSVParser parseCsv(InputStream input, String fileName) {
        try {
            return CSVFormat.EXCEL.parse(new InputStreamReader(input));
        } catch (IOException e) {
            throw new RunException("Static csv resource [{}] is abnormal, and the file cannot be read", fileName);
        }
    }
}
