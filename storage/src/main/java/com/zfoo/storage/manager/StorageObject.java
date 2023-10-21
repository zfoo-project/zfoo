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
import com.zfoo.protocol.util.*;
import com.zfoo.storage.interpreter.ResourceInterpreter;
import com.zfoo.storage.model.IStorage;
import com.zfoo.storage.model.IdDef;
import com.zfoo.storage.model.IndexDef;
import com.zfoo.storage.util.function.Func1;
import com.zfoo.storage.util.lambda.*;
import org.springframework.lang.Nullable;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

/**
 * @author godotg
 */
public class StorageObject<K, V> implements IStorage<K, V> {
    // all storage data
    private Map<K, V> dataMap;
    // 非唯一索引
    protected Map<String, Map<Object, List<V>>> indexMap = new HashMap<>();
    // 唯一索引
    protected Map<String, Map<Object, V>> uniqueIndexMap = new HashMap<>();

    protected Class<?> clazz;
    protected IdDef idDef;
    protected Map<String, IndexDef> indexDefMap;
    // EN: unused configuration tables will clear data to save memory.
    // CN: 没有被使用的配置表会清除data数据，以达到节省内存的目的
    protected boolean recycle = true;

    private ConcurrentReferenceHashMap<Func1<V, ?>, String> funcCaches = new ConcurrentReferenceHashMap<>();

    public static StorageObject<?, ?> parse(InputStream inputStream, Class<?> resourceClazz, String suffix) {
        var idDef = IdDef.valueOf(resourceClazz);
        var indexDefMap = Collections.unmodifiableMap(IndexDef.createResourceIndexes(resourceClazz));

        try {
            var list = ResourceInterpreter.read(inputStream, resourceClazz, suffix);
            var storage = new StorageObject<>(resourceClazz, idDef, indexDefMap, list);

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

    protected StorageObject() {
    }

    public StorageObject(Class<?> clazz, IdDef idDef, Map<String, IndexDef> indexDefMap, List<?> values) {
        this.dataMap = new HashMap<>(CollectionUtils.capacity(values.size()));
        this.clazz = clazz;
        this.idDef = idDef;
        this.indexDefMap = indexDefMap;
        for (var value : values) {
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
    public Collection<V> getAll() {
        return dataMap.values();
    }

    @Override
    public Map<K, V> getData() {
        return Collections.unmodifiableMap(dataMap);
    }

    @Override
    public <INDEX> List<V> getIndexes(Func1<V, INDEX> func, INDEX index) {
        String indexName = getMethodToField(func);
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
        String uniqueIndexName = getMethodToField(func);
        var indexValueMap = uniqueIndexMap.get(uniqueIndexName);
        AssertionUtils.notNull(indexValueMap, "There is no a unique index for [uniqueIndexName:{}] in the static resource [resource:{}]", uniqueIndexName, clazz.getSimpleName());
        var value = indexValueMap.get(index);
        return value;
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
    public IdDef getIdDef() {
        return idDef;
    }

    @Override
    public int size() {
        return dataMap.size();
    }

    private <INDEX> String getMethodToField(Func1<V, INDEX> func) {
        var indexName = funcCaches.get(func);
        if (indexName != null) {
            return indexName;
        }

        // 1. IDEA 调试模式下 lambda 表达式是一个代理
        if (func instanceof Proxy) {
            try {
                var lambda = new IdeaProxyLambdaMeta((Proxy) func);
                indexName = FieldUtils.getMethodToField(clazz, lambda.getImplMethodName());
            } catch (Exception e) {
            }
        }

        // 2. 反射读取
        if (indexName == null) {
            try {
                var method = func.getClass().getDeclaredMethod("writeReplace");
                ReflectionUtils.makeAccessible(method);
                var lambda = new ReflectLambdaMeta((java.lang.invoke.SerializedLambda) method.invoke(func));
                indexName = FieldUtils.getMethodToField(clazz, lambda.getImplMethodName());
            } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            }
        }

        // 3. 反射失败使用序列化的方式读取
        if (indexName == null) {
            try {
                var lambda = new ShadowLambdaMeta(SerializedLambda.extract(func));
                indexName = FieldUtils.getMethodToField(clazz, lambda.getImplMethodName());
            } catch (Exception e) {
            }
        }

        // 4. 通过将func带入到dataMap中求解，适合GraalVM环境中
        if (indexName == null) {
            try {
                var fields = clazz.getDeclaredFields();
                Arrays.stream(fields).forEach(ReflectionUtils::makeAccessible);
                for (var value : dataMap.values()) {
                    var r = func.call(value);
                    var valueFields = Arrays.stream(fields)
                            .map(it -> ReflectionUtils.getField(it, value))
                            .filter(it -> it.equals(r) && it.getClass() == r.getClass())
                            .toList();
                    // 如果只有一个能匹配到func的返回值则就是这个方法
                    if (valueFields.size() == 1) {
                        for (var field : fields) {
                            if (!ReflectionUtils.getField(field, value).equals(r)) {
                                continue;
                            }
                            indexName = field.getName();
                            break;
                        }
                        break;
                    }
                }
            } catch (Exception e) {
            }
        }

        funcCaches.put(func, indexName);
        return indexName;
    }
}
