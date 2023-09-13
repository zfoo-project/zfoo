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

package com.zfoo.storage.manager;

import com.google.common.collect.ImmutableMap;
import com.zfoo.protocol.collection.CollectionUtils;
import com.zfoo.protocol.util.AssertionUtils;
import com.zfoo.protocol.util.ClassUtils;
import com.zfoo.protocol.util.IOUtils;
import com.zfoo.protocol.util.ReflectionUtils;
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.storage.interpreter.ResourceInterpreter;
import com.zfoo.storage.model.IStorage;
import com.zfoo.storage.model.IdDef;
import com.zfoo.storage.model.IndexDef;
import com.zfoo.storage.util.LambdaUtils;
import com.zfoo.storage.util.support.SerializableFunction;
import org.springframework.lang.Nullable;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author godotg
 */
public class StorageObject<K, V> implements IStorage<K, V> {
    private ImmutableMap<K, V> dataMap;
    // 非唯一索引
    protected ImmutableMap<String, Map<Object, List<V>>> indexMap;
    // 唯一索引
    protected ImmutableMap<String, Map<Object, V>> uniqueIndexMap;

    protected Class<?> clazz;
    protected IdDef idDef;
    protected ImmutableMap<String, IndexDef> indexDefMap;
    // 当前配置表是否在当前项目中使用，没有被使用的会清除data数据，以达到节省内存的目的
    protected boolean recycle = true;


    public static StorageObject<?, ?> parse(InputStream inputStream, Class<?> resourceClazz, String suffix) {
        try {
            var storage = new StorageObject<>();
            storage.clazz = resourceClazz;
            var idDef = IdDef.valueOf(resourceClazz);
            storage.idDef = idDef;
            storage.indexDefMap = ImmutableMap.copyOf(IndexDef.createResourceIndexes(resourceClazz));
            var list = ResourceInterpreter.read(inputStream, resourceClazz, suffix);
            storage.append(list);
            var idType = idDef.getField().getType();
            if (idType == int.class || idType == Integer.class) {
                return new StorageInt<>(storage);
            } else if (idType == long.class || idType == Long.class) {
                return new StorageLong<>(storage);
            } else {
                return storage;
            }
        } catch (Throwable e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            IOUtils.closeIO(inputStream);
        }
    }

    @Override
    public boolean contain(K key) {
        return dataMap.containsKey(key);
    }

    @Override
    public boolean contain(int key) {
        @SuppressWarnings("unchecked")
        var k = (K) Integer.valueOf(key);
        return contain(k);
    }

    @Override
    public boolean contain(long key) {
        @SuppressWarnings("unchecked")
        var k = (K) Long.valueOf(key);
        return contain(k);
    }

    @Override
    public V get(K id) {
        V result = dataMap.get(id);
        AssertionUtils.notNull(result, "The static resource represented as [id:{}] in the static resource [resource:{}] does not exist", id, clazz.getSimpleName());
        return result;
    }

    @Override
    public V get(int id) {
        @SuppressWarnings("unchecked")
        var key = (K) Integer.valueOf(id);
        return get(key);
    }

    @Override
    public V get(long id) {
        @SuppressWarnings("unchecked")
        var key = (K) Long.valueOf(id);
        return get(key);
    }

    @Override
    public void recycleStorage() {
        recycle = true;
        dataMap = null;
        indexMap = null;
        uniqueIndexMap = null;
        idDef = null;
        indexDefMap = null;
    }

    @Override
    public boolean isRecycle() {
        return recycle;
    }

    @Override
    public void setRecycle(boolean recycle) {
        this.recycle = recycle;
    }

    @Override
    public Collection<V> getAll() {
        return dataMap.values();
    }

    @Override
    public List<V> getList() {
        Collection<V> all = getAll();
        return all.stream().collect(Collectors.toUnmodifiableList());
    }

    @Override
    public Map<K, V> getData() {
        return Collections.unmodifiableMap(dataMap);
    }

    @Override
    public IdDef getIdDef() {
        return idDef;
    }

    @Override
    public <K> List<V> getIndexes(SerializableFunction<V, ?> indexFunction, K key) {
        String indexName = ClassUtils.getFieldName(LambdaUtils.extract(indexFunction).getImplMethodName());
        var indexValues = indexMap.get(indexName);
        AssertionUtils.notNull(indexValues, "The index of [indexName:{}] does not exist in the static resource [resource:{}]", indexName, clazz.getSimpleName());
        var values = indexValues.get(key);
        if (CollectionUtils.isEmpty(values)) {
            return Collections.emptyList();
        }
        return values;
    }

    @Nullable
    @Override
    public <K, V> V getUniqueIndex(SerializableFunction<V, ?> uniqueIndexFunction, K key) {
        String uniqueIndexName = ClassUtils.getFieldName(LambdaUtils.extract(uniqueIndexFunction).getImplMethodName());
        var indexValueMap = uniqueIndexMap.get(uniqueIndexName);
        AssertionUtils.notNull(indexValueMap, "There is no a unique index for [uniqueIndexName:{}] in the static resource [resource:{}]", uniqueIndexName, clazz.getSimpleName());
        var value = indexValueMap.get(key);
        return (V) value;
    }

    @Override
    public int size() {
        return dataMap.size();
    }

    private void append(List<?> values) {
        ImmutableMap.Builder<K, V> dataMapBuilder = ImmutableMap.builder();
        Map<String, Map<Object, V>> uniqueIndexMap = new HashMap<>(32);
        Map<String, Map<Object, List<V>>> indexMap = new HashMap<>(32);
        for (var value : values) {
            var key = (K) ReflectionUtils.getField(idDef.getField(), value);

            if (key == null) {
                throw new RuntimeException("There is an item with an unconfigured id in the static resource");
            }

            // 添加资源
            var v = (V) value;
            dataMapBuilder.put(key, v);

            // 添加索引
            for (var def : indexDefMap.values()) {
                // 使用field的名称作为索引的名称
                var indexKey = def.getField().getName();
                var indexValue = ReflectionUtils.getField(def.getField(), v);
                if (def.isUnique()) {// 唯一索引
                    var index = uniqueIndexMap.computeIfAbsent(indexKey, k -> new HashMap<>(8));
                    if (index.put(indexValue, v) != null) {
                        throw new RuntimeException(StringUtils.format("Duplicate unique index [index:{}][value:{}] of static resource [class:{}]", indexKey, indexValue, clazz.getName()));
                    }
                } else {// 不是唯一索引
                    var index = indexMap.computeIfAbsent(indexKey, k -> new HashMap<>(12));
                    var list = index.computeIfAbsent(indexValue, k -> new ArrayList<>());
                    list.add(v);
                }
            }
        }
        this.uniqueIndexMap = ImmutableMap.copyOf(uniqueIndexMap);
        this.indexMap = ImmutableMap.copyOf(indexMap);
        this.dataMap = dataMapBuilder.build();
    }

    @Deprecated
    protected V put(Object value) {
        @SuppressWarnings("unchecked")
        var key = (K) ReflectionUtils.getField(idDef.getField(), value);

        if (key == null) {
            throw new RuntimeException("There is an item with an unconfigured id in the static resource");
        }

        if (dataMap.containsKey(key)) {
            throw new RuntimeException(StringUtils.format("Duplicate [id:{}] of static resource [resource:{}]", key, clazz.getSimpleName()));
        }

        // 添加资源
        @SuppressWarnings("unchecked")
        var v = (V) value;
        var result = dataMap.put(key, v);

        // 添加索引
        for (var def : indexDefMap.values()) {
            // 使用field的名称作为索引的名称
            var indexKey = def.getField().getName();
            var indexValue = ReflectionUtils.getField(def.getField(), v);
            if (def.isUnique()) {// 唯一索引
                var index = uniqueIndexMap.computeIfAbsent(indexKey, k -> new HashMap<>());
                if (index.put(indexValue, v) != null) {
                    throw new RuntimeException(StringUtils.format("Duplicate unique index [index:{}][value:{}] of static resource [class:{}]", indexKey, indexValue, clazz.getName()));
                }
            } else {// 不是唯一索引
                var index = indexMap.computeIfAbsent(indexKey, k -> new HashMap<>());
                var list = index.computeIfAbsent(indexValue, k -> new ArrayList<V>());
                list.add(v);
            }
        }
        return result;
    }

}
