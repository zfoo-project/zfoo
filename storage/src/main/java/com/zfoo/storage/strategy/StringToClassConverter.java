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

package com.zfoo.storage.strategy;

import com.zfoo.protocol.util.StringUtils;
import com.zfoo.storage.StorageContext;
import org.springframework.core.convert.converter.Converter;

/**
 * @author godotg
 * @version 4.0
 */
public class StringToClassConverter implements Converter<String, Class<?>> {


    @Override
    public Class<?> convert(String source) {
        if (!source.contains(".") && !source.startsWith("[")) {
            source = "java.lang." + source;
        }

        ClassLoader loader = null;

        StorageContext context = StorageContext.getInstance();

        if (context != null) {
            loader = StorageContext.getApplicationContext().getClassLoader();
        } else {
            loader = Thread.currentThread().getContextClassLoader();
        }

        try {
            return Class.forName(source, true, loader);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(StringUtils.format("无法将字符串[{}]转换为Class对象", source));
        }

    }
}
