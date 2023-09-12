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
package com.zfoo.storage.interpreter.data;

import com.zfoo.protocol.util.AssertionUtils;
import com.zfoo.protocol.util.StringUtils;
import org.springframework.lang.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * 支持读取配置文件的类型后缀
 *
 * @author godotg
 */
public enum StorageEnum {

    EXCEL_XLS("xls"),

    EXCEL_XLSX("xlsx"),

    JSON("json"),

    CSV("csv"),

    ;

    private static Map<String, StorageEnum> typeMap = new HashMap<>();

    static {
        for (var resourceEnum : StorageEnum.values()) {
            var previousValue = typeMap.putIfAbsent(resourceEnum.type, resourceEnum);
            AssertionUtils.isNull(previousValue, "ResourceEnum should not contain enumeration classes [{}] and [{}] of repeated type", resourceEnum, previousValue);
        }
    }

    private String type;

    StorageEnum(String type) {
        this.type = type;
    }

    @Nullable
    public static StorageEnum getResourceEnumByType(String type) {
        return typeMap.get(type);
    }

    public static boolean containsResourceEnum(String type) {
        return typeMap.containsKey(type);
    }

    public static boolean isExcel(String type) {
        var resourceEnum = getResourceEnumByType(type);
        return resourceEnum == StorageEnum.EXCEL_XLS || resourceEnum == StorageEnum.EXCEL_XLSX;
    }

    public static String typesToString() {
        return StringUtils.joinWith(StringUtils.SPACE, typeMap.values().toArray());
    }

    public String getType() {
        return type;
    }

}
