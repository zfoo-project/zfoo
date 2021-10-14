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

package com.zfoo.protocol.serializer.enhance;

import com.zfoo.protocol.generate.GenerateProtocolFile;
import com.zfoo.protocol.registration.EnhanceUtils;
import com.zfoo.protocol.registration.field.BaseField;
import com.zfoo.protocol.registration.field.IFieldRegistration;
import com.zfoo.protocol.registration.field.MapField;
import com.zfoo.protocol.registration.field.ObjectProtocolField;
import com.zfoo.protocol.serializer.IntSerializer;
import com.zfoo.protocol.serializer.LongSerializer;
import com.zfoo.protocol.serializer.ObjectProtocolSerializer;
import com.zfoo.protocol.serializer.StringSerializer;
import com.zfoo.protocol.util.StringUtils;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public class EnhanceMapSerializer implements IEnhanceSerializer {

    @Override
    public void writeObject(StringBuilder builder, String objectStr, Field field, IFieldRegistration fieldRegistration) {
        var mapField = (MapField) fieldRegistration;
        var keyRegistration = mapField.getMapKeyRegistration();
        var valueRegistration = mapField.getMapValueRegistration();
        var keySerializer = keyRegistration.serializer();
        var valueSerializer = valueRegistration.serializer();

        // 直接在字节码里调用方法是为了减小生成字节码的体积，下面的代码去掉也不会有任何影响
        if (keyRegistration instanceof BaseField) {
            if (valueRegistration instanceof BaseField) {
                if (keySerializer == IntSerializer.getInstance() && valueSerializer == IntSerializer.getInstance()) {
                    builder.append(StringUtils.format("{}.writeIntIntMap($1, (Map){});", EnhanceUtils.byteBufUtils, objectStr));
                    return;
                } else if (keySerializer == IntSerializer.getInstance() && valueSerializer == LongSerializer.getInstance()) {
                    builder.append(StringUtils.format("{}.writeIntLongMap($1, (Map){});", EnhanceUtils.byteBufUtils, objectStr));
                    return;
                } else if (keySerializer == IntSerializer.getInstance() && valueSerializer == StringSerializer.getInstance()) {
                    builder.append(StringUtils.format("{}.writeIntStringMap($1, (Map){});", EnhanceUtils.byteBufUtils, objectStr));
                    return;
                } else if (keySerializer == LongSerializer.getInstance() && valueSerializer == IntSerializer.getInstance()) {
                    builder.append(StringUtils.format("{}.writeLongIntMap($1, (Map){});", EnhanceUtils.byteBufUtils, objectStr));
                    return;
                } else if (keySerializer == LongSerializer.getInstance() && valueSerializer == LongSerializer.getInstance()) {
                    builder.append(StringUtils.format("{}.writeLongLongMap($1, (Map){});", EnhanceUtils.byteBufUtils, objectStr));
                    return;
                } else if (keySerializer == LongSerializer.getInstance() && valueSerializer == StringSerializer.getInstance()) {
                    builder.append(StringUtils.format("{}.writeLongStringMap($1, (Map){});", EnhanceUtils.byteBufUtils, objectStr));
                    return;
                } else if (keySerializer == StringSerializer.getInstance() && valueSerializer == IntSerializer.getInstance()) {
                    builder.append(StringUtils.format("{}.writeStringIntMap($1, (Map){});", EnhanceUtils.byteBufUtils, objectStr));
                    return;
                } else if (keySerializer == StringSerializer.getInstance() && valueSerializer == LongSerializer.getInstance()) {
                    builder.append(StringUtils.format("{}.writeStringLongMap($1, (Map){});", EnhanceUtils.byteBufUtils, objectStr));
                    return;
                } else if (keySerializer == StringSerializer.getInstance() && valueSerializer == StringSerializer.getInstance()) {
                    builder.append(StringUtils.format("{}.writeStringStringMap($1, (Map){});", EnhanceUtils.byteBufUtils, objectStr));
                    return;
                }
            } else if (valueRegistration instanceof ObjectProtocolField) {
                var valueProtocolRegistration = (ObjectProtocolField) valueRegistration;
                if (keySerializer == IntSerializer.getInstance() && valueSerializer == ObjectProtocolSerializer.getInstance()) {
                    builder.append(StringUtils.format("{}.writeIntPacketMap($1, (Map){}, {});", EnhanceUtils.byteBufUtils, objectStr, EnhanceUtils.getProtocolRegistrationFieldNameByProtocolId(valueProtocolRegistration.getProtocolId())));
                    return;
                } else if (keySerializer == LongSerializer.getInstance() && valueSerializer == ObjectProtocolSerializer.getInstance()) {
                    builder.append(StringUtils.format("{}.writeLongPacketMap($1, (Map){}, {});", EnhanceUtils.byteBufUtils, objectStr, EnhanceUtils.getProtocolRegistrationFieldNameByProtocolId(valueProtocolRegistration.getProtocolId())));
                    return;
                } else if (keySerializer == StringSerializer.getInstance() && valueSerializer == ObjectProtocolSerializer.getInstance()) {
                    builder.append(StringUtils.format("{}.writeStringPacketMap($1, (Map){}, {});", EnhanceUtils.byteBufUtils, objectStr, EnhanceUtils.getProtocolRegistrationFieldNameByProtocolId(valueProtocolRegistration.getProtocolId())));
                    return;
                }
            }
        }

        var map = "map" + GenerateProtocolFile.index.getAndIncrement();
        builder.append(StringUtils.format("Map {} = (Map){};", map, objectStr));
        builder.append(StringUtils.format("{}.writeInt($1, CollectionUtils.size({}));", EnhanceUtils.byteBufUtils, map));

        var iterator = "iterator" + GenerateProtocolFile.index.getAndIncrement();
        builder.append(StringUtils.format("Iterator {} = CollectionUtils.iterator({});", iterator, map));
        builder.append(StringUtils.format("while({}.hasNext()) {", iterator));

        var entry = "entry" + GenerateProtocolFile.index.getAndIncrement();
        builder.append(StringUtils.format("{} {}=({}){}.next();", Map.Entry.class.getCanonicalName(), entry, Map.Entry.class.getCanonicalName(), iterator));

        var key = "key" + GenerateProtocolFile.index.getAndIncrement();
        builder.append(StringUtils.format("Object {} = {}.getKey();", key, entry));

        var value = "value" + GenerateProtocolFile.index.getAndIncrement();
        builder.append(StringUtils.format("Object {} = {}.getValue();", value, entry));

        EnhanceUtils.enhanceSerializer(keyRegistration.serializer()).writeObject(builder, key, field, keyRegistration);
        EnhanceUtils.enhanceSerializer(valueRegistration.serializer()).writeObject(builder, value, field, valueRegistration);

        builder.append("}");
    }

    @Override
    public String readObject(StringBuilder builder, Field field, IFieldRegistration fieldRegistration) {
        var mapField = (MapField) fieldRegistration;
        var keyRegistration = mapField.getMapKeyRegistration();
        var valueRegistration = mapField.getMapValueRegistration();
        var keySerializer = keyRegistration.serializer();
        var valueSerializer = valueRegistration.serializer();

        var map = "map" + GenerateProtocolFile.index.getAndIncrement();

        if (keyRegistration instanceof BaseField) {
            if (valueRegistration instanceof BaseField) {
                if (keySerializer == IntSerializer.getInstance() && valueSerializer == IntSerializer.getInstance()) {
                    builder.append(StringUtils.format("Map {} = {}.readIntIntMap($1);", map, EnhanceUtils.byteBufUtils));
                    return map;
                } else if (keySerializer == IntSerializer.getInstance() && valueSerializer == LongSerializer.getInstance()) {
                    builder.append(StringUtils.format("Map {} = {}.readIntLongMap($1);", map, EnhanceUtils.byteBufUtils));
                    return map;
                } else if (keySerializer == IntSerializer.getInstance() && valueSerializer == StringSerializer.getInstance()) {
                    builder.append(StringUtils.format("Map {} = {}.readIntStringMap($1);", map, EnhanceUtils.byteBufUtils));
                    return map;
                } else if (keySerializer == LongSerializer.getInstance() && valueSerializer == IntSerializer.getInstance()) {
                    builder.append(StringUtils.format("Map {} = {}.readLongIntMap($1);", map, EnhanceUtils.byteBufUtils));
                    return map;
                } else if (keySerializer == LongSerializer.getInstance() && valueSerializer == LongSerializer.getInstance()) {
                    builder.append(StringUtils.format("Map {} = {}.readLongLongMap($1);", map, EnhanceUtils.byteBufUtils));
                    return map;
                } else if (keySerializer == LongSerializer.getInstance() && valueSerializer == StringSerializer.getInstance()) {
                    builder.append(StringUtils.format("Map {} = {}.readLongStringMap($1);", map, EnhanceUtils.byteBufUtils));
                    return map;
                } else if (keySerializer == StringSerializer.getInstance() && valueSerializer == IntSerializer.getInstance()) {
                    builder.append(StringUtils.format("Map {} = {}.readStringIntMap($1);", map, EnhanceUtils.byteBufUtils));
                    return map;
                } else if (keySerializer == StringSerializer.getInstance() && valueSerializer == LongSerializer.getInstance()) {
                    builder.append(StringUtils.format("Map {} = {}.readStringLongMap($1);", map, EnhanceUtils.byteBufUtils));
                    return map;
                } else if (keySerializer == StringSerializer.getInstance() && valueSerializer == StringSerializer.getInstance()) {
                    builder.append(StringUtils.format("Map {} = {}.readStringStringMap($1);", map, EnhanceUtils.byteBufUtils));
                    return map;
                }
            } else if (valueRegistration instanceof ObjectProtocolField) {
                var valueProtocolRegistration = (ObjectProtocolField) valueRegistration;
                if (keySerializer == IntSerializer.getInstance() && valueSerializer == ObjectProtocolSerializer.getInstance()) {
                    builder.append(StringUtils.format("Map {} = {}.readIntPacketMap($1, {});", map, EnhanceUtils.byteBufUtils, EnhanceUtils.getProtocolRegistrationFieldNameByProtocolId(valueProtocolRegistration.getProtocolId())));
                    return map;
                } else if (keySerializer == LongSerializer.getInstance() && valueSerializer == ObjectProtocolSerializer.getInstance()) {
                    builder.append(StringUtils.format("Map {} = {}.readLongPacketMap($1, {});", map, EnhanceUtils.byteBufUtils, EnhanceUtils.getProtocolRegistrationFieldNameByProtocolId(valueProtocolRegistration.getProtocolId())));
                    return map;
                } else if (keySerializer == StringSerializer.getInstance() && valueSerializer == ObjectProtocolSerializer.getInstance()) {
                    builder.append(StringUtils.format("Map {} = {}.readStringPacketMap($1, {});", map, EnhanceUtils.byteBufUtils, EnhanceUtils.getProtocolRegistrationFieldNameByProtocolId(valueProtocolRegistration.getProtocolId())));
                    return map;
                }
            }
        }

        var size = "size" + GenerateProtocolFile.index.getAndIncrement();
        builder.append(StringUtils.format("int {}={}.readInt($1);", size, EnhanceUtils.byteBufUtils));
        builder.append(StringUtils.format("Map {} = CollectionUtils.newFixedMap({});", map, size));

        var i = "i" + GenerateProtocolFile.index.getAndIncrement();
        builder.append(StringUtils.format("for(int {}=0; {}<{}; {}++){", i, i, size, i));

        var keyObject = EnhanceUtils.enhanceSerializer(keyRegistration.serializer()).readObject(builder, field, keyRegistration);
        var valueObject = EnhanceUtils.enhanceSerializer(valueRegistration.serializer()).readObject(builder, field, valueRegistration);

        builder.append(StringUtils.format("{}.put({},{});}", map, keyObject, valueObject));
        return map;
    }

}
