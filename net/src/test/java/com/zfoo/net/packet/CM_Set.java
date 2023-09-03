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
import java.util.Set;

/**
 * @author godotg
 * @version 3.0
 */
@Protocol(id = 1121, enhance = false)
public class CM_Set implements IPacket {

    private Set<Integer> a;
    private Set<ObjectA> b;
    private Set<List<Integer>> c;

    private Set<Set<Set<ObjectA>>> setSetSetWithObject;
    private Set<Map<Integer, String>> setWithMap;
    private Set<List<Map<Integer, ObjectA>>> setListWithMap;

    public Set<Integer> getA() {
        return a;
    }

    public void setA(Set<Integer> a) {
        this.a = a;
    }

    public Set<ObjectA> getB() {
        return b;
    }

    public void setB(Set<ObjectA> b) {
        this.b = b;
    }

    public Set<List<Integer>> getC() {
        return c;
    }

    public void setC(Set<List<Integer>> c) {
        this.c = c;
    }

    public Set<Set<Set<ObjectA>>> getSetSetSetWithObject() {
        return setSetSetWithObject;
    }

    public void setSetSetSetWithObject(Set<Set<Set<ObjectA>>> setSetSetWithObject) {
        this.setSetSetWithObject = setSetSetWithObject;
    }

    public Set<Map<Integer, String>> getSetWithMap() {
        return setWithMap;
    }

    public void setSetWithMap(Set<Map<Integer, String>> setWithMap) {
        this.setWithMap = setWithMap;
    }

    public Set<List<Map<Integer, ObjectA>>> getSetListWithMap() {
        return setListWithMap;
    }

    public void setSetListWithMap(Set<List<Map<Integer, ObjectA>>> setListWithMap) {
        this.setListWithMap = setListWithMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CM_Set cm_set = (CM_Set) o;
        return Objects.equals(a, cm_set.a) &&
                Objects.equals(b, cm_set.b) &&
                Objects.equals(c, cm_set.c) &&
                Objects.equals(setSetSetWithObject, cm_set.setSetSetWithObject) &&
                Objects.equals(setWithMap, cm_set.setWithMap) &&
                Objects.equals(setListWithMap, cm_set.setListWithMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b, c, setSetSetWithObject, setWithMap, setListWithMap);
    }
}
