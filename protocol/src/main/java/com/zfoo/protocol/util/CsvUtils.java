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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * @author jaysunxiao
 */
public class CsvUtils {

    /**
     * CSV 字符串转对象列表
     *
     * @param csv   CSV 格式字符串，第一行是表头
     * @param clazz 目标类
     * @param <T>   泛型类型
     * @return 对象列表
     */
    public static <T> List<T> parse(String csv, Class<T> clazz) {
        if (StringUtils.isEmpty(csv)) {
            return Collections.emptyList();
        }

        var lines = csv.split(FileUtils.LS_REGEX);
        if (lines.length < 2) {
            return Collections.emptyList();
        }

        var result = new ArrayList<T>();
        // 第一行作为表头
        var headers = lines[0].split(StringUtils.COMMA_REGEX);
        var headerIndex = new HashMap<String, Integer>();
        for (var i = 0; i < headers.length; i++) {
            headerIndex.put(headers[i].trim(), i);
        }

        var fields = ReflectionUtils.notStaticAndTransientFields(clazz);
        fields.forEach(it -> ReflectionUtils.makeAccessible(it));
        // 从第二行开始解析
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

}
