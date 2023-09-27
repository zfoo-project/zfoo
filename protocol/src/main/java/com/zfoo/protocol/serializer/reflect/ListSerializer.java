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
import com.zfoo.protocol.registration.field.IFieldRegistration;
import com.zfoo.protocol.registration.field.ListField;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

/**
 * @author godotg
 */
public class ListSerializer implements ISerializer {

    public static final ListSerializer INSTANCE = new ListSerializer();

    @Override
    public void writeObject(ByteBuf buffer, Object object, IFieldRegistration fieldRegistration) {
        if (object == null) {
            ByteBufUtils.writeInt(buffer, 0);
            return;
        }

        List<?> list = (List<?>) object;
        ListField listField = (ListField) fieldRegistration;

        int size = list.size();
        if (size == 0) {
            ByteBufUtils.writeInt(buffer, 0);
            return;
        }
        ByteBufUtils.writeInt(buffer, size);

        for (Object element : list) {
            listField.getListElementRegistration().serializer().writeObject(buffer, element, listField.getListElementRegistration());
        }
    }

    @Override
    public Object readObject(ByteBuf buffer, IFieldRegistration fieldRegistration) {
        var size = ByteBufUtils.readInt(buffer);
        var listField = (ListField) fieldRegistration;
        List<Object> list = CollectionUtils.newList(size);
        for (int i = 0; i < size; i++) {
            Object value = listField.getListElementRegistration().serializer().readObject(buffer, listField.getListElementRegistration());
            list.add(value);
        }

        return list;
    }

    @Override
    public Object defaultValue(IFieldRegistration fieldRegistration) {
        return new ArrayList<>();
    }

    @Override
    public int predictionLength(IFieldRegistration fieldRegistration) {
        ListField listField = (ListField) fieldRegistration;
        var length = listField.getListElementRegistration().serializer().predictionLength(listField.getListElementRegistration());
        return 7 * length;
    }
}
