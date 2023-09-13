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

package com.zfoo.protocol.serializer.csharp;

import com.zfoo.protocol.generate.GenerateProtocolFile;
import com.zfoo.protocol.registration.field.IFieldRegistration;
import com.zfoo.protocol.registration.field.ListField;
import com.zfoo.protocol.serializer.CodeLanguage;
import com.zfoo.protocol.serializer.CutDownListSerializer;
import com.zfoo.protocol.util.StringUtils;

import java.lang.reflect.Field;

import static com.zfoo.protocol.util.FileUtils.LS;

/**
 * @author godotg
 */
public class CsListSerializer implements ICsSerializer {

    @Override
    public void writeObject(StringBuilder builder, String objectStr, int deep, Field field, IFieldRegistration fieldRegistration) {
        GenerateProtocolFile.addTab(builder, deep);
        if (CutDownListSerializer.getInstance().writeObject(builder, objectStr, field, fieldRegistration, CodeLanguage.CSharp)) {
            return;
        }

        ListField listField = (ListField) fieldRegistration;
        builder.append(StringUtils.format("if ({} == null)", objectStr)).append(LS);
        GenerateProtocolFile.addTab(builder, deep);
        builder.append("{").append(LS);
        GenerateProtocolFile.addTab(builder, deep + 1);
        builder.append("buffer.WriteInt(0);").append(LS);

        GenerateProtocolFile.addTab(builder, deep);
        builder.append("}").append(LS);
        GenerateProtocolFile.addTab(builder, deep);
        builder.append("else").append(LS);
        GenerateProtocolFile.addTab(builder, deep);
        builder.append("{").append(LS);

        GenerateProtocolFile.addTab(builder, deep + 1);
        builder.append(StringUtils.format("buffer.WriteInt({}.Count);", objectStr)).append(LS);

        GenerateProtocolFile.addTab(builder, deep + 1);
        String length = "length" + GenerateProtocolFile.index.getAndIncrement();
        builder.append(StringUtils.format("int {} = {}.Count;", length, objectStr)).append(LS);

        GenerateProtocolFile.addTab(builder, deep + 1);
        String i = "i" + GenerateProtocolFile.index.getAndIncrement();
        builder.append(StringUtils.format("for (int {} = 0; {} < {}; {}++)", i, i, length, i)).append(LS);
        GenerateProtocolFile.addTab(builder, deep + 1);
        builder.append("{").append(LS);

        GenerateProtocolFile.addTab(builder, deep + 2);
        String element = "element" + GenerateProtocolFile.index.getAndIncrement();
        builder.append(StringUtils.format("var {} = {}[{}];", element, objectStr, i)).append(LS);

        GenerateCsUtils.csSerializer(listField.getListElementRegistration().serializer())
                .writeObject(builder, element, deep + 2, field, listField.getListElementRegistration());

        GenerateProtocolFile.addTab(builder, deep + 1);
        builder.append("}").append(LS);
        GenerateProtocolFile.addTab(builder, deep);
        builder.append("}").append(LS);
    }

    @Override
    public String readObject(StringBuilder builder, int deep, Field field, IFieldRegistration fieldRegistration) {
        GenerateProtocolFile.addTab(builder, deep);
        var cutDown = CutDownListSerializer.getInstance().readObject(builder, field, fieldRegistration, CodeLanguage.CSharp);
        if (cutDown != null) {
            return cutDown;
        }

        var listField = (ListField) fieldRegistration;
        var result = "result" + GenerateProtocolFile.index.getAndIncrement();

        var typeName = GenerateCsUtils.toCsClassName(listField.getType().toString());

        var i = "index" + GenerateProtocolFile.index.getAndIncrement();
        var size = "size" + GenerateProtocolFile.index.getAndIncrement();

        builder.append(StringUtils.format("int {} = buffer.ReadInt();", size)).append(LS);
        GenerateProtocolFile.addTab(builder, deep);
        builder.append(StringUtils.format("var {} = new {}({});", result, typeName, size)).append(LS);

        GenerateProtocolFile.addTab(builder, deep);
        builder.append(StringUtils.format("if ({} > 0)", size)).append(LS);
        GenerateProtocolFile.addTab(builder, deep);
        builder.append("{").append(LS);

        GenerateProtocolFile.addTab(builder, deep + 1);
        builder.append(StringUtils.format("for (int {} = 0; {} < {}; {}++)", i, i, size, i)).append(LS);
        GenerateProtocolFile.addTab(builder, deep + 1);
        builder.append("{").append(LS);
        var readObject = GenerateCsUtils.csSerializer(listField.getListElementRegistration().serializer())
                .readObject(builder, deep + 2, field, listField.getListElementRegistration());
        GenerateProtocolFile.addTab(builder, deep + 2);
        builder.append(StringUtils.format("{}.Add({});", result, readObject)).append(LS);
        GenerateProtocolFile.addTab(builder, deep + 1);
        builder.append("}").append(LS);
        GenerateProtocolFile.addTab(builder, deep);
        builder.append("}").append(LS);


        return result;
    }

}
