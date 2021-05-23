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
import com.zfoo.protocol.registration.field.ArrayField;
import com.zfoo.protocol.registration.field.IFieldRegistration;
import com.zfoo.protocol.util.StringUtils;

import java.lang.reflect.Field;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public class EnhanceArraySerializer implements IEnhanceSerializer {

    @Override
    public void writeObject(StringBuilder builder, String objectStr, Field field, IFieldRegistration fieldRegistration) {
        var arrayField = (ArrayField) fieldRegistration;
        var arrayName = getArrayClassName(arrayField);

        // 直接在字节码里调用方法是为了减小生成字节码的体积，下面的代码去掉也不会有任何影响
        switch (arrayName) {
            case "boolean":
                builder.append(StringUtils.format("{}.writeBooleanArray($1, {});", EnhanceUtils.byteBufUtils, objectStr));
                return;
            case "Boolean":
                builder.append(StringUtils.format("{}.writeBooleanBoxArray($1, {});", EnhanceUtils.byteBufUtils, objectStr));
                return;
            case "byte":
                builder.append(StringUtils.format("{}.writeByteArray($1, {});", EnhanceUtils.byteBufUtils, objectStr));
                return;
            case "Byte":
                builder.append(StringUtils.format("{}.writeByteBoxArray($1, {});", EnhanceUtils.byteBufUtils, objectStr));
                return;
            case "short":
                builder.append(StringUtils.format("{}.writeShortArray($1, {});", EnhanceUtils.byteBufUtils, objectStr));
                return;
            case "Short":
                builder.append(StringUtils.format("{}.writeShortBoxArray($1, {});", EnhanceUtils.byteBufUtils, objectStr));
                return;
            case "int":
                builder.append(StringUtils.format("{}.writeIntArray($1, {});", EnhanceUtils.byteBufUtils, objectStr));
                return;
            case "Integer":
                builder.append(StringUtils.format("{}.writeIntBoxArray($1, {});", EnhanceUtils.byteBufUtils, objectStr));
                return;
            case "long":
                builder.append(StringUtils.format("{}.writeLongArray($1, {});", EnhanceUtils.byteBufUtils, objectStr));
                return;
            case "Long":
                builder.append(StringUtils.format("{}.writeLongBoxArray($1, {});", EnhanceUtils.byteBufUtils, objectStr));
                return;
            case "float":
                builder.append(StringUtils.format("{}.writeFloatArray($1, {});", EnhanceUtils.byteBufUtils, objectStr));
                return;
            case "Float":
                builder.append(StringUtils.format("{}.writeFloatBoxArray($1, {});", EnhanceUtils.byteBufUtils, objectStr));
                return;
            case "double":
                builder.append(StringUtils.format("{}.writeDoubleArray($1, {});", EnhanceUtils.byteBufUtils, objectStr));
                return;
            case "Double":
                builder.append(StringUtils.format("{}.writeDoubleBoxArray($1, {});", EnhanceUtils.byteBufUtils, objectStr));
                return;
            case "String":
                builder.append(StringUtils.format("{}.writeStringArray($1, {});", EnhanceUtils.byteBufUtils, objectStr));
                return;
            case "char":
                builder.append(StringUtils.format("{}.writeCharArray($1, {});", EnhanceUtils.byteBufUtils, objectStr));
                return;
            case "Character":
                builder.append(StringUtils.format("{}.writeCharBoxArray($1, {});", EnhanceUtils.byteBufUtils, objectStr));
                return;
            default:
        }

        var array = "array" + GenerateProtocolFile.index.getAndIncrement();
        var length = "length" + GenerateProtocolFile.index.getAndIncrement();
        builder.append(StringUtils.format("{}[] {} = {};", arrayName, array, objectStr));
        builder.append(StringUtils.format("int {} = ArrayUtils.length({});", length, array));
        builder.append(StringUtils.format("{}.writeInt($1,{});", EnhanceUtils.byteBufUtils, length));

        var i = "i" + GenerateProtocolFile.index.getAndIncrement();
        builder.append(StringUtils.format("for(int {}=0; {}<{}; {}++){", i, i, length, i));

        var element = "element" + GenerateProtocolFile.index.getAndIncrement();
        builder.append(StringUtils.format("{} {} = {}[{}];", arrayName, element, array, i));

        EnhanceUtils.enhanceSerializer(arrayField.getArrayElementRegistration().serializer())
                .writeObject(builder, element, arrayField.getField(), arrayField.getArrayElementRegistration());

        builder.append("}");
    }

    @Override
    public String readObject(StringBuilder builder, Field field, IFieldRegistration fieldRegistration) {
        var arrayField = (ArrayField) fieldRegistration;
        var arrayName = getArrayClassName(arrayField);

        var array = "array" + GenerateProtocolFile.index.getAndIncrement();

        switch (arrayName) {
            case "boolean":
                builder.append(StringUtils.format("{}[] {} = {}.readBooleanArray($1);", arrayName, array, EnhanceUtils.byteBufUtils));
                return array;
            case "Boolean":
                builder.append(StringUtils.format("{}[] {} = {}.readBooleanBoxArray($1);", arrayName, array, EnhanceUtils.byteBufUtils));
                return array;
            case "byte":
                builder.append(StringUtils.format("{}[] {} = {}.readByteArray($1);", arrayName, array, EnhanceUtils.byteBufUtils));
                return array;
            case "Byte":
                builder.append(StringUtils.format("{}[] {} = {}.readByteBoxArray($1);", arrayName, array, EnhanceUtils.byteBufUtils));
                return array;
            case "short":
                builder.append(StringUtils.format("{}[] {} = {}.readShortArray($1);", arrayName, array, EnhanceUtils.byteBufUtils));
                return array;
            case "Short":
                builder.append(StringUtils.format("{}[] {} = {}.readShortBoxArray($1);", arrayName, array, EnhanceUtils.byteBufUtils));
                return array;
            case "int":
                builder.append(StringUtils.format("{}[] {} = {}.readIntArray($1);", arrayName, array, EnhanceUtils.byteBufUtils));
                return array;
            case "Integer":
                builder.append(StringUtils.format("{}[] {} = {}.readIntBoxArray($1);", arrayName, array, EnhanceUtils.byteBufUtils));
                return array;
            case "long":
                builder.append(StringUtils.format("{}[] {} = {}.readLongArray($1);", arrayName, array, EnhanceUtils.byteBufUtils));
                return array;
            case "Long":
                builder.append(StringUtils.format("{}[] {} = {}.readLongBoxArray($1);", arrayName, array, EnhanceUtils.byteBufUtils));
                return array;
            case "float":
                builder.append(StringUtils.format("{}[] {} = {}.readFloatArray($1);", arrayName, array, EnhanceUtils.byteBufUtils));
                return array;
            case "Float":
                builder.append(StringUtils.format("{}[] {} = {}.readFloatBoxArray($1);", arrayName, array, EnhanceUtils.byteBufUtils));
                return array;
            case "double":
                builder.append(StringUtils.format("{}[] {} = {}.readDoubleArray($1);", arrayName, array, EnhanceUtils.byteBufUtils));
                return array;
            case "Double":
                builder.append(StringUtils.format("{}[] {} = {}.readDoubleBoxArray($1);", arrayName, array, EnhanceUtils.byteBufUtils));
                return array;
            case "String":
                builder.append(StringUtils.format("{}[] {} = {}.readStringArray($1);", arrayName, array, EnhanceUtils.byteBufUtils));
                return array;
            case "char":
                builder.append(StringUtils.format("{}[] {} = {}.readCharArray($1);", arrayName, array, EnhanceUtils.byteBufUtils));
                return array;
            case "Character":
                builder.append(StringUtils.format("{}[] {} = {}.readCharBoxArray($1);", arrayName, array, EnhanceUtils.byteBufUtils));
                return array;
            default:
        }

        var length = "length" + GenerateProtocolFile.index.getAndIncrement();
        builder.append(StringUtils.format("int {} = {}.readInt($1);", length, EnhanceUtils.byteBufUtils));

        builder.append(StringUtils.format("{}[] {} = new {}[{}];", arrayName, array, arrayName, length));

        var i = "i" + GenerateProtocolFile.index.getAndIncrement();
        builder.append(StringUtils.format("for(int {}=0; {} < {}; {}++){", i, i, length, i));
        var readObject = EnhanceUtils.enhanceSerializer(arrayField.getArrayElementRegistration().serializer())
                .readObject(builder, arrayField.getField(), arrayField.getArrayElementRegistration());
        builder.append(StringUtils.format("{}[{}] = {};}", array, i, readObject));
        return array;
    }


    private String getArrayClassName(ArrayField arrayField) {
        // 去掉包装类型的前缀java.lang
        return arrayField.getField().getType().getComponentType().getCanonicalName().replaceFirst("java.lang.", StringUtils.EMPTY);
    }

}
