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

import com.zfoo.protocol.serializer.ArraySerializer;
import com.zfoo.protocol.serializer.ISerializer;

import java.lang.reflect.Field;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public class ArrayField implements IFieldRegistration {

    private IFieldRegistration arrayElementRegistration;
    private Field field;

    public static ArrayField valueOf(Field field, IFieldRegistration arrayElementRegistration) {
        ArrayField arrayField = new ArrayField();
        arrayField.field = field;
        arrayField.arrayElementRegistration = arrayElementRegistration;
        return arrayField;
    }


    public Field getField() {
        return field;
    }


    @Override
    public ISerializer serializer() {
        return ArraySerializer.getInstance();
    }

    public IFieldRegistration getArrayElementRegistration() {
        return arrayElementRegistration;
    }
}

