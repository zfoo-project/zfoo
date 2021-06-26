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

package com.zfoo.orm.model.vo;

import com.zfoo.orm.model.anno.EntityCache;
import com.zfoo.orm.model.anno.Id;
import com.zfoo.orm.model.anno.Index;
import com.zfoo.orm.model.anno.IndexText;
import com.zfoo.orm.model.config.CacheStrategy;
import com.zfoo.orm.model.config.PersisterStrategy;
import com.zfoo.orm.model.entity.IEntity;
import com.zfoo.protocol.collection.ArrayUtils;
import com.zfoo.protocol.util.AssertionUtils;
import com.zfoo.protocol.util.ReflectionUtils;
import com.zfoo.protocol.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public class EntityDef {

    private Field idField;

    private Class<? extends IEntity<?>> clazz;

    private int cacheSize;

    private long expireMillisecond;

    private PersisterStrategy persisterStrategy;

    private Map<String, IndexDef> indexDefMap;

    private Map<String, IndexTextDef> indexTextDefMap;

    private EntityDef() {
    }

    public static EntityDef valueOf(Class<?> clazz, List<CacheStrategy> cacheStrategies, List<PersisterStrategy> persisterStrategies) {
        if (!IEntity.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException(StringUtils.format("被[{}]注解标注的实体类[{}]必须继承[{}]", EntityCache.class.getName(), clazz.getName(), IEntity.class.getName()));
        }

        var entityDef = new EntityDef();
        entityDef.clazz = (Class<? extends IEntity<?>>) clazz;
        try {
            ReflectionUtils.makeAccessible(clazz.getDeclaredConstructor());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(StringUtils.format("实体类Entity[{}]必须包含一个默认的构造器", clazz.getSimpleName()));
        }

        var cache = clazz.getAnnotation(EntityCache.class);
        AssertionUtils.notNull(cache);
        var cacheStrategyOptional = cacheStrategies.stream().filter(it -> it.getStrategy().equals(cache.cacheStrategy())).findFirst();
        if (cacheStrategyOptional.isEmpty()) {
            throw new RuntimeException(StringUtils.format("实体类Entity[{}]没有找到缓存策略[{}]", clazz.getSimpleName(), cache.cacheStrategy()));
        }
        var cacheStrategy = cacheStrategyOptional.get();
        entityDef.cacheSize = cacheStrategy.getSize();
        entityDef.expireMillisecond = cacheStrategy.getExpireMillisecond();

        var idFields = ReflectionUtils.getFieldsByAnnoInPOJOClass(clazz, Id.class);
        AssertionUtils.isTrue(ArrayUtils.isNotEmpty(idFields) && idFields.length == 1, "实体类Entity[{}]必须只有且仅有一个Id注解", clazz.getSimpleName());
        entityDef.idField = ReflectionUtils.getFieldsByAnnoInPOJOClass(clazz, Id.class)[0];
        ReflectionUtils.makeAccessible(entityDef.idField);
        // idField必须用private修饰
        if (!Modifier.isPrivate(entityDef.idField.getModifiers())) {
            throw new RuntimeException(StringUtils.format("实体类Entity[{}]的id必须是private私有的", clazz.getSimpleName()));
        }

        // entity必须包含属性的get和set方法
        Arrays.stream(clazz.getDeclaredFields())
                .filter(it -> !Modifier.isTransient(it.getModifiers()))
                .forEach(it -> {
                    ReflectionUtils.fieldToGetMethod(clazz, it);
                    ReflectionUtils.fieldToSetMethod(clazz, it);
                });
        var persister = cache.persister();
        AssertionUtils.notNull(persister);
        var persisterStrategyOptional = persisterStrategies.stream().filter(it -> it.getStrategy().equals(persister.value())).findFirst();
        if (persisterStrategyOptional.isEmpty()) {
            throw new RuntimeException(StringUtils.format("实体类Entity[{}]没有找到持久化策略[{}]", clazz.getSimpleName(), persister));
        }
        entityDef.persisterStrategy = persisterStrategyOptional.get();

        var indexDefMap = new HashMap<String, IndexDef>();
        var fields = ReflectionUtils.getFieldsByAnnoInPOJOClass(clazz, Index.class);
        for (Field field : fields) {
            var indexAnnotation = field.getAnnotation(Index.class);
            IndexDef indexDef = new IndexDef(field, indexAnnotation.ascending(), indexAnnotation.unique());
            indexDefMap.put(field.getName(), indexDef);
        }

        var indexTextDefMap = new HashMap<String, IndexTextDef>();
        fields = ReflectionUtils.getFieldsByAnnoInPOJOClass(clazz, IndexText.class);
        for (Field field : fields) {
            IndexTextDef indexTextDef = new IndexTextDef(field, field.getAnnotation(IndexText.class));
            indexTextDefMap.put(field.getName(), indexTextDef);
        }

        entityDef.indexDefMap = indexDefMap;
        entityDef.indexTextDefMap = indexTextDefMap;

        return entityDef;
    }


    public Class<? extends IEntity<?>> getClazz() {
        return clazz;
    }

    public IEntity<?> newEntity(Object id) {
        var entity = ReflectionUtils.newInstance(clazz);
        return entity;
    }

    public int getCacheSize() {
        return cacheSize;
    }

    public long getExpireMillisecond() {
        return expireMillisecond;
    }

    public PersisterStrategy getPersisterStrategy() {
        return persisterStrategy;
    }

    public Map<String, IndexDef> getIndexDefMap() {
        return indexDefMap;
    }

    public Map<String, IndexTextDef> getIndexTextDefMap() {
        return indexTextDefMap;
    }
}
