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
@Protocol(id = 1120, enhance = false)
public class CM_Map {

    private Map<Integer, Integer> map;

    private Map<Integer, ObjectA> mapA;

    private Map<ObjectA, String> mapB;

    private Map<ObjectA, ObjectA> mapC;

    private Map<List<Integer>, Map<List<Integer>, ObjectA>> mapWithListAndMap;

    public Map<Integer, Integer> getMap() {
        return map;
    }

    public void setMap(Map<Integer, Integer> map) {
        this.map = map;
    }

    @Override
    public String toString() {
        return "CM_Map{" + "map=" + map + ", mapA=" + mapA + ", mapB=" + mapB + ", mapC=" + mapC + '}';
    }

    public Map<Integer, ObjectA> getMapA() {
        return mapA;
    }

    public void setMapA(Map<Integer, ObjectA> mapA) {
        this.mapA = mapA;
    }

    public Map<ObjectA, String> getMapB() {
        return mapB;
    }

    public void setMapB(Map<ObjectA, String> mapB) {
        this.mapB = mapB;
    }

    public Map<ObjectA, ObjectA> getMapC() {
        return mapC;
    }

    public void setMapC(Map<ObjectA, ObjectA> mapC) {
        this.mapC = mapC;
    }

    public Map<List<Integer>, Map<List<Integer>, ObjectA>> getMapWithListAndMap() {
        return mapWithListAndMap;
    }

    public void setMapWithListAndMap(Map<List<Integer>, Map<List<Integer>, ObjectA>> mapWithListAndMap) {
        this.mapWithListAndMap = mapWithListAndMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CM_Map cm_map = (CM_Map) o;
        return Objects.equals(map, cm_map.map) &&
                Objects.equals(mapA, cm_map.mapA) &&
                Objects.equals(mapB, cm_map.mapB) &&
                Objects.equals(mapC, cm_map.mapC) &&
                Objects.equals(mapWithListAndMap, cm_map.mapWithListAndMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(map, mapA, mapB, mapC, mapWithListAndMap);
    }
}
