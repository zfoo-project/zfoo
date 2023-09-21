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

import com.zfoo.protocol.collection.CollectionUtils;
import com.zfoo.protocol.util.AssertionUtils;
import com.zfoo.protocol.util.IOUtils;
import com.zfoo.protocol.util.ReflectionUtils;
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.storage.interpreter.ResourceInterpreter;
import com.zfoo.storage.model.IStorage;
import com.zfoo.storage.model.IdDef;
import com.zfoo.storage.model.IndexDef;
import com.zfoo.storage.util.LambdaUtils;
import com.zfoo.storage.util.function.Func1;
import org.springframework.lang.Nullable;

import java.io.InputStream;
import java.util.*;

/**
 * @author godotg
 */
public class StorageObject<K, V> implements IStorage<K, V> {
    private Map<K, V> dataMap = new HashMap<>(64);
    // 非唯一索引
    protected Map<String, Map<Object, List<V>>> indexMap = new HashMap<>(16);
    // 唯一索引
    protected Map<String, Map<Object, V>> uniqueIndexMap = new HashMap<>(16);

    protected Class<?> clazz;
    protected IdDef idDef;
    protected Map<String, IndexDef> indexDefMap;
    // 当前配置表是否在当前项目中使用，没有被使用的会清除data数据，以达到节省内存的目的
    protected boolean recycle = true;


    public static StorageObject<?, ?> parse(InputStream inputStream, Class<?> resourceClazz, String suffix) {
        var storage = new StorageObject<>();
        storage.clazz = resourceClazz;
        var idDef = IdDef.valueOf(resourceClazz);
        storage.idDef = idDef;
        storage.indexDefMap = Collections.unmodifiableMap(IndexDef.createResourceIndexes(resourceClazz));

        try {
            var list = ResourceInterpreter.read(inputStream, resourceClazz, suffix);
            storage.putAll(list);
        } catch (Throwable e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            IOUtils.closeIO(inputStream);
        }

        var idType = idDef.getField().getType();
        if (idType == int.class || idType == Integer.class) {
            return new StorageInt<>(storage);
        } else if (idType == long.class || idType == Long.class) {
            return new StorageLong<>(storage);
        } else {
            return storage;
        }
    }

    @Override
    public boolean contain(K id) {
        return dataMap.containsKey(id);
    }

    @Override
    public boolean contain(int id) {
        @SuppressWarnings("unchecked")
        var key = (K) Integer.valueOf(id);
        return contain(key);
    }

    @Override
    public boolean contain(long id) {
        @SuppressWarnings("unchecked")
        var key = (K) Long.valueOf(id);
        return contain(key);
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
        return all.stream().toList();
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
    public <INDEX> List<V> getIndexes(Func1<V, INDEX> func, INDEX index) {
        String indexName = LambdaUtils.getFieldName(func);
        var indexValues = indexMap.get(indexName);
        AssertionUtils.notNull(indexValues, "The index of [indexName:{}] does not exist in the static resource [resource:{}]", indexName, clazz.getSimpleName());
        var values = indexValues.get(index);
        if (CollectionUtils.isEmpty(values)) {
            return Collections.emptyList();
        }
        return values;
    }

    @Nullable
    @Override
    public <INDEX> V getUniqueIndex(Func1<V, INDEX> func, INDEX index) {
        String uniqueIndexName = LambdaUtils.getFieldName(func);
        var indexValueMap = uniqueIndexMap.get(uniqueIndexName);
        AssertionUtils.notNull(indexValueMap, "There is no a unique index for [uniqueIndexName:{}] in the static resource [resource:{}]", uniqueIndexName, clazz.getSimpleName());
        var value = indexValueMap.get(index);
        return value;
    }

    @Override
    public int size() {
        return dataMap.size();
    }

    public void putAll(List<?> values) {
        for (var value: values) {
            @SuppressWarnings("unchecked")
            var id = (K) ReflectionUtils.getField(idDef.getField(), value);

            if (id == null) {
                throw new RuntimeException("There is an item with an unconfigured id in the static resource");
            }
            if (dataMap.containsKey(id)) {
                throw new RuntimeException(StringUtils.format("Duplicate [id:{}] of static resource [resource:{}]", id, clazz.getSimpleName()));
            }
            // 添加资源
            @SuppressWarnings("unchecked")
            var v = (V) value;
            dataMap.put(id, v);
            // 添加索引
            for (var def : indexDefMap.values()) {
                // 使用field的名称作为索引的名称
                var indexKey = def.getField().getName();
                var indexValue = ReflectionUtils.getField(def.getField(), v);
                if (def.isUnique()) {
                    var uniqueIndex = uniqueIndexMap.computeIfAbsent(indexKey, it -> new HashMap<>(values.size()));
                    if (uniqueIndex.put(indexValue, v) != null) {
                        throw new RuntimeException(StringUtils.format("Duplicate unique index [index:{}][value:{}] of static resource [class:{}]", indexKey, indexValue, clazz.getName()));
                    }
                } else {
                    var index = indexMap.computeIfAbsent(indexKey, it -> new HashMap<>(values.size()));
                    var list = index.computeIfAbsent(indexValue, it -> new ArrayList<V>());
                    list.add(v);
                }
            }
        }
    }

}
