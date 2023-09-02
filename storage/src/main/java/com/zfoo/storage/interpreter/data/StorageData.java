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

import java.util.ArrayList;
import java.util.List;

/**
 * 配置文件资源
 *
 * @author meiwei666
 */
public class StorageData {

    // 文件名
    private String name;
    // 配置表字段名
    private List<StorageHeader> headers = new ArrayList<>();
    // 配置表数据
    private List<List<String>> rows = new ArrayList<>();

    public static StorageData valueOf(String name, List<StorageHeader> headers, List<List<String>> rows) {
        var resourceData = new StorageData();
        resourceData.name = name;
        resourceData.headers = headers;
        resourceData.rows = rows;
        return resourceData;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<StorageHeader> getHeaders() {
        return headers;
    }

    public void setHeaders(List<StorageHeader> headers) {
        this.headers = headers;
    }

    public List<List<String>> getRows() {
        return rows;
    }

    public void setRows(List<List<String>> rows) {
        this.rows = rows;
    }

}
