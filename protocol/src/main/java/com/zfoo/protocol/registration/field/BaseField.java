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

package com.zfoo.protocol.registration.field;

import com.zfoo.protocol.serializer.reflect.ISerializer;

/**
 * 一个包里所包含的变量还有这个变量的序列化器
 * 描述boolean，byte，short，int，long，float，double，char，String等基本序列化器
 *
 * @author godotg
 */
public class BaseField implements IFieldRegistration {

    private ISerializer serializer;

    public static BaseField valueOf(ISerializer serializer) {
        BaseField packetField = new BaseField();
        packetField.serializer = serializer;
        return packetField;
    }

    @Override
    public ISerializer serializer() {
        return serializer;
    }

}
