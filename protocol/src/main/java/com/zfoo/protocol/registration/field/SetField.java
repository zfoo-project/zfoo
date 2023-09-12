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
import com.zfoo.protocol.serializer.reflect.SetSerializer;

import java.lang.reflect.Type;

/**
 * @author godotg
 */
public class SetField implements IFieldRegistration {

    private IFieldRegistration setElementRegistration;
    private Type type;

    public static SetField valueOf(IFieldRegistration listElementRegistration, Type type) {
        SetField setField = new SetField();
        setField.setElementRegistration = listElementRegistration;
        setField.type = type;
        return setField;
    }

    @Override
    public ISerializer serializer() {
        return SetSerializer.INSTANCE;
    }

    public IFieldRegistration getSetElementRegistration() {
        return setElementRegistration;
    }

    public Type getType() {
        return type;
    }

}
