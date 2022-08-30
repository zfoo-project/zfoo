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
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.storage.model.resource.ResourceData;
import com.zfoo.storage.model.resource.ResourceHeader;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * @author meiwei666
 * @version 4.0
 */
public class CsvReaderUtils {

    /**
     * 构建配置表消息头
     * @param iterator
     * @param fileName
     * @return
     */
    private static List<ResourceHeader> getHeaders(Iterator<CSVRecord> iterator, String fileName) {
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
        var descRow = iterator.next();

        List<ResourceHeader> headers = new ArrayList<>();
        for (int i = 0; i < fieldRow.size(); i++) {
            String fieldName = fieldRow.get(i);
            if (fieldName == null) {
                throw new RunException("{}列名不能为空，第{}列没有配置名字", fileName ,i + 1);
            }
            String filedType = typeRow.get(i);
            if (filedType == null) {
                throw new RunException("{}列类型不能为空，第{}列没有配置类型", fileName ,i + 1);
            }
//            String desc = descRecord.get(i);
            headers.add(ResourceHeader.valueOf(fieldName, filedType, i ));
        }
        return headers;
    }

    public static ResourceData readResourceDataFromCSV(InputStream input, String fileName){
        CSVParser records = parseCsv(input, fileName);
        Iterator<CSVRecord> iter = records.iterator();
        List<ResourceHeader> headers = getHeaders(iter, fileName);
        List<List<String>> rows = new ArrayList<>();
        while (iter.hasNext()) {
            CSVRecord record = iter.next();
            List<String> data = new ArrayList<>();
            for (ResourceHeader header : headers) {
                String value = record.get(header.getIndex());
                if (StringUtils.isBlank(value)) {
                    value = StringUtils.EMPTY;
                }
                data.add(value);
            }
            rows.add(data);
        }
        return ResourceData.valueOf(fileName, headers, rows);
    }

    private static CSVParser parseCsv(InputStream input, String fileName) {
        try {
            return CSVFormat.EXCEL.parse(new InputStreamReader(input));
        } catch (IOException e) {
            throw new RunException("静态资源[{}]异常，无法读取文件", fileName);
        }
    }
}
