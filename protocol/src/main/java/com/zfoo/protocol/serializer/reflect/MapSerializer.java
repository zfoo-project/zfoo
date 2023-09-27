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
import com.zfoo.protocol.registration.field.MapField;
import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.Map;

/**
 * @author godotg
 */
public class MapSerializer implements ISerializer {

    public static final MapSerializer INSTANCE = new MapSerializer();

    @Override
    public void writeObject(ByteBuf buffer, Object object, IFieldRegistration fieldRegistration) {
        if (object == null) {
            ByteBufUtils.writeInt(buffer, 0);
            return;
        }

        Map<?, ?> map = (Map<?, ?>) object;
        MapField mapField = (MapField) fieldRegistration;

        // map有几组key、value
        int size = map.size();
        if (size == 0) {
            ByteBufUtils.writeInt(buffer, 0);
            return;
        }
        ByteBufUtils.writeInt(buffer, size);

        // buffer中顺序写入
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            mapField.getMapKeyRegistration().serializer().writeObject(buffer, entry.getKey(), mapField.getMapKeyRegistration());

            mapField.getMapValueRegistration().serializer().writeObject(buffer, entry.getValue(), mapField.getMapValueRegistration());
        }
    }

    @Override
    public Object readObject(ByteBuf buffer, IFieldRegistration fieldRegistration) {
        var size = ByteBufUtils.readInt(buffer);
        var mapField = (MapField) fieldRegistration;
        Map<Object, Object> map = CollectionUtils.newMap(size);

        for (int i = 0; i < size; i++) {
            Object key = mapField.getMapKeyRegistration().serializer().readObject(buffer, mapField.getMapKeyRegistration());

            Object value = mapField.getMapValueRegistration().serializer().readObject(buffer, mapField.getMapValueRegistration());

            map.put(key, value);
        }
        return map;
    }

    @Override
    public Object defaultValue(IFieldRegistration fieldRegistration) {
        return new HashMap<>();
    }

    @Override
    public int predictionLength(IFieldRegistration fieldRegistration) {
        var mapField = (MapField) fieldRegistration;
        var keyLength = mapField.getMapKeyRegistration().serializer().predictionLength(mapField.getMapKeyRegistration());
        var valueLength = mapField.getMapValueRegistration().serializer().predictionLength(mapField.getMapValueRegistration());
        return 7 * (keyLength + valueLength);
    }
}
