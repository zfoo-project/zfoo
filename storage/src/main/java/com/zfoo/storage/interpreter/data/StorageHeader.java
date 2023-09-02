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

/**
 * @author godotg
 * @version 3.0
 */
public class StorageHeader {

    //字段名
    private String name;
    //类型
    private String type;
    //列
    private int index;

    public static StorageHeader valueOf(String name, String type, int index) {
        var resourceHeader = new StorageHeader();
        resourceHeader.name = name;
        resourceHeader.type = type;
        resourceHeader.index = index;
        return resourceHeader;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

}
