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
 *
 * EN: You can specify the corresponding resource file name (only the file name is specified, no file suffix is required)
 * CN: 可以指定对应的资源文件名（只指定文件名，不需要文件后缀），如果不指定资源文件名，则默认通过扫描路径获取与类名相同的文件资源
 *
 * @author godotg
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Reflective
public @interface Storage {
    String value() default "";
}
