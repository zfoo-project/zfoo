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
 */

package com.zfoo.protocol.serializer;

import com.zfoo.protocol.generate.GenerateProtocolFile;
import com.zfoo.protocol.registration.EnhanceUtils;
import com.zfoo.protocol.registration.field.BaseField;
import com.zfoo.protocol.registration.field.IFieldRegistration;
import com.zfoo.protocol.registration.field.MapField;
import com.zfoo.protocol.registration.field.ObjectProtocolField;
import com.zfoo.protocol.serializer.reflect.IntSerializer;
import com.zfoo.protocol.serializer.reflect.LongSerializer;
import com.zfoo.protocol.serializer.reflect.ObjectProtocolSerializer;
import com.zfoo.protocol.serializer.reflect.StringSerializer;
import com.zfoo.protocol.util.StringUtils;

import java.lang.reflect.Field;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public class CutDownMapSerializer implements ICutDownSerializer {

    private static final CutDownMapSerializer INSTANCE = new CutDownMapSerializer();

    public static CutDownMapSerializer getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean writeObject(StringBuilder builder, String objectStr, Field field, IFieldRegistration fieldRegistration, CodeLanguage language) {
        var mapField = (MapField) fieldRegistration;
        var keyRegistration = mapField.getMapKeyRegistration();
        var valueRegistration = mapField.getMapValueRegistration();
        var keySerializer = keyRegistration.serializer();
        var valueSerializer = valueRegistration.serializer();

        // 直接在字节码里调用方法是为了减小生成字节码的体积，下面的代码去掉也不会有任何影响
        if (keyRegistration instanceof BaseField) {
            if (keySerializer == IntSerializer.INSTANCE) {
                if (valueSerializer == IntSerializer.INSTANCE) {
                    builder.append(StringUtils.format("{}.writeIntIntMap($1, (Map){});", EnhanceUtils.byteBufUtils, objectStr));
                    return true;
                } else if (valueSerializer == LongSerializer.INSTANCE) {
                    builder.append(StringUtils.format("{}.writeIntLongMap($1, (Map){});", EnhanceUtils.byteBufUtils, objectStr));
                    return true;
                } else if (valueSerializer == StringSerializer.INSTANCE) {
                    builder.append(StringUtils.format("{}.writeIntStringMap($1, (Map){});", EnhanceUtils.byteBufUtils, objectStr));
                    return true;
                } else if (valueSerializer == ObjectProtocolSerializer.INSTANCE) {
                    builder.append(StringUtils.format("{}.writeIntPacketMap($1, (Map){}, {});", EnhanceUtils.byteBufUtils, objectStr, EnhanceUtils.getProtocolRegistrationFieldNameByProtocolId(((ObjectProtocolField) valueRegistration).getProtocolId())));
                    return true;
                }
            } else if (keySerializer == LongSerializer.INSTANCE) {
                if (valueSerializer == IntSerializer.INSTANCE) {
                    builder.append(StringUtils.format("{}.writeLongIntMap($1, (Map){});", EnhanceUtils.byteBufUtils, objectStr));
                    return true;
                } else if (valueSerializer == LongSerializer.INSTANCE) {
                    builder.append(StringUtils.format("{}.writeLongLongMap($1, (Map){});", EnhanceUtils.byteBufUtils, objectStr));
                    return true;
                } else if (valueSerializer == StringSerializer.INSTANCE) {
                    builder.append(StringUtils.format("{}.writeLongStringMap($1, (Map){});", EnhanceUtils.byteBufUtils, objectStr));
                    return true;
                } else if (valueSerializer == ObjectProtocolSerializer.INSTANCE) {
                    builder.append(StringUtils.format("{}.writeLongPacketMap($1, (Map){}, {});", EnhanceUtils.byteBufUtils, objectStr, EnhanceUtils.getProtocolRegistrationFieldNameByProtocolId(((ObjectProtocolField) valueRegistration).getProtocolId())));
                    return true;
                }

            } else if (keySerializer == StringSerializer.INSTANCE) {
                if (valueSerializer == IntSerializer.INSTANCE) {
                    builder.append(StringUtils.format("{}.writeStringIntMap($1, (Map){});", EnhanceUtils.byteBufUtils, objectStr));
                    return true;
                } else if (valueSerializer == LongSerializer.INSTANCE) {
                    builder.append(StringUtils.format("{}.writeStringLongMap($1, (Map){});", EnhanceUtils.byteBufUtils, objectStr));
                    return true;
                } else if (valueSerializer == StringSerializer.INSTANCE) {
                    builder.append(StringUtils.format("{}.writeStringStringMap($1, (Map){});", EnhanceUtils.byteBufUtils, objectStr));
                    return true;
                } else if (valueSerializer == ObjectProtocolSerializer.INSTANCE) {
                    builder.append(StringUtils.format("{}.writeStringPacketMap($1, (Map){}, {});", EnhanceUtils.byteBufUtils, objectStr, EnhanceUtils.getProtocolRegistrationFieldNameByProtocolId(((ObjectProtocolField) valueRegistration).getProtocolId())));
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public String readObject(StringBuilder builder, Field field, IFieldRegistration fieldRegistration, CodeLanguage language) {
        var mapField = (MapField) fieldRegistration;
        var keyRegistration = mapField.getMapKeyRegistration();
        var valueRegistration = mapField.getMapValueRegistration();
        var keySerializer = keyRegistration.serializer();
        var valueSerializer = valueRegistration.serializer();

        var map = "map" + GenerateProtocolFile.index.getAndIncrement();

        if (keyRegistration instanceof BaseField) {
            if (keySerializer == IntSerializer.INSTANCE) {
                if (valueSerializer == IntSerializer.INSTANCE) {
                    builder.append(StringUtils.format("Map {} = {}.readIntIntMap($1);", map, EnhanceUtils.byteBufUtils));
                    return map;
                } else if (valueSerializer == LongSerializer.INSTANCE) {
                    builder.append(StringUtils.format("Map {} = {}.readIntLongMap($1);", map, EnhanceUtils.byteBufUtils));
                    return map;
                } else if (valueSerializer == StringSerializer.INSTANCE) {
                    builder.append(StringUtils.format("Map {} = {}.readIntStringMap($1);", map, EnhanceUtils.byteBufUtils));
                    return map;
                } else if (valueSerializer == ObjectProtocolSerializer.INSTANCE) {
                    builder.append(StringUtils.format("Map {} = {}.readIntPacketMap($1, {});", map, EnhanceUtils.byteBufUtils, EnhanceUtils.getProtocolRegistrationFieldNameByProtocolId(((ObjectProtocolField) valueRegistration).getProtocolId())));
                    return map;
                }
            } else if (keySerializer == LongSerializer.INSTANCE) {
                if (valueSerializer == IntSerializer.INSTANCE) {
                    builder.append(StringUtils.format("Map {} = {}.readLongIntMap($1);", map, EnhanceUtils.byteBufUtils));
                    return map;
                } else if (valueSerializer == LongSerializer.INSTANCE) {
                    builder.append(StringUtils.format("Map {} = {}.readLongLongMap($1);", map, EnhanceUtils.byteBufUtils));
                    return map;
                } else if (valueSerializer == StringSerializer.INSTANCE) {
                    builder.append(StringUtils.format("Map {} = {}.readLongStringMap($1);", map, EnhanceUtils.byteBufUtils));
                    return map;
                } else if (valueSerializer == ObjectProtocolSerializer.INSTANCE) {
                    builder.append(StringUtils.format("Map {} = {}.readLongPacketMap($1, {});", map, EnhanceUtils.byteBufUtils, EnhanceUtils.getProtocolRegistrationFieldNameByProtocolId(((ObjectProtocolField) valueRegistration).getProtocolId())));
                    return map;
                }
            } else if (keySerializer == StringSerializer.INSTANCE) {
                if (valueSerializer == IntSerializer.INSTANCE) {
                    builder.append(StringUtils.format("Map {} = {}.readStringIntMap($1);", map, EnhanceUtils.byteBufUtils));
                    return map;
                } else if (valueSerializer == LongSerializer.INSTANCE) {
                    builder.append(StringUtils.format("Map {} = {}.readStringLongMap($1);", map, EnhanceUtils.byteBufUtils));
                    return map;
                } else if (valueSerializer == StringSerializer.INSTANCE) {
                    builder.append(StringUtils.format("Map {} = {}.readStringStringMap($1);", map, EnhanceUtils.byteBufUtils));
                    return map;
                } else if (valueSerializer == ObjectProtocolSerializer.INSTANCE) {
                    builder.append(StringUtils.format("Map {} = {}.readStringPacketMap($1, {});", map, EnhanceUtils.byteBufUtils, EnhanceUtils.getProtocolRegistrationFieldNameByProtocolId(((ObjectProtocolField) valueRegistration).getProtocolId())));
                    return map;
                }
            }
        }

        GenerateProtocolFile.index.getAndDecrement();
        return null;
    }
}
