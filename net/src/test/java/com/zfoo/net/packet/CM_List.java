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

package com.zfoo.net.packet;

import com.zfoo.protocol.anno.Protocol;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author godotg
 */
@Protocol(id = 1118, enhance = false)
public class CM_List {

    private List<Integer> list;

    private List<List<Integer>> listWitList;

    private List<ObjectA> objs;

    private List<List<List<ObjectA>>> listWithObject;

    private List<Map<Integer, String>> listWithMap;

    private List<List<Map<Integer, ObjectA>>> listListWithMap;


    public List<ObjectA> getObjs() {
        return objs;
    }

    public void setObjs(List<ObjectA> objs) {
        this.objs = objs;
    }

    public List<Integer> getList() {
        return list;
    }

    public void setList(List<Integer> list) {
        this.list = list;
    }

    public List<List<Integer>> getListWitList() {
        return listWitList;
    }

    public void setListWitList(List<List<Integer>> listWitList) {
        this.listWitList = listWitList;
    }

    public List<List<List<ObjectA>>> getListWithObject() {
        return listWithObject;
    }

    public void setListWithObject(List<List<List<ObjectA>>> listWithObject) {
        this.listWithObject = listWithObject;
    }

    public List<Map<Integer, String>> getListWithMap() {
        return listWithMap;
    }

    public void setListWithMap(List<Map<Integer, String>> listWithMap) {
        this.listWithMap = listWithMap;
    }

    public List<List<Map<Integer, ObjectA>>> getListListWithMap() {
        return listListWithMap;
    }

    public void setListListWithMap(List<List<Map<Integer, ObjectA>>> listListWithMap) {
        this.listListWithMap = listListWithMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CM_List cm_list = (CM_List) o;
        return Objects.equals(list, cm_list.list) &&
                Objects.equals(listWitList, cm_list.listWitList) &&
                Objects.equals(objs, cm_list.objs) &&
                Objects.equals(listWithObject, cm_list.listWithObject) &&
                Objects.equals(listWithMap, cm_list.listWithMap) &&
                Objects.equals(listListWithMap, cm_list.listListWithMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(list, listWitList, objs, listWithObject, listWithMap, listListWithMap);
    }
}
