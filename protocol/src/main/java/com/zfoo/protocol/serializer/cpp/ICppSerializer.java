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

package com.zfoo.protocol.serializer.cpp;

import com.zfoo.protocol.model.Pair;
import com.zfoo.protocol.registration.field.IFieldRegistration;

import java.lang.reflect.Field;

/**
 * @author godotg
 */
public interface ICppSerializer {

    /**
     * 获取属性的类型和名称
     */
    Pair<String, String> field(Field field, IFieldRegistration fieldRegistration);

    void writeObject(StringBuilder builder, String objectStr, int deep, Field field, IFieldRegistration fieldRegistration);

    String readObject(StringBuilder builder, int deep, Field field, IFieldRegistration fieldRegistration);

}
