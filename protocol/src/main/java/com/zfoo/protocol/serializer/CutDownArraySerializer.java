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
import com.zfoo.protocol.registration.field.ArrayField;
import com.zfoo.protocol.registration.field.BaseField;
import com.zfoo.protocol.registration.field.IFieldRegistration;
import com.zfoo.protocol.registration.field.ObjectProtocolField;
import com.zfoo.protocol.serializer.enhance.EnhanceObjectProtocolSerializer;
import com.zfoo.protocol.util.StringUtils;

import java.lang.reflect.Field;

import static com.zfoo.protocol.util.FileUtils.LS;

/**
 * @author godotg
 */
public class CutDownArraySerializer implements ICutDownSerializer {

    private static final CutDownArraySerializer INSTANCE = new CutDownArraySerializer();

    public static CutDownArraySerializer getInstance() {
        return INSTANCE;
    }


    @Override
    public boolean writeObject(StringBuilder builder, String objectStr, Field field, IFieldRegistration fieldRegistration, CodeLanguage language) {
        var arrayField = (ArrayField) fieldRegistration;
        var arrayName = getArrayClassName(arrayField);
        var flag = true;

        // 直接在字节码里调用方法是为了减小生成字节码的体积，下面的代码去掉也不会有任何影响
        switch (arrayName) {
            case "boolean":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}.writeBooleanArray($1, {});", EnhanceUtils.byteBufUtils, objectStr));
                        break;
                    case GdScript:
                    case Python:
                        builder.append(StringUtils.format("buffer.writeBooleanArray({})", objectStr)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("buffer:writeBooleanArray({})", objectStr)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("buffer.WriteBooleanArray({});", objectStr)).append(LS);
                        break;
                    case Go:
                        builder.append(StringUtils.format("buffer.WriteBooleanArray({})", objectStr)).append(LS);
                        break;
                    case Cpp:
                    case JavaScript:
                    case TypeScript:
                        builder.append(StringUtils.format("buffer.writeBooleanArray({});", objectStr)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "Boolean":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}.writeBooleanBoxArray($1, {});", EnhanceUtils.byteBufUtils, objectStr));
                        break;
                    case GdScript:
                    case Python:
                        builder.append(StringUtils.format("buffer.writeBooleanArray({})", objectStr)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("buffer:writeBooleanArray({})", objectStr)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("buffer.WriteBooleanArray({});", objectStr)).append(LS);
                        break;
                    case Go:
                        builder.append(StringUtils.format("buffer.WriteBooleanArray({})", objectStr)).append(LS);
                        break;
                    case Cpp:
                    case JavaScript:
                    case TypeScript:
                        builder.append(StringUtils.format("buffer.writeBooleanArray({});", objectStr)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "byte":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}.writeByteArray($1, {});", EnhanceUtils.byteBufUtils, objectStr));
                        break;
                    case GdScript:
                    case Python:
                        builder.append(StringUtils.format("buffer.writeByteArray({})", objectStr)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("buffer:writeByteArray({})", objectStr)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("buffer.WriteByteArray({});", objectStr)).append(LS);
                        break;
                    case Go:
                        builder.append(StringUtils.format("buffer.WriteByteArray({})", objectStr)).append(LS);
                        break;
                    case Cpp:
                    case JavaScript:
                    case TypeScript:
                        builder.append(StringUtils.format("buffer.writeByteArray({});", objectStr)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "Byte":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}.writeByteBoxArray($1, {});", EnhanceUtils.byteBufUtils, objectStr));
                        break;
                    case GdScript:
                    case Python:
                        builder.append(StringUtils.format("buffer.writeByteArray({})", objectStr)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("buffer:writeByteArray({})", objectStr)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("buffer.WriteByteArray({});", objectStr)).append(LS);
                        break;
                    case Go:
                        builder.append(StringUtils.format("buffer.WriteByteArray({})", objectStr)).append(LS);
                        break;
                    case Cpp:
                    case JavaScript:
                    case TypeScript:
                        builder.append(StringUtils.format("buffer.writeByteArray({});", objectStr)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "short":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}.writeShortArray($1, {});", EnhanceUtils.byteBufUtils, objectStr));
                        break;
                    case GdScript:
                    case Python:
                        builder.append(StringUtils.format("buffer.writeShortArray({})", objectStr)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("buffer:writeShortArray({})", objectStr)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("buffer.WriteShortArray({});", objectStr)).append(LS);
                        break;
                    case Go:
                        builder.append(StringUtils.format("buffer.WriteShortArray({})", objectStr)).append(LS);
                        break;
                    case Cpp:
                    case JavaScript:
                    case TypeScript:
                        builder.append(StringUtils.format("buffer.writeShortArray({});", objectStr)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "Short":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}.writeShortBoxArray($1, {});", EnhanceUtils.byteBufUtils, objectStr));
                        break;
                    case GdScript:
                    case Python:
                        builder.append(StringUtils.format("buffer.writeShortArray({})", objectStr)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("buffer:writeShortArray({})", objectStr)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("buffer.WriteShortArray({});", objectStr)).append(LS);
                        break;
                    case Go:
                        builder.append(StringUtils.format("buffer.WriteShortArray({})", objectStr)).append(LS);
                        break;
                    case Cpp:
                    case JavaScript:
                    case TypeScript:
                        builder.append(StringUtils.format("buffer.writeShortArray({});", objectStr)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "int":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}.writeIntArray($1, {});", EnhanceUtils.byteBufUtils, objectStr));
                        break;
                    case GdScript:
                    case Python:
                        builder.append(StringUtils.format("buffer.writeIntArray({})", objectStr)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("buffer:writeIntArray({})", objectStr)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("buffer.WriteIntArray({});", objectStr)).append(LS);
                        break;
                    case Go:
                        builder.append(StringUtils.format("buffer.WriteIntArray({})", objectStr)).append(LS);
                        break;
                    case Cpp:
                    case JavaScript:
                    case TypeScript:
                        builder.append(StringUtils.format("buffer.writeIntArray({});", objectStr)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "Integer":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}.writeIntBoxArray($1, {});", EnhanceUtils.byteBufUtils, objectStr));
                        break;
                    case GdScript:
                    case Python:
                        builder.append(StringUtils.format("buffer.writeIntArray({})", objectStr)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("buffer:writeIntArray({})", objectStr)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("buffer.WriteIntArray({});", objectStr)).append(LS);
                        break;
                    case Go:
                        builder.append(StringUtils.format("buffer.WriteIntArray({})", objectStr)).append(LS);
                        break;
                    case Cpp:
                    case JavaScript:
                    case TypeScript:
                        builder.append(StringUtils.format("buffer.writeIntArray({});", objectStr)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "long":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}.writeLongArray($1, {});", EnhanceUtils.byteBufUtils, objectStr));
                        break;
                    case GdScript:
                    case Python:
                        builder.append(StringUtils.format("buffer.writeLongArray({})", objectStr)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("buffer:writeLongArray({})", objectStr)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("buffer.WriteLongArray({});", objectStr)).append(LS);
                        break;
                    case Go:
                        builder.append(StringUtils.format("buffer.WriteLongArray({})", objectStr)).append(LS);
                        break;
                    case Cpp:
                    case JavaScript:
                    case TypeScript:
                        builder.append(StringUtils.format("buffer.writeLongArray({});", objectStr)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "Long":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}.writeLongBoxArray($1, {});", EnhanceUtils.byteBufUtils, objectStr));
                        break;
                    case GdScript:
                    case Python:
                        builder.append(StringUtils.format("buffer.writeLongArray({})", objectStr)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("buffer:writeLongArray({})", objectStr)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("buffer.WriteLongArray({});", objectStr)).append(LS);
                        break;
                    case Go:
                        builder.append(StringUtils.format("buffer.WriteLongArray({})", objectStr)).append(LS);
                        break;
                    case Cpp:
                    case JavaScript:
                    case TypeScript:
                        builder.append(StringUtils.format("buffer.writeLongArray({});", objectStr)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "float":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}.writeFloatArray($1, {});", EnhanceUtils.byteBufUtils, objectStr));
                        break;
                    case GdScript:
                    case Python:
                        builder.append(StringUtils.format("buffer.writeFloatArray({})", objectStr)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("buffer:writeFloatArray({})", objectStr)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("buffer.WriteFloatArray({});", objectStr)).append(LS);
                        break;
                    case Go:
                        builder.append(StringUtils.format("buffer.WriteFloatArray({})", objectStr)).append(LS);
                        break;
                    case Cpp:
                    case JavaScript:
                    case TypeScript:
                        builder.append(StringUtils.format("buffer.writeFloatArray({});", objectStr)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "Float":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}.writeFloatBoxArray($1, {});", EnhanceUtils.byteBufUtils, objectStr));
                        break;
                    case GdScript:
                    case Python:
                        builder.append(StringUtils.format("buffer.writeFloatArray({})", objectStr)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("buffer:writeFloatArray({})", objectStr)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("buffer.WriteFloatArray({});", objectStr)).append(LS);
                        break;
                    case Go:
                        builder.append(StringUtils.format("buffer.WriteFloatArray({})", objectStr)).append(LS);
                        break;
                    case Cpp:
                    case JavaScript:
                    case TypeScript:
                        builder.append(StringUtils.format("buffer.writeFloatArray({});", objectStr)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "double":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}.writeDoubleArray($1, {});", EnhanceUtils.byteBufUtils, objectStr));
                        break;
                    case GdScript:
                    case Python:
                        builder.append(StringUtils.format("buffer.writeDoubleArray({})", objectStr)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("buffer:writeDoubleArray({})", objectStr)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("buffer.WriteDoubleArray({});", objectStr)).append(LS);
                        break;
                    case Go:
                        builder.append(StringUtils.format("buffer.WriteDoubleArray({})", objectStr)).append(LS);
                        break;
                    case Cpp:
                    case JavaScript:
                    case TypeScript:
                        builder.append(StringUtils.format("buffer.writeDoubleArray({});", objectStr)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "Double":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}.writeDoubleBoxArray($1, {});", EnhanceUtils.byteBufUtils, objectStr));
                        break;
                    case GdScript:
                    case Python:
                        builder.append(StringUtils.format("buffer.writeDoubleArray({})", objectStr)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("buffer:writeDoubleArray({})", objectStr)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("buffer.WriteDoubleArray({});", objectStr)).append(LS);
                        break;
                    case Go:
                        builder.append(StringUtils.format("buffer.WriteDoubleArray({})", objectStr)).append(LS);
                        break;
                    case Cpp:
                    case JavaScript:
                    case TypeScript:
                        builder.append(StringUtils.format("buffer.writeDoubleArray({});", objectStr)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "String":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}.writeStringArray($1, {});", EnhanceUtils.byteBufUtils, objectStr));
                        break;
                    case GdScript:
                    case Python:
                        builder.append(StringUtils.format("buffer.writeStringArray({})", objectStr)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("buffer:writeStringArray({})", objectStr)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("buffer.WriteStringArray({});", objectStr)).append(LS);
                        break;
                    case Go:
                        builder.append(StringUtils.format("buffer.WriteStringArray({})", objectStr)).append(LS);
                        break;
                    case Cpp:
                    case JavaScript:
                    case TypeScript:
                        builder.append(StringUtils.format("buffer.writeStringArray({});", objectStr)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "char":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}.writeCharArray($1, {});", EnhanceUtils.byteBufUtils, objectStr));
                        break;
                    case GdScript:
                    case Python:
                        builder.append(StringUtils.format("buffer.writeCharArray({})", objectStr)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("buffer:writeCharArray({})", objectStr)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("buffer.WriteCharArray({});", objectStr)).append(LS);
                        break;
                    case Go:
                        builder.append(StringUtils.format("buffer.WriteCharArray({})", objectStr)).append(LS);
                        break;
                    case Cpp:
                    case JavaScript:
                    case TypeScript:
                        builder.append(StringUtils.format("buffer.writeCharArray({});", objectStr)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "Character":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}.writeCharBoxArray($1, {});", EnhanceUtils.byteBufUtils, objectStr));
                        break;
                    case GdScript:
                    case Python:
                        builder.append(StringUtils.format("buffer.writeCharArray({})", objectStr)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("buffer:writeCharArray({})", objectStr)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("buffer.WriteCharArray({});", objectStr)).append(LS);
                        break;
                    case Go:
                        builder.append(StringUtils.format("buffer.WriteCharArray({})", objectStr)).append(LS);
                        break;
                    case Cpp:
                    case JavaScript:
                    case TypeScript:
                        builder.append(StringUtils.format("buffer.writeCharArray({});", objectStr)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            default:
                if (arrayField.getArrayElementRegistration() instanceof ObjectProtocolField) {
                    var protocolId = ((ObjectProtocolField) arrayField.getArrayElementRegistration()).getProtocolId();
                    switch (language) {
                        case Enhance:
                            builder.append(StringUtils.format("{}.writePacketArray($1, {}, {});", EnhanceUtils.byteBufUtils, objectStr, EnhanceUtils.getProtocolRegistrationFieldNameByProtocolId(protocolId)));
                            break;
                        case GdScript:
                        case Python:
                            builder.append(StringUtils.format("buffer.writePacketArray({}, {})", objectStr, protocolId)).append(LS);
                            break;
                        case Lua:
                            builder.append(StringUtils.format("buffer:writePacketArray({}, {})", objectStr, protocolId)).append(LS);
                            break;
                        case CSharp:
                            builder.append(StringUtils.format("buffer.WritePacketArray<{}>({}, {});", EnhanceObjectProtocolSerializer.getProtocolClassSimpleName(protocolId), objectStr, protocolId)).append(LS);
                            break;
                        case Cpp:
                            builder.append(StringUtils.format("buffer.writePacketArray<{}>({}, {});", EnhanceObjectProtocolSerializer.getProtocolClassSimpleName(protocolId), objectStr, protocolId)).append(LS);
                            break;
                        case JavaScript:
                        case TypeScript:
                            builder.append(StringUtils.format("buffer.writePacketArray({}, {});", objectStr, protocolId)).append(LS);
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
        var arrayField = (ArrayField) fieldRegistration;
        var arrayName = getArrayClassName(arrayField);

        var array = "array" + GenerateProtocolFile.index.getAndIncrement();

        var flag = true;
        switch (arrayName) {
            case "boolean":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}[] {} = {}.readBooleanArray($1);", arrayName, array, EnhanceUtils.byteBufUtils));
                        break;
                    case GdScript:
                        builder.append(StringUtils.format("var {} = buffer.readBooleanArray()", array)).append(LS);
                        break;
                    case Python:
                        builder.append(StringUtils.format("{} = buffer.readBooleanArray()", array)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("local {} = buffer:readBooleanArray()", array)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("var {} = buffer.ReadBooleanArray();", array)).append(LS);
                        break;
                    case Go:
                        builder.append(StringUtils.format("var {} = buffer.ReadBooleanArray()", array)).append(LS);
                        break;
                    case Cpp:
                        builder.append(StringUtils.format("auto {} = buffer.readBooleanArray();", array)).append(LS);
                        break;
                    case JavaScript:
                    case TypeScript:
                        builder.append(StringUtils.format("const {} = buffer.readBooleanArray();", array)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "Boolean":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}[] {} = {}.readBooleanBoxArray($1);", arrayName, array, EnhanceUtils.byteBufUtils));
                        break;
                    case GdScript:
                        builder.append(StringUtils.format("var {} = buffer.readBooleanArray()", array)).append(LS);
                        break;
                    case Python:
                        builder.append(StringUtils.format("{} = buffer.readBooleanArray()", array)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("local {} = buffer:readBooleanArray()", array)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("var {} = buffer.ReadBooleanArray();", array)).append(LS);
                        break;
                    case Go:
                        builder.append(StringUtils.format("var {} = buffer.ReadBooleanArray()", array)).append(LS);
                        break;
                    case Cpp:
                        builder.append(StringUtils.format("auto {} = buffer.readBooleanArray();", array)).append(LS);
                        break;
                    case JavaScript:
                    case TypeScript:
                        builder.append(StringUtils.format("const {} = buffer.readBooleanArray();", array)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "byte":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}[] {} = {}.readByteArray($1);", arrayName, array, EnhanceUtils.byteBufUtils));
                        break;
                    case GdScript:
                        builder.append(StringUtils.format("var {} = buffer.readByteArray()", array)).append(LS);
                        break;
                    case Python:
                        builder.append(StringUtils.format("{} = buffer.readByteArray()", array)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("local {} = buffer:readByteArray()", array)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("var {} = buffer.ReadByteArray();", array)).append(LS);
                        break;
                    case Go:
                        builder.append(StringUtils.format("var {} = buffer.ReadByteArray()", array)).append(LS);
                        break;
                    case Cpp:
                        builder.append(StringUtils.format("auto {} = buffer.readByteArray();", array)).append(LS);
                        break;
                    case JavaScript:
                    case TypeScript:
                        builder.append(StringUtils.format("const {} = buffer.readByteArray();", array)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "Byte":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}[] {} = {}.readByteBoxArray($1);", arrayName, array, EnhanceUtils.byteBufUtils));
                        break;
                    case GdScript:
                        builder.append(StringUtils.format("var {} = buffer.readByteArray()", array)).append(LS);
                        break;
                    case Python:
                        builder.append(StringUtils.format("{} = buffer.readByteArray()", array)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("local {} = buffer:readByteArray()", array)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("var {} = buffer.ReadByteArray();", array)).append(LS);
                        break;
                    case Go:
                        builder.append(StringUtils.format("var {} = buffer.ReadByteArray()", array)).append(LS);
                        break;
                    case Cpp:
                        builder.append(StringUtils.format("auto {} = buffer.readByteArray();", array)).append(LS);
                        break;
                    case JavaScript:
                    case TypeScript:
                        builder.append(StringUtils.format("const {} = buffer.readByteArray();", array)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "short":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}[] {} = {}.readShortArray($1);", arrayName, array, EnhanceUtils.byteBufUtils));
                        break;
                    case GdScript:
                        builder.append(StringUtils.format("var {} = buffer.readShortArray()", array)).append(LS);
                        break;
                    case Python:
                        builder.append(StringUtils.format("{} = buffer.readShortArray()", array)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("local {} = buffer:readShortArray()", array)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("var {} = buffer.ReadShortArray();", array)).append(LS);
                        break;
                    case Go:
                        builder.append(StringUtils.format("var {} = buffer.ReadShortArray()", array)).append(LS);
                        break;
                    case Cpp:
                        builder.append(StringUtils.format("auto {} = buffer.readShortArray();", array)).append(LS);
                        break;
                    case JavaScript:
                    case TypeScript:
                        builder.append(StringUtils.format("const {} = buffer.readShortArray();", array)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "Short":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}[] {} = {}.readShortBoxArray($1);", arrayName, array, EnhanceUtils.byteBufUtils));
                        break;
                    case GdScript:
                        builder.append(StringUtils.format("var {} = buffer.readShortArray()", array)).append(LS);
                        break;
                    case Python:
                        builder.append(StringUtils.format("{} = buffer.readShortArray()", array)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("local {} = buffer:readShortArray()", array)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("var {} = buffer.ReadShortArray();", array)).append(LS);
                        break;
                    case Go:
                        builder.append(StringUtils.format("var {} = buffer.ReadShortArray()", array)).append(LS);
                        break;
                    case Cpp:
                        builder.append(StringUtils.format("auto {} = buffer.readShortArray();", array)).append(LS);
                        break;
                    case JavaScript:
                    case TypeScript:
                        builder.append(StringUtils.format("const {} = buffer.readShortArray();", array)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "int":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}[] {} = {}.readIntArray($1);", arrayName, array, EnhanceUtils.byteBufUtils));
                        break;
                    case GdScript:
                        builder.append(StringUtils.format("var {} = buffer.readIntArray()", array)).append(LS);
                        break;
                    case Python:
                        builder.append(StringUtils.format("{} = buffer.readIntArray()", array)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("local {} = buffer:readIntArray()", array)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("var {} = buffer.ReadIntArray();", array)).append(LS);
                        break;
                    case Go:
                        builder.append(StringUtils.format("var {} = buffer.ReadIntArray()", array)).append(LS);
                        break;
                    case Cpp:
                        builder.append(StringUtils.format("auto {} = buffer.readIntArray();", array)).append(LS);
                        break;
                    case JavaScript:
                    case TypeScript:
                        builder.append(StringUtils.format("const {} = buffer.readIntArray();", array)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "Integer":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}[] {} = {}.readIntBoxArray($1);", arrayName, array, EnhanceUtils.byteBufUtils));
                        break;
                    case GdScript:
                        builder.append(StringUtils.format("var {} = buffer.readIntArray()", array)).append(LS);
                        break;
                    case Python:
                        builder.append(StringUtils.format("{} = buffer.readIntArray()", array)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("local {} = buffer:readIntArray()", array)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("var {} = buffer.ReadIntArray();", array)).append(LS);
                        break;
                    case Go:
                        builder.append(StringUtils.format("var {} = buffer.ReadIntArray()", array)).append(LS);
                        break;
                    case Cpp:
                        builder.append(StringUtils.format("auto {} = buffer.readIntArray();", array)).append(LS);
                        break;
                    case JavaScript:
                    case TypeScript:
                        builder.append(StringUtils.format("const {} = buffer.readIntArray();", array)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "long":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}[] {} = {}.readLongArray($1);", arrayName, array, EnhanceUtils.byteBufUtils));
                        break;
                    case GdScript:
                        builder.append(StringUtils.format("var {} = buffer.readLongArray()", array)).append(LS);
                        break;
                    case Python:
                        builder.append(StringUtils.format("{} = buffer.readLongArray()", array)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("local {} = buffer:readLongArray()", array)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("var {} = buffer.ReadLongArray();", array)).append(LS);
                        break;
                    case Go:
                        builder.append(StringUtils.format("var {} = buffer.ReadLongArray()", array)).append(LS);
                        break;
                    case Cpp:
                        builder.append(StringUtils.format("auto {} = buffer.readLongArray();", array)).append(LS);
                        break;
                    case JavaScript:
                    case TypeScript:
                        builder.append(StringUtils.format("const {} = buffer.readLongArray();", array)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "Long":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}[] {} = {}.readLongBoxArray($1);", arrayName, array, EnhanceUtils.byteBufUtils));
                        break;
                    case GdScript:
                        builder.append(StringUtils.format("var {} = buffer.readLongArray()", array)).append(LS);
                        break;
                    case Python:
                        builder.append(StringUtils.format("{} = buffer.readLongArray()", array)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("local {} = buffer:readLongArray()", array)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("var {} = buffer.ReadLongArray();", array)).append(LS);
                        break;
                    case Go:
                        builder.append(StringUtils.format("var {} = buffer.ReadLongArray()", array)).append(LS);
                        break;
                    case Cpp:
                        builder.append(StringUtils.format("auto {} = buffer.readLongArray();", array)).append(LS);
                        break;
                    case JavaScript:
                    case TypeScript:
                        builder.append(StringUtils.format("const {} = buffer.readLongArray();", array)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "float":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}[] {} = {}.readFloatArray($1);", arrayName, array, EnhanceUtils.byteBufUtils));
                        break;
                    case GdScript:
                        builder.append(StringUtils.format("var {} = buffer.readFloatArray()", array)).append(LS);
                        break;
                    case Python:
                        builder.append(StringUtils.format("{} = buffer.readFloatArray()", array)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("local {} = buffer:readFloatArray()", array)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("var {} = buffer.ReadFloatArray();", array)).append(LS);
                        break;
                    case Go:
                        builder.append(StringUtils.format("var {} = buffer.ReadFloatArray()", array)).append(LS);
                        break;
                    case Cpp:
                        builder.append(StringUtils.format("auto {} = buffer.readFloatArray();", array)).append(LS);
                        break;
                    case JavaScript:
                    case TypeScript:
                        builder.append(StringUtils.format("const {} = buffer.readFloatArray();", array)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "Float":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}[] {} = {}.readFloatBoxArray($1);", arrayName, array, EnhanceUtils.byteBufUtils));
                        break;
                    case GdScript:
                        builder.append(StringUtils.format("var {} = buffer.readFloatArray()", array)).append(LS);
                        break;
                    case Python:
                        builder.append(StringUtils.format("{} = buffer.readFloatArray()", array)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("local {} = buffer:readFloatArray()", array)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("var {} = buffer.ReadFloatArray();", array)).append(LS);
                        break;
                    case Go:
                        builder.append(StringUtils.format("var {} = buffer.ReadFloatArray()", array)).append(LS);
                        break;
                    case Cpp:
                        builder.append(StringUtils.format("auto {} = buffer.readFloatArray();", array)).append(LS);
                        break;
                    case JavaScript:
                    case TypeScript:
                        builder.append(StringUtils.format("const {} = buffer.readFloatArray();", array)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "double":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}[] {} = {}.readDoubleArray($1);", arrayName, array, EnhanceUtils.byteBufUtils));
                        break;
                    case GdScript:
                        builder.append(StringUtils.format("var {} = buffer.readDoubleArray()", array)).append(LS);
                        break;
                    case Python:
                        builder.append(StringUtils.format("{} = buffer.readDoubleArray()", array)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("local {} = buffer:readDoubleArray()", array)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("var {} = buffer.ReadDoubleArray();", array)).append(LS);
                        break;
                    case Go:
                        builder.append(StringUtils.format("var {} = buffer.ReadDoubleArray()", array)).append(LS);
                        break;
                    case Cpp:
                        builder.append(StringUtils.format("auto {} = buffer.readDoubleArray();", array)).append(LS);
                        break;
                    case JavaScript:
                    case TypeScript:
                        builder.append(StringUtils.format("const {} = buffer.readDoubleArray();", array)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "Double":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}[] {} = {}.readDoubleBoxArray($1);", arrayName, array, EnhanceUtils.byteBufUtils));
                        break;
                    case GdScript:
                        builder.append(StringUtils.format("var {} = buffer.readDoubleArray()", array)).append(LS);
                        break;
                    case Python:
                        builder.append(StringUtils.format("{} = buffer.readDoubleArray()", array)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("local {} = buffer:readDoubleArray()", array)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("var {} = buffer.ReadDoubleArray();", array)).append(LS);
                        break;
                    case Go:
                        builder.append(StringUtils.format("var {} = buffer.ReadDoubleArray()", array)).append(LS);
                        break;
                    case Cpp:
                        builder.append(StringUtils.format("auto {} = buffer.readDoubleArray();", array)).append(LS);
                        break;
                    case JavaScript:
                    case TypeScript:
                        builder.append(StringUtils.format("const {} = buffer.readDoubleArray();", array)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "String":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}[] {} = {}.readStringArray($1);", arrayName, array, EnhanceUtils.byteBufUtils));
                        break;
                    case GdScript:
                        builder.append(StringUtils.format("var {} = buffer.readStringArray()", array)).append(LS);
                        break;
                    case Python:
                        builder.append(StringUtils.format("{} = buffer.readStringArray()", array)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("local {} = buffer:readStringArray()", array)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("var {} = buffer.ReadStringArray();", array)).append(LS);
                        break;
                    case Go:
                        builder.append(StringUtils.format("var {} = buffer.ReadStringArray()", array)).append(LS);
                        break;
                    case Cpp:
                        builder.append(StringUtils.format("auto {} = buffer.readStringArray();", array)).append(LS);
                        break;
                    case JavaScript:
                    case TypeScript:
                        builder.append(StringUtils.format("const {} = buffer.readStringArray();", array)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "char":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}[] {} = {}.readCharArray($1);", arrayName, array, EnhanceUtils.byteBufUtils));
                        break;
                    case GdScript:
                        builder.append(StringUtils.format("var {} = buffer.readCharArray()", array)).append(LS);
                        break;
                    case Python:
                        builder.append(StringUtils.format("{} = buffer.readCharArray()", array)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("local {} = buffer:readCharArray()", array)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("var {} = buffer.ReadCharArray();", array)).append(LS);
                        break;
                    case Go:
                        builder.append(StringUtils.format("var {} = buffer.ReadCharArray()", array)).append(LS);
                        break;
                    case Cpp:
                        builder.append(StringUtils.format("auto {} = buffer.readCharArray();", array)).append(LS);
                        break;
                    case JavaScript:
                    case TypeScript:
                        builder.append(StringUtils.format("const {} = buffer.readCharArray();", array)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "Character":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}[] {} = {}.readCharBoxArray($1);", arrayName, array, EnhanceUtils.byteBufUtils));
                        break;
                    case GdScript:
                        builder.append(StringUtils.format("var {} = buffer.readCharArray()", array)).append(LS);
                        break;
                    case Python:
                        builder.append(StringUtils.format("{} = buffer.readCharArray()", array)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("local {} = buffer:readCharArray()", array)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("var {} = buffer.ReadCharArray();", array)).append(LS);
                        break;
                    case Go:
                        builder.append(StringUtils.format("var {} = buffer.ReadCharArray()", array)).append(LS);
                        break;
                    case Cpp:
                        builder.append(StringUtils.format("auto {} = buffer.readCharArray();", array)).append(LS);
                        break;
                    case JavaScript:
                    case TypeScript:
                        builder.append(StringUtils.format("const {} = buffer.readCharArray();", array)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            default:
                if (arrayField.getArrayElementRegistration() instanceof ObjectProtocolField) {
                    var protocolId = ((ObjectProtocolField) arrayField.getArrayElementRegistration()).getProtocolId();
                    switch (language) {
                        // Java不支持泛型的数组初始化，这里不做任何操作
                        case GdScript:
                            builder.append(StringUtils.format("var {} = buffer.readPacketArray({})", array, protocolId)).append(LS);
                            break;
                        case Python:
                            builder.append(StringUtils.format("{} = buffer.readPacketArray({})", array, protocolId)).append(LS);
                            break;
                        case Lua:
                            builder.append(StringUtils.format("local {} = buffer:readPacketArray({})", array, protocolId)).append(LS);
                            break;
                        case CSharp:
                            builder.append(StringUtils.format("var {} = buffer.ReadPacketArray<{}>({});", array, EnhanceObjectProtocolSerializer.getProtocolClassSimpleName(protocolId), protocolId)).append(LS);
                            break;
                        case Cpp:
                            builder.append(StringUtils.format("auto {} = buffer.readPacketArray<{}>({});", array, EnhanceObjectProtocolSerializer.getProtocolClassSimpleName(protocolId), protocolId)).append(LS);
                            break;
                        case JavaScript:
                        case TypeScript:
                            builder.append(StringUtils.format("const {} = buffer.readPacketArray({});", array, protocolId)).append(LS);
                            break;
                        case Go:
                        case Protobuf:
                        case Enhance:
                        default:
                            flag = false;
                    }
                } else {
                    flag = false;
                }
        }

        if (flag) {
            return array;
        } else {
            GenerateProtocolFile.index.getAndDecrement();
            return null;
        }
    }

    public String getArrayClassName(ArrayField arrayField) {
        if (arrayField.getArrayElementRegistration() instanceof BaseField) {
            return arrayField.getType().getSimpleName();
        } else {
            return arrayField.getType().getCanonicalName();
        }
    }

}
