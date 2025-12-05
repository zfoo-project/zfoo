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
package com.zfoo.protocol.util;

import com.zfoo.protocol.collection.ArrayUtils;
import com.zfoo.protocol.collection.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author jaysunxiao
 */
public class CsvUtils {


    public static <T> List<T> toList(String csv, Class<T> clazz) {
        if (StringUtils.isEmpty(csv)) {
            return Collections.emptyList();
        }

        var lines = csv.split(FileUtils.LS_REGEX);
        if (lines.length < 2) {
            return Collections.emptyList();
        }

        var result = new ArrayList<T>();
        // first line as header
        var headers = lines[0].split(StringUtils.COMMA_REGEX);
        var headerIndex = new HashMap<String, Integer>();
        for (var i = 0; i < headers.length; i++) {
            headerIndex.put(headers[i].trim(), i);
        }

        var fields = ReflectionUtils.notStaticAndTransientFields(clazz);
        fields.forEach(it -> ReflectionUtils.makeAccessible(it));
        // next line from second line is data
        for (var i = 1; i < lines.length; i++) {
            var values = lines[i].split(StringUtils.COMMA_REGEX);

            T obj = ReflectionUtils.newInstance(clazz);
            for (var field : fields) {
                if (!headerIndex.containsKey(field.getName())) {
                    continue;
                }
                var idx = headerIndex.get(field.getName());
                if (idx < values.length) {
                    var raw = values[idx].trim();
                    Object converted = ReflectionUtils.convertSimple(raw, field.getType());
                    ReflectionUtils.setField(field, obj, converted);
                }
            }
            result.add(obj);
        }
        return result;
    }

    public static <T> T[] toArray(String csv, Class<T> clazz) {
        return ArrayUtils.listToArray(toList(csv, clazz), clazz);
    }


    public static <T> String toCsv(List<T> list) {
        if (CollectionUtils.isEmpty(list)) {
            return StringUtils.EMPTY;
        }

        var builder = new StringBuilder();

        // header
        var clazz = list.get(0).getClass();
        var fields = ReflectionUtils.notStaticAndTransientFields(clazz);
        fields.forEach(it -> ReflectionUtils.makeAccessible(it));

        var headers = fields.stream().map(it -> it.getName()).collect(Collectors.joining(StringUtils.COMMA));
        builder.append(headers).append(FileUtils.LS);

        // data row
        for (T obj : list) {
            var row = fields.stream()
                    .map(it -> ReflectionUtils.getField(it, obj))
                    .map(it -> it.toString())
                    .collect(Collectors.joining(StringUtils.COMMA));
            builder.append(row).append(FileUtils.LS);
        }

        return builder.toString();
    }

    public static <T> String toCsv(T[] array) {
        return toCsv(ArrayUtils.toList(array));
    }
}
