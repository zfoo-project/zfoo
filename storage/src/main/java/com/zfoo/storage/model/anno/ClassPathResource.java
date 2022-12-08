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

package com.zfoo.storage.model.anno;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 类路径资源注解
 *
 * @author godotg
 * @version 4.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ClassPathResource {
    /**
     * 指定对应的资源文件路径，路径为类路径，路径前缀可添加"classpath:"，也可以不添加
     * 若未指定文件路径，则通过配置xml文件中reource标签扫描与类同名文件，并且只在前缀为"classpath:"的路径中扫描
     * @return
     */
    @AliasFor("path")
    String value() default "";
    @AliasFor("value")
    String path() default "";

    /**
     * 指定是否开启全局类路径搜索，默认不开启，相当于路径前缀为"classpath*"
     * @return
     */
    boolean isAllJar() default false;
}
