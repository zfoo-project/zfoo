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
 *
 */

package com.zfoo.protocol.serializer.reflect;

import com.zfoo.protocol.buffer.ByteBufUtils;
import com.zfoo.protocol.registration.field.ArrayField;
import com.zfoo.protocol.registration.field.IFieldRegistration;
import io.netty.buffer.ByteBuf;

import java.lang.reflect.Array;

/**
 * @author godotg
 */
public class ArraySerializer implements ISerializer {

    public static final ArraySerializer INSTANCE = new ArraySerializer();

    @Override
    public void writeObject(ByteBuf buffer, Object object, IFieldRegistration fieldRegistration) {
        if (object == null) {
            ByteBufUtils.writeInt(buffer, 0);
            return;
        }

        ArrayField arrayField = (ArrayField) fieldRegistration;

        int length = Array.getLength(object);
        if (length == 0) {
            ByteBufUtils.writeInt(buffer, 0);
            return;
        }

        ByteBufUtils.writeInt(buffer, length);

        for (int i = 0; i < length; i++) {
            Object element = Array.get(object, i);
            arrayField.getArrayElementRegistration().serializer().writeObject(buffer, element, arrayField.getArrayElementRegistration());
        }
    }

    @Override
    public Object readObject(ByteBuf buffer, IFieldRegistration fieldRegistration) {
        var length = ByteBufUtils.readInt(buffer);
        ArrayField arrayField = (ArrayField) fieldRegistration;
        if (length <= 0) {
            return Array.newInstance(arrayField.getType(), 0);
        }

        Object array = Array.newInstance(arrayField.getType(), length);

        for (var i = 0; i < length; i++) {
            Object value = arrayField.getArrayElementRegistration().serializer().readObject(buffer, arrayField.getArrayElementRegistration());
            Array.set(array, i, value);
        }

        return array;
    }

    @Override
    public Object defaultValue(IFieldRegistration fieldRegistration) {
        ArrayField arrayField = (ArrayField) fieldRegistration;
        return Array.newInstance(arrayField.getType(), 0);
    }

    @Override
    public int predictionLength(IFieldRegistration fieldRegistration) {
        ArrayField arrayField = (ArrayField) fieldRegistration;
        var length = arrayField.getArrayElementRegistration().serializer().predictionLength(arrayField.getArrayElementRegistration());
        return 7 * length;
    }

}
