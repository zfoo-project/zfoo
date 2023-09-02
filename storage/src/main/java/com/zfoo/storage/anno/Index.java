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

package com.zfoo.storage.anno;

import org.springframework.aot.hint.annotation.Reflective;

import java.lang.annotation.*;

/**
 * EN: the name of the index uses the name of the field attribute, implemented with HaspMap
 * CN: 索引的名称使用字段属性的名称，用HaspMap实现
 *
 * @author godotg
 * @version 3.0
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Reflective
public @interface Index {

    boolean unique() default false;

}
