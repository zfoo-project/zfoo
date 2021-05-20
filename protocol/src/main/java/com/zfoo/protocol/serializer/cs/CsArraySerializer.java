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

package com.zfoo.protocol.serializer.cs;

import com.zfoo.protocol.registration.field.ArrayField;
import com.zfoo.protocol.registration.field.IFieldRegistration;
import com.zfoo.protocol.serializer.GenerateUtils;
import com.zfoo.protocol.util.StringUtils;

import java.lang.reflect.Field;

import static com.zfoo.protocol.util.FileUtils.LS;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public class CsArraySerializer implements ICsSerializer {

    @Override
    public void writeObject(StringBuilder builder, String objectStr, int deep, Field field, IFieldRegistration fieldRegistration) {
        ArrayField arrayField = (ArrayField) fieldRegistration;

        GenerateUtils.addTab(builder, deep);
        builder.append(StringUtils.format("if (({} == null) || ({}.Length == 0))", objectStr, objectStr)).append(LS);
        GenerateUtils.addTab(builder, deep);
        builder.append("{").append(LS);
        GenerateUtils.addTab(builder, deep + 1);
        builder.append("buffer.WriteInt(0);").append(LS);
        GenerateUtils.addTab(builder, deep);

        builder.append("}").append(LS);
        GenerateUtils.addTab(builder, deep);
        builder.append("else").append(LS);
        GenerateUtils.addTab(builder, deep);
        builder.append("{").append(LS);
        GenerateUtils.addTab(builder, deep + 1);
        builder.append(StringUtils.format("buffer.WriteInt({}.Length);", objectStr)).append(LS);
        GenerateUtils.addTab(builder, deep + 1);
        String length = "length" + GenerateUtils.index.getAndIncrement();
        builder.append(StringUtils.format("int {} = {}.Length;", length, objectStr)).append(LS);

        String i = "i" + GenerateUtils.index.getAndIncrement();
        GenerateUtils.addTab(builder, deep + 1);
        builder.append(StringUtils.format("for (int {} = 0; {} < {}; {}++)", i, i, length, i)).append(LS);
        GenerateUtils.addTab(builder, deep + 1);
        builder.append("{").append(LS);

        GenerateUtils.addTab(builder, deep + 2);
        String element = "element" + GenerateUtils.index.getAndIncrement();
        builder.append(StringUtils.format("{} {} = {}[{}];", GenerateCsUtils.toCsClassName(arrayField.getField().getType().getComponentType().getSimpleName()), element, objectStr, i)).append(LS);

        GenerateCsUtils.csSerializer(arrayField.getArrayElementRegistration().serializer())
                .writeObject(builder, element, deep + 2, field, arrayField.getArrayElementRegistration());

        GenerateUtils.addTab(builder, deep + 1);
        builder.append("}").append(LS);
        GenerateUtils.addTab(builder, deep);
        builder.append("}").append(LS);
    }

    @Override
    public String readObject(StringBuilder builder, int deep, Field field, IFieldRegistration fieldRegistration) {
        var arrayField = (ArrayField) fieldRegistration;
        var result = "result" + GenerateUtils.index.getAndIncrement();

        var typeName = GenerateCsUtils.toCsClassName(arrayField.getField().getType().getComponentType().getSimpleName());

        var i = "index" + GenerateUtils.index.getAndIncrement();
        var size = "size" + GenerateUtils.index.getAndIncrement();
        GenerateUtils.addTab(builder, deep);
        builder.append(StringUtils.format("int {} = buffer.ReadInt();", size)).append(LS);

        GenerateUtils.addTab(builder, deep);
        builder.append(StringUtils.format("{}[] {} = new {}[{}];", typeName, result, typeName, size)).append(LS);

        GenerateUtils.addTab(builder, deep);
        builder.append(StringUtils.format("if ({} > 0)", size)).append(LS);
        GenerateUtils.addTab(builder, deep);
        builder.append("{").append(LS);

        GenerateUtils.addTab(builder, deep + 1);
        builder.append(StringUtils.format("for (int {} = 0; {} < {}; {}++)", i, i, size, i)).append(LS);
        GenerateUtils.addTab(builder, deep + 1);
        builder.append("{").append(LS);
        var readObject = GenerateCsUtils.csSerializer(arrayField.getArrayElementRegistration().serializer())
                .readObject(builder, deep + 2, field, arrayField.getArrayElementRegistration());
        GenerateUtils.addTab(builder, deep + 2);
        builder.append(StringUtils.format("{}[{}] = {};", result, i, readObject));
        builder.append(LS);
        GenerateUtils.addTab(builder, deep + 1);
        builder.append("}").append(LS);
        GenerateUtils.addTab(builder, deep);
        builder.append("}").append(LS);


        return result;
    }
}
