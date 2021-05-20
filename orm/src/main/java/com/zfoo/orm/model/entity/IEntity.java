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

package com.zfoo.orm.model.entity;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public interface IEntity<PK extends Comparable<PK>> {

    /**
     * 一个文档的主键
     */
    PK id();


    /**
     * 一个文档的写入到数据库的version版本，version的get和set方法
     */
    default long gvs() {
        return 0L;
    }

    default void svs(long vs) {
    }

}
