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
import com.zfoo.protocol.registration.field.IFieldRegistration;
import com.zfoo.protocol.registration.field.ObjectProtocolField;
import com.zfoo.protocol.util.StringUtils;

import java.lang.reflect.Field;

import static com.zfoo.protocol.util.FileUtils.LS;

/**
 * @author jaysunxiao
 * @version 3.0
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
                    case JavaScript:
                        builder.append(StringUtils.format("byteBuffer.writeBooleanArray({});", objectStr)).append(LS);
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
                    case JavaScript:
                        builder.append(StringUtils.format("byteBuffer.writeBooleanArray({});", objectStr)).append(LS);
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
                    case JavaScript:
                        builder.append(StringUtils.format("byteBuffer.writeByteArray({});", objectStr)).append(LS);
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
                    case JavaScript:
                        builder.append(StringUtils.format("byteBuffer.writeByteArray({});", objectStr)).append(LS);
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
                    case JavaScript:
                        builder.append(StringUtils.format("byteBuffer.writeShortArray({});", objectStr)).append(LS);
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
                    case JavaScript:
                        builder.append(StringUtils.format("byteBuffer.writeShortArray({});", objectStr)).append(LS);
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
                    case JavaScript:
                        builder.append(StringUtils.format("byteBuffer.writeIntArray({});", objectStr)).append(LS);
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
                    case JavaScript:
                        builder.append(StringUtils.format("byteBuffer.writeIntArray({});", objectStr)).append(LS);
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
                    case JavaScript:
                        builder.append(StringUtils.format("byteBuffer.writeLongArray({});", objectStr)).append(LS);
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
                    case JavaScript:
                        builder.append(StringUtils.format("byteBuffer.writeLongArray({});", objectStr)).append(LS);
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
                    case JavaScript:
                        builder.append(StringUtils.format("byteBuffer.writeFloatArray({});", objectStr)).append(LS);
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
                    case JavaScript:
                        builder.append(StringUtils.format("byteBuffer.writeFloatArray({});", objectStr)).append(LS);
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
                    case JavaScript:
                        builder.append(StringUtils.format("byteBuffer.writeDoubleArray({});", objectStr)).append(LS);
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
                    case JavaScript:
                        builder.append(StringUtils.format("byteBuffer.writeDoubleArray({});", objectStr)).append(LS);
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
                    case JavaScript:
                        builder.append(StringUtils.format("byteBuffer.writeStringArray({});", objectStr)).append(LS);
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
                    case JavaScript:
                        builder.append(StringUtils.format("byteBuffer.writeCharArray({});", objectStr)).append(LS);
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
                    case JavaScript:
                        builder.append(StringUtils.format("byteBuffer.writeCharArray({});", objectStr)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            default:
                if (arrayField.getArrayElementRegistration() instanceof ObjectProtocolField) {
                    var objectProtocolField = (ObjectProtocolField) arrayField.getArrayElementRegistration();
                    switch (language) {
                        case Enhance:
                            builder.append(StringUtils.format("{}.writePacketArray($1, {}, {});", EnhanceUtils.byteBufUtils, objectStr, EnhanceUtils.getProtocolRegistrationFieldNameByProtocolId(objectProtocolField.getProtocolId())));
                            break;
                        case JavaScript:
                            builder.append(StringUtils.format("byteBuffer.writePacketArray({}, {});", objectStr, objectProtocolField.getProtocolId())).append(LS);
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
                    case JavaScript:
                        builder.append(StringUtils.format("const {} = byteBuffer.readBooleanArray();", array)).append(LS);
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
                    case JavaScript:
                        builder.append(StringUtils.format("const {} = byteBuffer.readBooleanArray();", array)).append(LS);
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
                    case JavaScript:
                        builder.append(StringUtils.format("const {} = byteBuffer.readByteArray();", array)).append(LS);
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
                    case JavaScript:
                        builder.append(StringUtils.format("const {} = byteBuffer.readByteArray();", array)).append(LS);
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
                    case JavaScript:
                        builder.append(StringUtils.format("const {} = byteBuffer.readShortArray();", array)).append(LS);
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
                    case JavaScript:
                        builder.append(StringUtils.format("const {} = byteBuffer.readShortArray();", array)).append(LS);
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
                    case JavaScript:
                        builder.append(StringUtils.format("const {} = byteBuffer.readIntArray();", array)).append(LS);
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
                    case JavaScript:
                        builder.append(StringUtils.format("const {} = byteBuffer.readIntArray();", array)).append(LS);
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
                    case JavaScript:
                        builder.append(StringUtils.format("const {} = byteBuffer.readLongArray();", array)).append(LS);
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
                    case JavaScript:
                        builder.append(StringUtils.format("const {} = byteBuffer.readLongArray();", array)).append(LS);
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
                    case JavaScript:
                        builder.append(StringUtils.format("const {} = byteBuffer.readFloatArray();", array)).append(LS);
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
                    case JavaScript:
                        builder.append(StringUtils.format("const {} = byteBuffer.readFloatArray();", array)).append(LS);
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
                    case JavaScript:
                        builder.append(StringUtils.format("const {} = byteBuffer.readDoubleArray();", array)).append(LS);
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
                    case JavaScript:
                        builder.append(StringUtils.format("const {} = byteBuffer.readDoubleArray();", array)).append(LS);
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
                    case JavaScript:
                        builder.append(StringUtils.format("const {} = byteBuffer.readStringArray();", array)).append(LS);
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
                    case JavaScript:
                        builder.append(StringUtils.format("const {} = byteBuffer.readCharArray();", array)).append(LS);
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
                    case JavaScript:
                        builder.append(StringUtils.format("const {} = byteBuffer.readCharArray();", array)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            default:
                if (arrayField.getArrayElementRegistration() instanceof ObjectProtocolField) {
                    var objectProtocolField = (ObjectProtocolField) arrayField.getArrayElementRegistration();
                    switch (language) {
                        // Java不支持泛型的数组初始化，这边就不做任何操作
                        case JavaScript:
                            builder.append(StringUtils.format("const {} = byteBuffer.readPacketArray({});", array, objectProtocolField.getProtocolId())).append(LS);
                            break;
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
        // 去掉包装类型的前缀java.lang
        return arrayField.getField().getType().getComponentType().getCanonicalName().replaceFirst("java.lang.", StringUtils.EMPTY);
    }

}
