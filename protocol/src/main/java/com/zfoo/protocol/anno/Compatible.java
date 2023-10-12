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

package com.zfoo.protocol.anno;

import java.lang.annotation.*;

/**
 * EN: Make the fields of the old and new protocols compatible in different version
 * CN: 让新老协议的字段前后兼容
 *
 * @author godotg
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Compatible {

    /**
     * EN: new compatible field value must be always greater than old compatible field value
     * CN: 新的兼容字段值必须始终大于旧的兼容字段
     */
    int value();

}
