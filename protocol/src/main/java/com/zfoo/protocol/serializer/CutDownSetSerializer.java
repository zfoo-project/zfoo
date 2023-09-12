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
import com.zfoo.protocol.registration.field.ObjectProtocolField;
import com.zfoo.protocol.registration.field.SetField;
import com.zfoo.protocol.serializer.enhance.EnhanceObjectProtocolSerializer;
import com.zfoo.protocol.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

import static com.zfoo.protocol.util.FileUtils.LS;

/**
 * @author godotg
 */
public class CutDownSetSerializer implements ICutDownSerializer {

    private static final CutDownSetSerializer INSTANCE = new CutDownSetSerializer();

    public static CutDownSetSerializer getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean writeObject(StringBuilder builder, String objectStr, Field field, IFieldRegistration fieldRegistration, CodeLanguage language) {
        var setField = (SetField) fieldRegistration;
        var flag = true;
        var setName = getSetClassName(setField);

        // 直接在字节码里调用方法是为了减小生成字节码的体积，下面的代码去掉也不会有任何影响
        switch (setName) {
            case "Boolean":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}.writeBooleanSet($1, (Set){});", EnhanceUtils.byteBufUtils, objectStr));
                        break;
                    case GdScript:
                        builder.append(StringUtils.format("buffer.writeBooleanArray({})", objectStr)).append(LS);
                        break;
                    case Python:
                        builder.append(StringUtils.format("buffer.writeBooleanSet({})", objectStr)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("buffer:writeBooleanArray({})", objectStr)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("buffer.WriteBooleanSet({});", objectStr)).append(LS);
                        break;
                    case Go:
                        builder.append(StringUtils.format("buffer.WriteBooleanArray({})", objectStr)).append(LS);
                        break;
                    case Cpp:
                    case JavaScript:
                    case TypeScript:
                        builder.append(StringUtils.format("buffer.writeBooleanSet({});", objectStr)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "Byte":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}.writeByteSet($1, (Set){});", EnhanceUtils.byteBufUtils, objectStr));
                        break;
                    case GdScript:
                        builder.append(StringUtils.format("buffer.writeByteArray({})", objectStr)).append(LS);
                        break;
                    case Python:
                        builder.append(StringUtils.format("buffer.writeByteSet({})", objectStr)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("buffer:writeByteArray({})", objectStr)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("buffer.WriteByteSet({});", objectStr)).append(LS);
                        break;
                    case Go:
                        builder.append(StringUtils.format("buffer.WriteByteArray({})", objectStr)).append(LS);
                        break;
                    case Cpp:
                    case JavaScript:
                    case TypeScript:
                        builder.append(StringUtils.format("buffer.writeByteSet({});", objectStr)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "Short":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}.writeShortSet($1, (Set){});", EnhanceUtils.byteBufUtils, objectStr));
                        break;
                    case GdScript:
                        builder.append(StringUtils.format("buffer.writeShortArray({})", objectStr)).append(LS);
                        break;
                    case Python:
                        builder.append(StringUtils.format("buffer.writeShortSet({})", objectStr)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("buffer:writeShortArray({})", objectStr)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("buffer.WriteShortSet({});", objectStr)).append(LS);
                        break;
                    case Go:
                        builder.append(StringUtils.format("buffer.WriteShortArray({})", objectStr)).append(LS);
                        break;
                    case Cpp:
                    case JavaScript:
                    case TypeScript:
                        builder.append(StringUtils.format("buffer.writeShortSet({});", objectStr)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "Integer":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}.writeIntSet($1, (Set){});", EnhanceUtils.byteBufUtils, objectStr));
                        break;
                    case GdScript:
                        builder.append(StringUtils.format("buffer.writeIntArray({})", objectStr)).append(LS);
                        break;
                    case Python:
                        builder.append(StringUtils.format("buffer.writeIntSet({})", objectStr)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("buffer:writeIntArray({})", objectStr)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("buffer.WriteIntSet({});", objectStr)).append(LS);
                        break;
                    case Go:
                        builder.append(StringUtils.format("buffer.WriteIntArray({})", objectStr)).append(LS);
                        break;
                    case Cpp:
                    case JavaScript:
                    case TypeScript:
                        builder.append(StringUtils.format("buffer.writeIntSet({});", objectStr)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "Long":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}.writeLongSet($1, (Set){});", EnhanceUtils.byteBufUtils, objectStr));
                        break;
                    case GdScript:
                        builder.append(StringUtils.format("buffer.writeLongArray({})", objectStr)).append(LS);
                        break;
                    case Python:
                        builder.append(StringUtils.format("buffer.writeLongSet({})", objectStr)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("buffer:writeLongArray({})", objectStr)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("buffer.WriteLongSet({});", objectStr)).append(LS);
                        break;
                    case Go:
                        builder.append(StringUtils.format("buffer.WriteLongArray({})", objectStr)).append(LS);
                        break;
                    case Cpp:
                    case JavaScript:
                    case TypeScript:
                        builder.append(StringUtils.format("buffer.writeLongSet({});", objectStr)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "Float":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}.writeFloatSet($1, (Set){});", EnhanceUtils.byteBufUtils, objectStr));
                        break;
                    case GdScript:
                        builder.append(StringUtils.format("buffer.writeFloatArray({})", objectStr)).append(LS);
                        break;
                    case Python:
                        builder.append(StringUtils.format("buffer.writeFloatSet({})", objectStr)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("buffer:writeFloatArray({})", objectStr)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("buffer.WriteFloatSet({});", objectStr)).append(LS);
                        break;
                    case Go:
                        builder.append(StringUtils.format("buffer.WriteFloatArray({})", objectStr)).append(LS);
                        break;
                    case Cpp:
                    case JavaScript:
                    case TypeScript:
                        builder.append(StringUtils.format("buffer.writeFloatSet({});", objectStr)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "Double":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}.writeDoubleSet($1, (Set){});", EnhanceUtils.byteBufUtils, objectStr));
                        break;
                    case GdScript:
                        builder.append(StringUtils.format("buffer.writeDoubleArray({})", objectStr)).append(LS);
                        break;
                    case Python:
                        builder.append(StringUtils.format("buffer.writeDoubleSet({})", objectStr)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("buffer:writeDoubleArray({})", objectStr)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("buffer.WriteDoubleSet({});", objectStr)).append(LS);
                        break;
                    case Go:
                        builder.append(StringUtils.format("buffer.WriteDoubleArray({})", objectStr)).append(LS);
                        break;
                    case Cpp:
                    case JavaScript:
                    case TypeScript:
                        builder.append(StringUtils.format("buffer.writeDoubleSet({});", objectStr)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "String":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}.writeStringSet($1, (Set){});", EnhanceUtils.byteBufUtils, objectStr));
                        break;
                    case GdScript:
                        builder.append(StringUtils.format("buffer.writeStringArray({})", objectStr)).append(LS);
                        break;
                    case Python:
                        builder.append(StringUtils.format("buffer.writeStringSet({})", objectStr)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("buffer:writeStringArray({})", objectStr)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("buffer.WriteStringSet({});", objectStr)).append(LS);
                        break;
                    case Go:
                        builder.append(StringUtils.format("buffer.WriteStringArray({})", objectStr)).append(LS);
                        break;
                    case Cpp:
                    case JavaScript:
                    case TypeScript:
                        builder.append(StringUtils.format("buffer.writeStringSet({});", objectStr)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            default:
                // Set<IProtocol>
                if (setField.getSetElementRegistration() instanceof ObjectProtocolField) {
                    var protocolId = ((ObjectProtocolField) setField.getSetElementRegistration()).getProtocolId();
                    switch (language) {
                        case Enhance:
                            builder.append(StringUtils.format("{}.writePacketSet($1, (Set){}, {});", EnhanceUtils.byteBufUtils, objectStr, EnhanceUtils.getProtocolRegistrationFieldNameByProtocolId(protocolId)));
                            break;
                        case GdScript:
                            builder.append(StringUtils.format("buffer.writePacketArray({}, {})", objectStr, protocolId)).append(LS);
                            break;
                        case Python:
                            builder.append(StringUtils.format("buffer.writePacketSet({}, {})", objectStr, protocolId)).append(LS);
                            break;
                        case Lua:
                            builder.append(StringUtils.format("buffer:writePacketArray({}, {})", objectStr, protocolId)).append(LS);
                            break;
                        case CSharp:
                            builder.append(StringUtils.format("buffer.WritePacketSet({}, {});", objectStr, protocolId)).append(LS);
                            break;
                        case Cpp:
                        case JavaScript:
                        case TypeScript:
                            builder.append(StringUtils.format("buffer.writePacketSet({}, {});", objectStr, protocolId)).append(LS);
                            break;
                        case Go:
                        case Protobuf:
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
        var setField = (SetField) fieldRegistration;
        var set = "set" + GenerateProtocolFile.index.getAndIncrement();
        var flag = true;
        var setName = getSetClassName(setField);

        switch (setName) {
            case "Boolean":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("Set {} = {}.readBooleanSet($1);", set, EnhanceUtils.byteBufUtils));
                        break;
                    case GdScript:
                        builder.append(StringUtils.format("var {} = buffer.readBooleanArray()", set)).append(LS);
                        break;
                    case Python:
                        builder.append(StringUtils.format("{} = buffer.readBooleanSet()", set)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("local {} = buffer:readBooleanArray()", set)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("var {} = buffer.ReadBooleanSet();", set)).append(LS);
                        break;
                    case Cpp:
                        builder.append(StringUtils.format("auto {} = buffer.readBooleanSet();", set)).append(LS);
                        break;
                    case Go:
                        builder.append(StringUtils.format("var {} = buffer.ReadBooleanArray()", set)).append(LS);
                        break;
                    case JavaScript:
                    case TypeScript:
                        builder.append(StringUtils.format("const {} = buffer.readBooleanSet();", set)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "Byte":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("Set {} = {}.readByteSet($1);", set, EnhanceUtils.byteBufUtils));
                        break;
                    case GdScript:
                        builder.append(StringUtils.format("var {} = buffer.readByteArray()", set)).append(LS);
                        break;
                    case Python:
                        builder.append(StringUtils.format("{} = buffer.readByteSet()", set)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("local {} = buffer:readByteArray()", set)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("var {} = buffer.ReadByteSet();", set)).append(LS);
                        break;
                    case Go:
                        builder.append(StringUtils.format("var {} = buffer.ReadByteArray()", set)).append(LS);
                        break;
                    case Cpp:
                        builder.append(StringUtils.format("auto {} = buffer.readByteSet();", set)).append(LS);
                        break;
                    case JavaScript:
                    case TypeScript:
                        builder.append(StringUtils.format("const {} = buffer.readByteSet();", set)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "Short":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("Set {} = {}.readShortSet($1);", set, EnhanceUtils.byteBufUtils));
                        break;
                    case GdScript:
                        builder.append(StringUtils.format("var {} = buffer.readShortArray()", set)).append(LS);
                        break;
                    case Python:
                        builder.append(StringUtils.format("{} = buffer.readShortSet()", set)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("local {} = buffer:readShortArray()", set)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("var {} = buffer.ReadShortSet();", set)).append(LS);
                        break;
                    case Go:
                        builder.append(StringUtils.format("var {} = buffer.ReadShortArray()", set)).append(LS);
                        break;
                    case Cpp:
                        builder.append(StringUtils.format("auto {} = buffer.readShortSet();", set)).append(LS);
                        break;
                    case JavaScript:
                    case TypeScript:
                        builder.append(StringUtils.format("const {} = buffer.readShortSet();", set)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "Integer":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("Set {} = {}.readIntSet($1);", set, EnhanceUtils.byteBufUtils));
                        break;
                    case GdScript:
                        builder.append(StringUtils.format("var {} = buffer.readIntArray()", set)).append(LS);
                        break;
                    case Python:
                        builder.append(StringUtils.format("{} = buffer.readIntSet()", set)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("local {} = buffer:readIntArray()", set)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("var {} = buffer.ReadIntSet();", set)).append(LS);
                        break;
                    case Go:
                        builder.append(StringUtils.format("var {} = buffer.ReadIntArray()", set)).append(LS);
                        break;
                    case Cpp:
                        builder.append(StringUtils.format("auto {} = buffer.readIntSet();", set)).append(LS);
                        break;
                    case JavaScript:
                    case TypeScript:
                        builder.append(StringUtils.format("const {} = buffer.readIntSet();", set)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "Long":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("Set {} = {}.readLongSet($1);", set, EnhanceUtils.byteBufUtils));
                        break;
                    case GdScript:
                        builder.append(StringUtils.format("var {} = buffer.readLongArray()", set)).append(LS);
                        break;
                    case Python:
                        builder.append(StringUtils.format("{} = buffer.readLongSet()", set)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("local {} = buffer:readLongArray()", set)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("var {} = buffer.ReadLongSet();", set)).append(LS);
                        break;
                    case Go:
                        builder.append(StringUtils.format("var {} = buffer.ReadLongArray()", set)).append(LS);
                        break;
                    case Cpp:
                        builder.append(StringUtils.format("auto {} = buffer.readLongSet();", set)).append(LS);
                        break;
                    case JavaScript:
                    case TypeScript:
                        builder.append(StringUtils.format("const {} = buffer.readLongSet();", set)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "Float":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("Set {} = {}.readFloatSet($1);", set, EnhanceUtils.byteBufUtils));
                        break;
                    case GdScript:
                        builder.append(StringUtils.format("var {} = buffer.readFloatArray()", set)).append(LS);
                        break;
                    case Python:
                        builder.append(StringUtils.format("{} = buffer.readFloatSet()", set)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("local {} = buffer:readFloatArray()", set)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("var {} = buffer.ReadFloatSet();", set)).append(LS);
                        break;
                    case Go:
                        builder.append(StringUtils.format("var {} = buffer.ReadFloatArray()", set)).append(LS);
                        break;
                    case Cpp:
                        builder.append(StringUtils.format("auto {} = buffer.readFloatSet();", set)).append(LS);
                        break;
                    case JavaScript:
                    case TypeScript:
                        builder.append(StringUtils.format("const {} = buffer.readFloatSet();", set)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "Double":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("Set {} = {}.readDoubleSet($1);", set, EnhanceUtils.byteBufUtils));
                        break;
                    case GdScript:
                        builder.append(StringUtils.format("var {} = buffer.readDoubleArray()", set)).append(LS);
                        break;
                    case Python:
                        builder.append(StringUtils.format("{} = buffer.readDoubleSet()", set)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("local {} = buffer:readDoubleArray()", set)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("var {} = buffer.ReadDoubleSet();", set)).append(LS);
                        break;
                    case Go:
                        builder.append(StringUtils.format("var {} = buffer.ReadDoubleArray()", set)).append(LS);
                        break;
                    case Cpp:
                        builder.append(StringUtils.format("auto {} = buffer.readDoubleSet();", set)).append(LS);
                        break;
                    case JavaScript:
                    case TypeScript:
                        builder.append(StringUtils.format("const {} = buffer.readDoubleSet();", set)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "String":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("Set {} = {}.readStringSet($1);", set, EnhanceUtils.byteBufUtils));
                        break;
                    case GdScript:
                        builder.append(StringUtils.format("var {} = buffer.readStringArray()", set)).append(LS);
                        break;
                    case Python:
                        builder.append(StringUtils.format("{} = buffer.readStringSet()", set)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("local {} = buffer:readStringArray()", set)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("var {} = buffer.ReadStringSet();", set)).append(LS);
                        break;
                    case Go:
                        builder.append(StringUtils.format("var {} = buffer.ReadStringArray()", set)).append(LS);
                        break;
                    case Cpp:
                        builder.append(StringUtils.format("auto {} = buffer.readStringSet();", set)).append(LS);
                        break;
                    case JavaScript:
                    case TypeScript:
                        builder.append(StringUtils.format("const {} = buffer.readStringSet();", set)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            default:
                if (setField.getSetElementRegistration() instanceof ObjectProtocolField) {
                    var protocolId = ((ObjectProtocolField) setField.getSetElementRegistration()).getProtocolId();
                    switch (language) {
                        case Enhance:
                            builder.append(StringUtils.format("Set {} = {}.readPacketSet($1, {});", set, EnhanceUtils.byteBufUtils, EnhanceUtils.getProtocolRegistrationFieldNameByProtocolId(protocolId)));
                            break;
                        case GdScript:
                            builder.append(StringUtils.format("var {} = buffer.readPacketArray({})", set, protocolId)).append(LS);
                            break;
                        case Python:
                            builder.append(StringUtils.format("{} = buffer.readPacketSet({})", set, protocolId)).append(LS);
                            break;
                        case Lua:
                            builder.append(StringUtils.format("local {} = buffer:readPacketArray({})", set, protocolId)).append(LS);
                            break;
                        case CSharp:
                            builder.append(StringUtils.format("var {} = buffer.ReadPacketSet<{}>({});", set, EnhanceObjectProtocolSerializer.getProtocolClassSimpleName(protocolId), protocolId)).append(LS);
                            break;
                        case Cpp:
                            builder.append(StringUtils.format("auto {} = buffer.readPacketSet<{}>({});", set, EnhanceObjectProtocolSerializer.getProtocolClassSimpleName(protocolId), protocolId)).append(LS);
                            break;
                        case JavaScript:
                        case TypeScript:
                            builder.append(StringUtils.format("const {} = buffer.readPacketSet({});", set, protocolId)).append(LS);
                            break;
                        case Go:
                        case Protobuf:
                        default:
                            flag = false;
                    }
                } else {
                    flag = false;
                }
        }


        if (flag) {
            return set;
        } else {
            GenerateProtocolFile.index.getAndDecrement();
            return null;
        }
    }

    public String getSetClassName(SetField setField) {
        if (setField.getSetElementRegistration() instanceof BaseField) {
            return ((Class<?>) ((ParameterizedType) setField.getType()).getActualTypeArguments()[0]).getSimpleName();
        } else {
            return setField.getType().getTypeName();
        }
    }

}
