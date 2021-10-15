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
import com.zfoo.protocol.registration.field.IFieldRegistration;
import com.zfoo.protocol.registration.field.ListField;
import com.zfoo.protocol.registration.field.ObjectProtocolField;
import com.zfoo.protocol.util.StringUtils;

import java.lang.reflect.Field;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public class CutDownListSerializer implements ICutDownSerializer {

    private static final CutDownListSerializer INSTANCE = new CutDownListSerializer();

    public static CutDownListSerializer getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean writeObject(StringBuilder builder, String objectStr, Field field, IFieldRegistration fieldRegistration, CodeLanguage language) {
        var listField = (ListField) fieldRegistration;

        switch (listField.getType().getTypeName()) {
            case "java.util.List<java.lang.Integer>":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}.writeIntList($1, (List){});", EnhanceUtils.byteBufUtils, objectStr));
                        return true;
                }
                break;
            case "java.util.List<java.lang.Long>": {
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}.writeLongList($1, (List){});", EnhanceUtils.byteBufUtils, objectStr));
                        return true;
                }
                break;
            }
            case "java.util.List<java.lang.String>":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}.writeStringList($1, (List){});", EnhanceUtils.byteBufUtils, objectStr));
                        return true;
                }
                break;
            default:
        }

        // List<IPacket>
        if (listField.getListElementRegistration() instanceof ObjectProtocolField) {
            var objectProtocolField = (ObjectProtocolField) listField.getListElementRegistration();
            switch (language) {
                case Enhance:
                    builder.append(StringUtils.format("{}.writePacketList($1, (List){}, {});", EnhanceUtils.byteBufUtils, objectStr, EnhanceUtils.getProtocolRegistrationFieldNameByProtocolId(objectProtocolField.getProtocolId())));
                    return true;
            }
        }
        return false;
    }

    @Override
    public String readObject(StringBuilder builder, Field field, IFieldRegistration fieldRegistration, CodeLanguage language) {
        var listField = (ListField) fieldRegistration;
        var list = "list" + GenerateProtocolFile.index.getAndIncrement();

        switch (listField.getType().getTypeName()) {
            case "java.util.List<java.lang.Integer>":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("List {} = {}.readIntList($1);", list, EnhanceUtils.byteBufUtils));
                        return list;
                }
                break;
            case "java.util.List<java.lang.Long>":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("List {} = {}.readLongList($1);", list, EnhanceUtils.byteBufUtils));
                        return list;
                }
                break;
            case "java.util.List<java.lang.String>":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("List {} = {}.readStringList($1);", list, EnhanceUtils.byteBufUtils));
                        return list;
                }
                break;
            default:
        }

        if (listField.getListElementRegistration() instanceof ObjectProtocolField) {
            var objectProtocolField = (ObjectProtocolField) listField.getListElementRegistration();
            switch (language) {
                case Enhance:
                    builder.append(StringUtils.format("List {} = {}.readPacketList($1, {});", list, EnhanceUtils.byteBufUtils, EnhanceUtils.getProtocolRegistrationFieldNameByProtocolId(objectProtocolField.getProtocolId())));
                    return list;
            }
        }

        GenerateProtocolFile.index.getAndDecrement();
        return null;
    }
}
