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

import com.zfoo.orm.anno.EntityCache;
import com.zfoo.orm.anno.Id;
import com.zfoo.orm.anno.Persister;
import com.zfoo.orm.model.IEntity;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@EntityCache(persister = @Persister("time30s"))
public class MapEntity implements IEntity<Long> {
    @Id
    private long id;

    private List<Integer> list = new ArrayList<>();

    private CopyOnWriteArrayList<Integer> copyOnWriteArrayList = new CopyOnWriteArrayList<>();

    private ConcurrentHashMap<Long, Integer> concurrentHashMap = new ConcurrentHashMap<>();

    private ConcurrentHashMap<Long, ConcurrentHashMap<Integer,Integer>> concurrentHashMapAndConcurrentHashMap = new ConcurrentHashMap<>();
    private Map<String, BagItem> bagMap = new HashMap<>();

    private Map<String, Map<String, String>> baseMap = new HashMap<>();

    private Map<Long, String> longStringMap = new HashMap<>();
    private Map<Integer, String> intStringMap = new HashMap<>();
    private Map<Integer, Map<Integer, String>> intBaseMap = new HashMap<>();
    private Map<Character, BagItem> charBagMap = new HashMap<>();
    private Map<Boolean, BagItem> boolBagMap = new HashMap<>();
    private Map<Byte, BagItem> byteBagMap = new HashMap<>();
    private Map<Short, BagItem> shortBagMap = new HashMap<>();
    private Map<Integer, BagItem> intBagMap = new HashMap<>();
    private Map<Long, BagItem> longBagMap = new HashMap<>();
    private Map<Float, BagItem> floatBagMap = new HashMap<>();
    private Map<Double, BagItem> doubleBagMap = new HashMap<>();

    @Override
    public Long id() {
        return id;
    }

    public List<Integer> getList() {
        return list;
    }

    public void setList(List<Integer> list) {
        this.list = list;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Map<String, BagItem> getBagMap() {
        return bagMap;
    }

    public void setBagMap(Map<String, BagItem> bagMap) {
        this.bagMap = bagMap;
    }

    public Map<String, Map<String, String>> getBaseMap() {
        return baseMap;
    }

    public void setBaseMap(Map<String, Map<String, String>> baseMap) {
        this.baseMap = baseMap;
    }

    public Map<Long, String> getLongStringMap() {
        return longStringMap;
    }

    public void setLongStringMap(Map<Long, String> longStringMap) {
        this.longStringMap = longStringMap;
    }

    public Map<Integer, String> getIntStringMap() {
        return intStringMap;
    }

    public void setIntStringMap(Map<Integer, String> intStringMap) {
        this.intStringMap = intStringMap;
    }

    public Map<Integer, Map<Integer, String>> getIntBaseMap() {
        return intBaseMap;
    }

    public void setIntBaseMap(Map<Integer, Map<Integer, String>> intBaseMap) {
        this.intBaseMap = intBaseMap;
    }

    public Map<Character, BagItem> getCharBagMap() {
        return charBagMap;
    }

    public void setCharBagMap(Map<Character, BagItem> charBagMap) {
        this.charBagMap = charBagMap;
    }

    public Map<Boolean, BagItem> getBoolBagMap() {
        return boolBagMap;
    }

    public void setBoolBagMap(Map<Boolean, BagItem> boolBagMap) {
        this.boolBagMap = boolBagMap;
    }

    public Map<Byte, BagItem> getByteBagMap() {
        return byteBagMap;
    }

    public void setByteBagMap(Map<Byte, BagItem> byteBagMap) {
        this.byteBagMap = byteBagMap;
    }

    public Map<Short, BagItem> getShortBagMap() {
        return shortBagMap;
    }

    public void setShortBagMap(Map<Short, BagItem> shortBagMap) {
        this.shortBagMap = shortBagMap;
    }

    public Map<Integer, BagItem> getIntBagMap() {
        return intBagMap;
    }

    public void setIntBagMap(Map<Integer, BagItem> intBagMap) {
        this.intBagMap = intBagMap;
    }

    public Map<Long, BagItem> getLongBagMap() {
        return longBagMap;
    }

    public void setLongBagMap(Map<Long, BagItem> longBagMap) {
        this.longBagMap = longBagMap;
    }

    public Map<Float, BagItem> getFloatBagMap() {
        return floatBagMap;
    }

    public void setFloatBagMap(Map<Float, BagItem> floatBagMap) {
        this.floatBagMap = floatBagMap;
    }

    public Map<Double, BagItem> getDoubleBagMap() {
        return doubleBagMap;
    }

    public void setDoubleBagMap(Map<Double, BagItem> doubleBagMap) {
        this.doubleBagMap = doubleBagMap;
    }

    public CopyOnWriteArrayList<Integer> getCopyOnWriteArrayList() {
        return copyOnWriteArrayList;
    }

    public void setCopyOnWriteArrayList(CopyOnWriteArrayList<Integer> copyOnWriteArrayList) {
        this.copyOnWriteArrayList = copyOnWriteArrayList;
    }

    public ConcurrentHashMap<Long, Integer> getConcurrentHashMap() {
        return concurrentHashMap;
    }

    public void setConcurrentHashMap(ConcurrentHashMap<Long, Integer> concurrentHashMap) {
        this.concurrentHashMap = concurrentHashMap;
    }


    public ConcurrentHashMap<Long, ConcurrentHashMap<Integer, Integer>> getConcurrentHashMapAndConcurrentHashMap() {
        return concurrentHashMapAndConcurrentHashMap;
    }

    public void setConcurrentHashMapAndConcurrentHashMap(ConcurrentHashMap<Long, ConcurrentHashMap<Integer, Integer>> concurrentHashMapAndConcurrentHashMap) {
        this.concurrentHashMapAndConcurrentHashMap = concurrentHashMapAndConcurrentHashMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MapEntity mapEntity = (MapEntity) o;
        return id == mapEntity.id && Objects.equals(list, mapEntity.list) && Objects.equals(bagMap, mapEntity.bagMap) && Objects.equals(baseMap, mapEntity.baseMap) && Objects.equals(longStringMap, mapEntity.longStringMap) && Objects.equals(intStringMap, mapEntity.intStringMap) && Objects.equals(intBaseMap, mapEntity.intBaseMap) && Objects.equals(charBagMap, mapEntity.charBagMap) && Objects.equals(boolBagMap, mapEntity.boolBagMap) && Objects.equals(byteBagMap, mapEntity.byteBagMap) && Objects.equals(shortBagMap, mapEntity.shortBagMap) && Objects.equals(intBagMap, mapEntity.intBagMap) && Objects.equals(longBagMap, mapEntity.longBagMap) && Objects.equals(floatBagMap, mapEntity.floatBagMap) && Objects.equals(doubleBagMap, mapEntity.doubleBagMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, list, bagMap, baseMap, longStringMap, intStringMap, intBaseMap, charBagMap, boolBagMap, byteBagMap, shortBagMap, intBagMap, longBagMap, floatBagMap, doubleBagMap);
    }
}
