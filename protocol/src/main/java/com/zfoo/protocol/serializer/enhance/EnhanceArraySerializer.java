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
import com.zfoo.protocol.serializer.CodeLanguage;
import com.zfoo.protocol.serializer.CutDownArraySerializer;
import com.zfoo.protocol.util.StringUtils;

import java.lang.reflect.Field;

/**
 * @author godotg
 */
public class EnhanceArraySerializer implements IEnhanceSerializer {

    @Override
    public void writeObject(StringBuilder builder, String objectStr, Field field, IFieldRegistration fieldRegistration) {
        if (CutDownArraySerializer.getInstance().writeObject(builder, objectStr, field, fieldRegistration, CodeLanguage.Enhance)) {
            return;
        }

        var arrayField = (ArrayField) fieldRegistration;
        var arrayName = CutDownArraySerializer.getInstance().getArrayClassName(arrayField);

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
                .writeObject(builder, element, field, arrayField.getArrayElementRegistration());

        builder.append("}");
    }

    @Override
    public String readObject(StringBuilder builder, Field field, IFieldRegistration fieldRegistration) {
        var cutDown = CutDownArraySerializer.getInstance().readObject(builder, field, fieldRegistration, CodeLanguage.Enhance);
        if (cutDown != null) {
            return cutDown;
        }

        var arrayField = (ArrayField) fieldRegistration;
        var arrayName = CutDownArraySerializer.getInstance().getArrayClassName(arrayField);

        var array = "array" + GenerateProtocolFile.index.getAndIncrement();
        var length = "length" + GenerateProtocolFile.index.getAndIncrement();
        builder.append(StringUtils.format("int {} = {}.readInt($1);", length, EnhanceUtils.byteBufUtils));

        builder.append(StringUtils.format("{}[] {} = new {}[CollectionUtils.comfortableLength({})];", arrayName, array, arrayName, length));

        var i = "i" + GenerateProtocolFile.index.getAndIncrement();
        builder.append(StringUtils.format("for(int {}=0; {} < {}; {}++){", i, i, length, i));
        var readObject = EnhanceUtils.enhanceSerializer(arrayField.getArrayElementRegistration().serializer())
                .readObject(builder, field, arrayField.getArrayElementRegistration());
        builder.append(StringUtils.format("{}[{}] = {};}", array, i, readObject));
        return array;
    }

    @Override
    public String defaultValue(StringBuilder builder, Field field, IFieldRegistration fieldRegistration) {
        var arrayField = (ArrayField) fieldRegistration;
        var arrayName = CutDownArraySerializer.getInstance().getArrayClassName(arrayField);
        var array = "array" + GenerateProtocolFile.index.getAndIncrement();
        builder.append(StringUtils.format("{}[] {} = new {}[0];", arrayName, array, arrayName));
        return array;
    }


}
