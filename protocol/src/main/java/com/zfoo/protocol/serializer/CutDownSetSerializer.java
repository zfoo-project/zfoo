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
import com.zfoo.protocol.registration.field.ObjectProtocolField;
import com.zfoo.protocol.registration.field.SetField;
import com.zfoo.protocol.serializer.enhance.EnhanceObjectProtocolSerializer;
import com.zfoo.protocol.util.StringUtils;

import java.lang.reflect.Field;

import static com.zfoo.protocol.util.FileUtils.LS;

/**
 * @author jaysunxiao
 * @version 3.0
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

        // 直接在字节码里调用方法是为了减小生成字节码的体积，下面的代码去掉也不会有任何影响
        switch (setField.getType().getTypeName()) {
            case "java.util.Set<java.lang.Boolean>":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}.writeBooleanSet($1, (Set){});", EnhanceUtils.byteBufUtils, objectStr));
                        break;
                    case JavaScript:
                        builder.append(StringUtils.format("buffer.writeBooleanArray({});", objectStr)).append(LS);
                        break;
                    case GdScript:
                        builder.append(StringUtils.format("buffer.writeBooleanArray({})", objectStr)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("buffer.WriteBooleanSet({});", objectStr)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "java.util.Set<java.lang.Byte>":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}.writeByteSet($1, (Set){});", EnhanceUtils.byteBufUtils, objectStr));
                        break;
                    case JavaScript:
                        builder.append(StringUtils.format("buffer.writeByteArray({});", objectStr)).append(LS);
                        break;
                    case GdScript:
                        builder.append(StringUtils.format("buffer.writeByteArray({})", objectStr)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("buffer.WriteByteSet({});", objectStr)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "java.util.Set<java.lang.Short>":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}.writeShortSet($1, (Set){});", EnhanceUtils.byteBufUtils, objectStr));
                        break;
                    case JavaScript:
                        builder.append(StringUtils.format("buffer.writeShortArray({});", objectStr)).append(LS);
                        break;
                    case GdScript:
                        builder.append(StringUtils.format("buffer.writeShortArray({})", objectStr)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("buffer.WriteShortSet({});", objectStr)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "java.util.Set<java.lang.Integer>":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}.writeIntSet($1, (Set){});", EnhanceUtils.byteBufUtils, objectStr));
                        break;
                    case JavaScript:
                        builder.append(StringUtils.format("buffer.writeIntArray({});", objectStr)).append(LS);
                        break;
                    case GdScript:
                        builder.append(StringUtils.format("buffer.writeIntArray({})", objectStr)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("buffer.WriteIntSet({});", objectStr)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "java.util.Set<java.lang.Long>":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}.writeLongSet($1, (Set){});", EnhanceUtils.byteBufUtils, objectStr));
                        break;
                    case JavaScript:
                        builder.append(StringUtils.format("buffer.writeLongArray({});", objectStr)).append(LS);
                        break;
                    case GdScript:
                        builder.append(StringUtils.format("buffer.writeLongArray({})", objectStr)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("buffer.WriteLongSet({});", objectStr)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "java.util.Set<java.lang.Float>":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}.writeFloatSet($1, (Set){});", EnhanceUtils.byteBufUtils, objectStr));
                        break;
                    case JavaScript:
                        builder.append(StringUtils.format("buffer.writeFloatArray({});", objectStr)).append(LS);
                        break;
                    case GdScript:
                        builder.append(StringUtils.format("buffer.writeFloatArray({})", objectStr)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("buffer.WriteFloatSet({});", objectStr)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "java.util.Set<java.lang.Double>":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}.writeDoubleSet($1, (Set){});", EnhanceUtils.byteBufUtils, objectStr));
                        break;
                    case JavaScript:
                        builder.append(StringUtils.format("buffer.writeDoubleArray({});", objectStr)).append(LS);
                        break;
                    case GdScript:
                        builder.append(StringUtils.format("buffer.writeDoubleArray({})", objectStr)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("buffer.WriteDoubleSet({});", objectStr)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "java.util.Set<java.lang.String>":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}.writeStringSet($1, (Set){});", EnhanceUtils.byteBufUtils, objectStr));
                        break;
                    case JavaScript:
                        builder.append(StringUtils.format("buffer.writeStringArray({});", objectStr)).append(LS);
                        break;
                    case GdScript:
                        builder.append(StringUtils.format("buffer.writeStringArray({})", objectStr)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("buffer.WriteStringSet({});", objectStr)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            default:
                // Set<IPacket>
                if (setField.getSetElementRegistration() instanceof ObjectProtocolField) {
                    var objectProtocolField = (ObjectProtocolField) setField.getSetElementRegistration();
                    switch (language) {
                        case Enhance:
                            builder.append(StringUtils.format("{}.writePacketSet($1, (Set){}, {});", EnhanceUtils.byteBufUtils, objectStr, EnhanceUtils.getProtocolRegistrationFieldNameByProtocolId(objectProtocolField.getProtocolId())));
                            break;
                        case JavaScript:
                            builder.append(StringUtils.format("buffer.writePacketArray({}, {});", objectStr, objectProtocolField.getProtocolId())).append(LS);
                            break;
                        case GdScript:
                            builder.append(StringUtils.format("buffer.writePacketArray({}, {})", objectStr, objectProtocolField.getProtocolId())).append(LS);
                            break;
                        case CSharp:
                            builder.append(StringUtils.format("buffer.WritePacketSet<>({}, {});", objectStr, objectProtocolField.getProtocolId())).append(LS);
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
        var setField = (SetField) fieldRegistration;
        var set = "set" + GenerateProtocolFile.index.getAndIncrement();
        var flag = true;

        switch (setField.getType().getTypeName()) {
            case "java.util.Set<java.lang.Boolean>":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("Set {} = {}.readBooleanSet($1);", set, EnhanceUtils.byteBufUtils));
                        break;
                    case JavaScript:
                        builder.append(StringUtils.format("const {} = buffer.readBooleanArray();", set)).append(LS);
                        break;
                    case GdScript:
                        builder.append(StringUtils.format("var {} = buffer.readBooleanArray()", set)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("var {} = buffer.ReadBooleanSet();", set)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "java.util.Set<java.lang.Byte>":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("Set {} = {}.readByteSet($1);", set, EnhanceUtils.byteBufUtils));
                        break;
                    case JavaScript:
                        builder.append(StringUtils.format("const {} = buffer.readByteArray();", set)).append(LS);
                        break;
                    case GdScript:
                        builder.append(StringUtils.format("var {} = buffer.readByteArray()", set)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("var {} = buffer.ReadByteSet();", set)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "java.util.Set<java.lang.Short>":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("Set {} = {}.readShortSet($1);", set, EnhanceUtils.byteBufUtils));
                        break;
                    case JavaScript:
                        builder.append(StringUtils.format("const {} = buffer.readShortArray();", set)).append(LS);
                        break;
                    case GdScript:
                        builder.append(StringUtils.format("var {} = buffer.readShortArray()", set)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("var {} = buffer.ReadShortSet();", set)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "java.util.Set<java.lang.Integer>":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("Set {} = {}.readIntSet($1);", set, EnhanceUtils.byteBufUtils));
                        break;
                    case JavaScript:
                        builder.append(StringUtils.format("const {} = buffer.readIntArray();", set)).append(LS);
                        break;
                    case GdScript:
                        builder.append(StringUtils.format("var {} = buffer.readIntArray()", set)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("var {} = buffer.ReadIntSet();", set)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "java.util.Set<java.lang.Long>":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("Set {} = {}.readLongSet($1);", set, EnhanceUtils.byteBufUtils));
                        break;
                    case JavaScript:
                        builder.append(StringUtils.format("const {} = buffer.readLongArray();", set)).append(LS);
                        break;
                    case GdScript:
                        builder.append(StringUtils.format("var {} = buffer.readLongArray()", set)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("var {} = buffer.ReadLongSet();", set)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "java.util.Set<java.lang.Float>":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("Set {} = {}.readFloatSet($1);", set, EnhanceUtils.byteBufUtils));
                        break;
                    case JavaScript:
                        builder.append(StringUtils.format("const {} = buffer.readFloatArray();", set)).append(LS);
                        break;
                    case GdScript:
                        builder.append(StringUtils.format("var {} = buffer.readFloatArray()", set)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("var {} = buffer.ReadFloatSet();", set)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "java.util.Set<java.lang.Double>":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("Set {} = {}.readDoubleSet($1);", set, EnhanceUtils.byteBufUtils));
                        break;
                    case JavaScript:
                        builder.append(StringUtils.format("const {} = buffer.readDoubleArray();", set)).append(LS);
                        break;
                    case GdScript:
                        builder.append(StringUtils.format("var {} = buffer.readDoubleArray()", set)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("var {} = buffer.ReadDoubleSet();", set)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "java.util.Set<java.lang.String>":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("Set {} = {}.readStringSet($1);", set, EnhanceUtils.byteBufUtils));
                        break;
                    case JavaScript:
                        builder.append(StringUtils.format("const {} = buffer.readStringArray();", set)).append(LS);
                        break;
                    case GdScript:
                        builder.append(StringUtils.format("var {} = buffer.readStringArray()", set)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("var {} = buffer.ReadStringSet();", set)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            default:
                if (setField.getSetElementRegistration() instanceof ObjectProtocolField) {
                    var objectProtocolField = (ObjectProtocolField) setField.getSetElementRegistration();
                    switch (language) {
                        case Enhance:
                            builder.append(StringUtils.format("Set {} = {}.readPacketSet($1, {});", set, EnhanceUtils.byteBufUtils, EnhanceUtils.getProtocolRegistrationFieldNameByProtocolId(objectProtocolField.getProtocolId())));
                            break;
                        case JavaScript:
                            builder.append(StringUtils.format("const {} = buffer.readPacketArray({});", set, objectProtocolField.getProtocolId())).append(LS);
                            break;
                        case GdScript:
                            builder.append(StringUtils.format("var {} = buffer.readPacketArray({})", set, objectProtocolField.getProtocolId())).append(LS);
                            break;
                        case CSharp:
                            builder.append(StringUtils.format("var {} = buffer.ReadPacketSet<{}>({});", set, EnhanceObjectProtocolSerializer.getProtocolClassSimpleName(objectProtocolField.getProtocolId()), objectProtocolField.getProtocolId())).append(LS);
                            break;
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

}
