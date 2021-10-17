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

import static com.zfoo.protocol.util.FileUtils.LS;

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
        var flag = true;

        switch (listField.getType().getTypeName()) {
            case "java.util.List<java.lang.Boolean>":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}.writeBooleanList($1, (List){});", EnhanceUtils.byteBufUtils, objectStr));
                        break;
                    case JavaScript:
                        builder.append(StringUtils.format("buffer.writeBooleanArray({});", objectStr)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "java.util.List<java.lang.Byte>":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}.writeByteList($1, (List){});", EnhanceUtils.byteBufUtils, objectStr));
                        break;
                    case JavaScript:
                        builder.append(StringUtils.format("buffer.writeByteArray({});", objectStr)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "java.util.List<java.lang.Short>":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}.writeShortList($1, (List){});", EnhanceUtils.byteBufUtils, objectStr));
                        break;
                    case JavaScript:
                        builder.append(StringUtils.format("buffer.writeShortArray({});", objectStr)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "java.util.List<java.lang.Integer>":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}.writeIntList($1, (List){});", EnhanceUtils.byteBufUtils, objectStr));
                        break;
                    case JavaScript:
                        builder.append(StringUtils.format("buffer.writeIntArray({});", objectStr)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "java.util.List<java.lang.Long>": {
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}.writeLongList($1, (List){});", EnhanceUtils.byteBufUtils, objectStr));
                        break;
                    case JavaScript:
                        builder.append(StringUtils.format("buffer.writeLongArray({});", objectStr)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            }
            case "java.util.List<java.lang.Float>": {
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}.writeFloatList($1, (List){});", EnhanceUtils.byteBufUtils, objectStr));
                        break;
                    case JavaScript:
                        builder.append(StringUtils.format("buffer.writeFloatArray({});", objectStr)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            }
            case "java.util.List<java.lang.Double>": {
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}.writeDoubleList($1, (List){});", EnhanceUtils.byteBufUtils, objectStr));
                        break;
                    case JavaScript:
                        builder.append(StringUtils.format("buffer.writeDoubleArray({});", objectStr)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            }
            case "java.util.List<java.lang.String>":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}.writeStringList($1, (List){});", EnhanceUtils.byteBufUtils, objectStr));
                        break;
                    case JavaScript:
                        builder.append(StringUtils.format("buffer.writeStringArray({});", objectStr)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            default:
                // List<IPacket>
                if (listField.getListElementRegistration() instanceof ObjectProtocolField) {
                    var objectProtocolField = (ObjectProtocolField) listField.getListElementRegistration();
                    switch (language) {
                        case Enhance:
                            builder.append(StringUtils.format("{}.writePacketList($1, (List){}, {});", EnhanceUtils.byteBufUtils, objectStr, EnhanceUtils.getProtocolRegistrationFieldNameByProtocolId(objectProtocolField.getProtocolId())));
                            break;
                        case JavaScript:
                            builder.append(StringUtils.format("buffer.writePacketArray({}, {});", objectStr, objectProtocolField.getProtocolId())).append(LS);
                            break;
                        default:
                            flag = false;
                    }
                } else {
                    flag = false;
                }
        }

        return flag;
    }

    @Override
    public String readObject(StringBuilder builder, Field field, IFieldRegistration fieldRegistration, CodeLanguage language) {
        var listField = (ListField) fieldRegistration;
        var list = "list" + GenerateProtocolFile.index.getAndIncrement();
        var flag = true;

        switch (listField.getType().getTypeName()) {
            case "java.util.List<java.lang.Boolean>":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("List {} = {}.readBooleanList($1);", list, EnhanceUtils.byteBufUtils));
                        break;
                    case JavaScript:
                        builder.append(StringUtils.format("const {} = buffer.readBooleanArray();", list)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "java.util.List<java.lang.Byte>":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("List {} = {}.readByteList($1);", list, EnhanceUtils.byteBufUtils));
                        break;
                    case JavaScript:
                        builder.append(StringUtils.format("const {} = buffer.readByteArray();", list)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "java.util.List<java.lang.Short>":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("List {} = {}.readShortList($1);", list, EnhanceUtils.byteBufUtils));
                        break;
                    case JavaScript:
                        builder.append(StringUtils.format("const {} = buffer.readShortArray();", list)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "java.util.List<java.lang.Integer>":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("List {} = {}.readIntList($1);", list, EnhanceUtils.byteBufUtils));
                        break;
                    case JavaScript:
                        builder.append(StringUtils.format("const {} = buffer.readIntArray();", list)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "java.util.List<java.lang.Long>":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("List {} = {}.readLongList($1);", list, EnhanceUtils.byteBufUtils));
                        break;
                    case JavaScript:
                        builder.append(StringUtils.format("const {} = buffer.readLongArray();", list)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "java.util.List<java.lang.Float>":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("List {} = {}.readFloatList($1);", list, EnhanceUtils.byteBufUtils));
                        break;
                    case JavaScript:
                        builder.append(StringUtils.format("const {} = buffer.readFloatArray();", list)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "java.util.List<java.lang.Double>":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("List {} = {}.readDoubleList($1);", list, EnhanceUtils.byteBufUtils));
                        break;
                    case JavaScript:
                        builder.append(StringUtils.format("const {} = buffer.readDoubleArray();", list)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "java.util.List<java.lang.String>":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("List {} = {}.readStringList($1);", list, EnhanceUtils.byteBufUtils));
                        break;
                    case JavaScript:
                        builder.append(StringUtils.format("const {} = buffer.readStringArray();", list)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            default:
                if (listField.getListElementRegistration() instanceof ObjectProtocolField) {
                    var objectProtocolField = (ObjectProtocolField) listField.getListElementRegistration();
                    switch (language) {
                        case Enhance:
                            builder.append(StringUtils.format("List {} = {}.readPacketList($1, {});", list, EnhanceUtils.byteBufUtils, EnhanceUtils.getProtocolRegistrationFieldNameByProtocolId(objectProtocolField.getProtocolId())));
                            break;
                        case JavaScript:
                            builder.append(StringUtils.format("const {} = buffer.readPacketArray({});", list, objectProtocolField.getProtocolId())).append(LS);
                            break;
                        default:
                            flag = false;
                    }
                } else {
                    flag = false;
                }
        }


        if (flag) {
            return list;
        } else {
            GenerateProtocolFile.index.getAndDecrement();
            return null;
        }
    }
}
