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
import com.zfoo.protocol.collection.CollectionUtils;
import com.zfoo.protocol.registration.field.ArrayField;
import com.zfoo.protocol.registration.field.IFieldRegistration;
import com.zfoo.protocol.registration.field.SetField;
import io.netty.buffer.ByteBuf;

import java.util.HashSet;
import java.util.Set;

/**
 * @author godotg
 */
public class SetSerializer implements ISerializer {

    public static final SetSerializer INSTANCE = new SetSerializer();

    @Override
    public void writeObject(ByteBuf buffer, Object object, IFieldRegistration fieldRegistration) {
        if (object == null) {
            ByteBufUtils.writeInt(buffer, 0);
            return;
        }

        Set<?> set = (Set<?>) object;
        SetField setField = (SetField) fieldRegistration;

        int size = set.size();
        if (size == 0) {
            ByteBufUtils.writeInt(buffer, 0);
            return;
        }
        ByteBufUtils.writeInt(buffer, size);

        for (Object element : set) {
            setField.getSetElementRegistration().serializer().writeObject(buffer, element, setField.getSetElementRegistration());
        }
    }

    @Override
    public Object readObject(ByteBuf buffer, IFieldRegistration fieldRegistration) {
        var size = ByteBufUtils.readInt(buffer);
        var setField = (SetField) fieldRegistration;
        Set<Object> set = CollectionUtils.newSet(size);

        for (int i = 0; i < size; i++) {
            Object value = setField.getSetElementRegistration().serializer().readObject(buffer, setField.getSetElementRegistration());
            set.add(value);
        }

        return set;
    }

    @Override
    public Object defaultValue(IFieldRegistration fieldRegistration) {
        return new HashSet<>();
    }

    @Override
    public int predictionLength(IFieldRegistration fieldRegistration) {
        var setField = (SetField) fieldRegistration;
        var length = setField.getSetElementRegistration().serializer().predictionLength(setField.getSetElementRegistration());
        return 7 * length;
    }

}
