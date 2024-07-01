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

package com.zfoo.orm.model;

import com.zfoo.protocol.util.StringUtils;

/**
 * @author godotg
 */
public interface IEntity<PK extends Comparable<PK>> {

    /**
     * 一个文档的主键 primary key
     */
    PK id();

    /**
     * 判空：由于查询不存在时缓存中也会有一份，因此判断为空需要根据实际类型才能决定
     *
     * @return EntityCaches中取出的值在数据库中是否存在
     */
    default boolean empty() {
        PK idValue = id();
        if (idValue == null) {
            return true;
        }
        // id在启动的时候做过校验，只能是int long float double String ObjectId几种类型
        if (idValue instanceof Number) {
            return ((Number) idValue).doubleValue() == 0D;
        } else {
            return StringUtils.isEmpty(idValue.toString());
        }
    }

}
