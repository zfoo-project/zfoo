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

import com.zfoo.protocol.serializer.reflect.ArraySerializer;
import com.zfoo.protocol.serializer.reflect.ISerializer;

/**
 * @author godotg
 */
public class ArrayField implements IFieldRegistration {

    private IFieldRegistration arrayElementRegistration;
    private Class<?> type;

    public static ArrayField valueOf(IFieldRegistration arrayElementRegistration, Class<?> type) {
        ArrayField arrayField = new ArrayField();
        arrayField.arrayElementRegistration = arrayElementRegistration;
        arrayField.type = type;
        return arrayField;
    }

    public Class<?> getType() {
        return type;
    }

    @Override
    public ISerializer serializer() {
        return ArraySerializer.INSTANCE;
    }

    public IFieldRegistration getArrayElementRegistration() {
        return arrayElementRegistration;
    }
}

