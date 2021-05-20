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
import com.zfoo.protocol.registration.field.ListField;
import com.zfoo.protocol.registration.field.ObjectProtocolField;
import com.zfoo.protocol.serializer.GenerateUtils;
import com.zfoo.protocol.util.StringUtils;

import java.lang.reflect.Field;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public class EnhanceListSerializer implements IEnhanceSerializer {

    @Override
    public void writeObject(StringBuilder builder, String objectStr, Field field, IFieldRegistration fieldRegistration) {
        var listField = (ListField) fieldRegistration;

        switch (listField.getType().getTypeName()) {
            case "java.util.List<java.lang.Integer>":
                builder.append(StringUtils.format("{}.writeIntList($1, (List){});", EnhanceUtils.byteBufUtils, objectStr));
                return;
            case "java.util.List<java.lang.Long>":
                builder.append(StringUtils.format("{}.writeLongList($1, (List){});", EnhanceUtils.byteBufUtils, objectStr));
                return;
            case "java.util.List<java.lang.String>":
                builder.append(StringUtils.format("{}.writeStringList($1, (List){});", EnhanceUtils.byteBufUtils, objectStr));
                return;
            default:
        }

        // List<IPacket>
        if (listField.getListElementRegistration() instanceof ObjectProtocolField) {
            var objectProtocolField = (ObjectProtocolField) listField.getListElementRegistration();
            builder.append(StringUtils.format("{}.writePacketList($1, (List){}, {});", EnhanceUtils.byteBufUtils, objectStr, EnhanceUtils.getProtocolRegistrationFieldNameByProtocolId(objectProtocolField.getProtocolId())));
            return;
        }

        var list = "list" + GenerateUtils.index.getAndIncrement();
        builder.append(StringUtils.format("List {} = (List){};", list, objectStr));

        builder.append(StringUtils.format("{}.writeInt($1, CollectionUtils.size({}));", EnhanceUtils.byteBufUtils, list));

        var iterator = "iterator" + GenerateUtils.index.getAndIncrement();
        builder.append(StringUtils.format("Iterator {} = CollectionUtils.iterator({});", iterator, list));
        builder.append(StringUtils.format("while({}.hasNext()){", iterator));

        var element = "element" + GenerateUtils.index.getAndIncrement();
        builder.append(StringUtils.format("Object {}={}.next();", element, iterator));
        EnhanceUtils.enhanceSerializer(listField.getListElementRegistration().serializer())
                .writeObject(builder, element, field, listField.getListElementRegistration());
        builder.append("}");

    }

    @Override
    public String readObject(StringBuilder builder, Field field, IFieldRegistration fieldRegistration) {
        var listField = (ListField) fieldRegistration;
        var list = "list" + GenerateUtils.index.getAndIncrement();

        switch (listField.getType().getTypeName()) {
            case "java.util.List<java.lang.Integer>":
                builder.append(StringUtils.format("List {} = {}.readIntList($1);", list, EnhanceUtils.byteBufUtils));
                return list;
            case "java.util.List<java.lang.Long>":
                builder.append(StringUtils.format("List {} = {}.readLongList($1);", list, EnhanceUtils.byteBufUtils));
                return list;
            case "java.util.List<java.lang.String>":
                builder.append(StringUtils.format("List {} = {}.readStringList($1);", list, EnhanceUtils.byteBufUtils));
                return list;
            default:
        }

        if (listField.getListElementRegistration() instanceof ObjectProtocolField) {
            var objectProtocolField = (ObjectProtocolField) listField.getListElementRegistration();
            builder.append(StringUtils.format("List {} = {}.readPacketList($1, {});", list, EnhanceUtils.byteBufUtils, EnhanceUtils.getProtocolRegistrationFieldNameByProtocolId(objectProtocolField.getProtocolId())));
            return list;
        }

        var size = "size" + GenerateUtils.index.getAndIncrement();
        builder.append(StringUtils.format("int {}={}.readInt($1);", size, EnhanceUtils.byteBufUtils));

        builder.append(StringUtils.format("List {} = CollectionUtils.newFixedList({});", list, size));

        var i = "i" + GenerateUtils.index.getAndIncrement();

        builder.append(StringUtils.format("for(int {}=0; {}<{}; {}++){", i, i, size, i));
        var readObject = EnhanceUtils.enhanceSerializer(listField.getListElementRegistration().serializer()).readObject(builder, field, listField.getListElementRegistration());
        builder.append(StringUtils.format("{}.add({});}", list, readObject));
        return list;
    }

}
