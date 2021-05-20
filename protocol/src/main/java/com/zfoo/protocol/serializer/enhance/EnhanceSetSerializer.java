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

import com.zfoo.protocol.registration.EnhanceUtils;
import com.zfoo.protocol.registration.field.IFieldRegistration;
import com.zfoo.protocol.registration.field.ObjectProtocolField;
import com.zfoo.protocol.registration.field.SetField;
import com.zfoo.protocol.serializer.GenerateUtils;
import com.zfoo.protocol.util.StringUtils;

import java.lang.reflect.Field;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public class EnhanceSetSerializer implements IEnhanceSerializer {

    @Override
    public void writeObject(StringBuilder builder, String objectStr, Field field, IFieldRegistration fieldRegistration) {
        var setField = (SetField) fieldRegistration;

        switch (setField.getType().getTypeName()) {
            case "java.util.Set<java.lang.Integer>":
                builder.append(StringUtils.format("{}.writeIntSet($1, (Set){});", EnhanceUtils.byteBufUtils, objectStr));
                return;
            case "java.util.Set<java.lang.Long>":
                builder.append(StringUtils.format("{}.writeLongSet($1, (Set){});", EnhanceUtils.byteBufUtils, objectStr));
                return;
            case "java.util.Set<java.lang.String>":
                builder.append(StringUtils.format("{}.writeStringSet($1, (Set){});", EnhanceUtils.byteBufUtils, objectStr));
                return;
            default:
        }

        // Set<IPacket>
        if (setField.getSetElementRegistration() instanceof ObjectProtocolField) {
            var objectProtocolField = (ObjectProtocolField) setField.getSetElementRegistration();
            builder.append(StringUtils.format("{}.writePacketSet($1, (Set){}, {});", EnhanceUtils.byteBufUtils, objectStr, EnhanceUtils.getProtocolRegistrationFieldNameByProtocolId(objectProtocolField.getProtocolId())));
            return;
        }

        var set = "set" + GenerateUtils.index.getAndIncrement();
        builder.append(StringUtils.format("Set {} = (Set){};", set, objectStr));

        builder.append(StringUtils.format("{}.writeInt($1, CollectionUtils.size({}));", EnhanceUtils.byteBufUtils, set));

        var iterator = "iterator" + GenerateUtils.index.getAndIncrement();
        builder.append(StringUtils.format("Iterator {} = CollectionUtils.iterator({});", iterator, set));
        builder.append(StringUtils.format("while({}.hasNext()) {", iterator));

        var element = "element" + GenerateUtils.index.getAndIncrement();
        builder.append(StringUtils.format("Object {}={}.next();", element, iterator));
        EnhanceUtils.enhanceSerializer(setField.getSetElementRegistration().serializer())
                .writeObject(builder, element, field, setField.getSetElementRegistration());
        builder.append("}");

    }

    @Override
    public String readObject(StringBuilder builder, Field field, IFieldRegistration fieldRegistration) {
        var setField = (SetField) fieldRegistration;
        var set = "set" + GenerateUtils.index.getAndIncrement();

        switch (setField.getType().getTypeName()) {
            case "java.util.Set<java.lang.Integer>":
                builder.append(StringUtils.format("Set {} = {}.readIntSet($1);", set, EnhanceUtils.byteBufUtils));
                return set;
            case "java.util.Set<java.lang.Long>":
                builder.append(StringUtils.format("Set {} = {}.readLongSet($1);", set, EnhanceUtils.byteBufUtils));
                return set;
            case "java.util.Set<java.lang.String>":
                builder.append(StringUtils.format("Set {} = {}.readStringSet($1);", set, EnhanceUtils.byteBufUtils));
                return set;
            default:
        }

        if (setField.getSetElementRegistration() instanceof ObjectProtocolField) {
            var objectProtocolField = (ObjectProtocolField) setField.getSetElementRegistration();
            builder.append(StringUtils.format("Set {} = {}.readPacketSet($1, {});", set, EnhanceUtils.byteBufUtils, EnhanceUtils.getProtocolRegistrationFieldNameByProtocolId(objectProtocolField.getProtocolId())));
            return set;
        }

        var size = "size" + GenerateUtils.index.getAndIncrement();
        builder.append(StringUtils.format("int {} = {}.readInt($1);", size, EnhanceUtils.byteBufUtils));
        builder.append(StringUtils.format("Set {} = CollectionUtils.newFixedSet({});", set, size));

        var i = "i" + GenerateUtils.index.getAndIncrement();
        builder.append(StringUtils.format("for(int {}=0; {}<{}; {}++){", i, i, size, i));

        var readObject = EnhanceUtils.enhanceSerializer(setField.getSetElementRegistration().serializer()).readObject(builder, field, setField.getSetElementRegistration());
        builder.append(StringUtils.format("{}.add({});}", set, readObject));
        return set;
    }

}
