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

package com.zfoo.storage.model;

import com.zfoo.protocol.collection.ArrayUtils;
import com.zfoo.protocol.exception.RunException;
import com.zfoo.protocol.util.ReflectionUtils;
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.storage.anno.Index;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 简化索引的名称，使用字段的名称作为索引的名称
 *
 * @author godotg
 */
public class IndexDef {

    private boolean unique;
    private Field field;

    public IndexDef(Field field) {
        ReflectionUtils.makeAccessible(field);
        this.field = field;
        var index = field.getAnnotation(Index.class);
        this.unique = index.unique();
    }

    public static Map<String, IndexDef> createResourceIndexes(Class<?> clazz) {
        var fields = ReflectionUtils.getFieldsByAnnoInPOJOClass(clazz, Index.class);
        var indexes = new ArrayList<IndexDef>(fields.length);

        var ormIndexes = ReflectionUtils.getFieldsByAnnoNameInPOJOClass(clazz, "com.zfoo.orm.model.anno.Index");
        if (ArrayUtils.isNotEmpty(ormIndexes)) {
            throw new RunException("Only the Index annotation of Storage can be used, and the Index annotation of Orm cannot be used in Storage. In order to avoid unnecessary misunderstanding and enhance the robustness of the project, such use is prohibited");
        }

        for (var field : fields) {
            IndexDef indexDef = new IndexDef(field);
            indexes.add(indexDef);
        }

        var result = new HashMap<String, IndexDef>();
        for (var index : indexes) {
            var indexName = index.field.getName();
            if (result.put(indexName, index) != null) {
                throw new RuntimeException(StringUtils.format("The  index name[{}] of resource class [{}] is duplicated.", indexName, clazz.getName()));
            }
        }

        return result;
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }
}
