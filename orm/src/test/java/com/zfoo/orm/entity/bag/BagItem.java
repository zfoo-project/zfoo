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

package com.zfoo.orm.entity.bag;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class BagItem {

    private int id;
    private String desc;

    private Map<String, Item> itemMap = new HashMap<>();

    public BagItem() {
    }

    public BagItem(int id, String desc, Map<String, Item> itemMap) {
        this.id = id;
        this.desc = desc;
        this.itemMap = itemMap;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Map<String, Item> getItemMap() {
        return itemMap;
    }

    public void setItemMap(Map<String, Item> itemMap) {
        this.itemMap = itemMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BagItem bagItem = (BagItem) o;
        return id == bagItem.id && Objects.equals(desc, bagItem.desc) && Objects.equals(itemMap, bagItem.itemMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, desc, itemMap);
    }
}
