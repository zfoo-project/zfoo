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

package com.zfoo.protocol.serializer.cpp;

import com.zfoo.protocol.generate.GenerateProtocolFile;
import com.zfoo.protocol.model.Pair;
import com.zfoo.protocol.registration.field.ArrayField;
import com.zfoo.protocol.registration.field.IFieldRegistration;
import com.zfoo.protocol.serializer.CodeLanguage;
import com.zfoo.protocol.serializer.CutDownArraySerializer;
import com.zfoo.protocol.serializer.reflect.ObjectProtocolSerializer;
import com.zfoo.protocol.util.StringUtils;

import java.lang.reflect.Field;

import static com.zfoo.protocol.util.FileUtils.LS;

/**
 * @author godotg
 */
public class CppArraySerializer implements ICppSerializer {

    @Override
    public Pair<String, String> field(Field field, IFieldRegistration fieldRegistration) {
        var type = GenerateCppUtils.toCppClassName(field.getType().getComponentType().getSimpleName());
        return new Pair<>(StringUtils.format("vector<{}>", type), field.getName());
    }

    @Override
    public void writeObject(StringBuilder builder, String objectStr, int deep, Field field, IFieldRegistration fieldRegistration) {
        GenerateProtocolFile.addTab(builder, deep);
        if (CutDownArraySerializer.getInstance().writeObject(builder, objectStr, field, fieldRegistration, CodeLanguage.Cpp)) {
            return;
        }

        ArrayField arrayField = (ArrayField) fieldRegistration;

        String length = "length" + GenerateProtocolFile.index.getAndIncrement();
        builder.append(StringUtils.format("int32_t {} = {}.size();", length, objectStr)).append(LS);
        GenerateProtocolFile.addTab(builder, deep);
        builder.append(StringUtils.format("buffer.writeInt({});", length)).append(LS);

        String i = "i" + GenerateProtocolFile.index.getAndIncrement();
        GenerateProtocolFile.addTab(builder, deep);
        builder.append(StringUtils.format("for (auto {} = 0; {} < {}; {}++) {", i, i, length, i)).append(LS);
        GenerateProtocolFile.addTab(builder, deep + 1);
        String element = "element" + GenerateProtocolFile.index.getAndIncrement();
        builder.append(StringUtils.format("{} {} = {}[{}];", GenerateCppUtils.toCppClassName(arrayField.getType().getSimpleName()), element, objectStr, i)).append(LS);

        GenerateCppUtils.cppSerializer(arrayField.getArrayElementRegistration().serializer())
                .writeObject(builder, element, deep + 1, field, arrayField.getArrayElementRegistration());

        GenerateProtocolFile.addTab(builder, deep);
        builder.append("}").append(LS);
    }

    @Override
    public String readObject(StringBuilder builder, int deep, Field field, IFieldRegistration fieldRegistration) {
        GenerateProtocolFile.addTab(builder, deep);
        var cutDown = CutDownArraySerializer.getInstance().readObject(builder, field, fieldRegistration, CodeLanguage.Cpp);
        if (cutDown != null) {
            return cutDown;
        }


        var arrayField = (ArrayField) fieldRegistration;
        var result = "result" + GenerateProtocolFile.index.getAndIncrement();

        var typeName = GenerateCppUtils.toCppClassName(arrayField.getType().getSimpleName());

        var i = "index" + GenerateProtocolFile.index.getAndIncrement();
        var size = "size" + GenerateProtocolFile.index.getAndIncrement();
        builder.append(StringUtils.format("int32_t {} = buffer.readInt();", size)).append(LS);

        GenerateProtocolFile.addTab(builder, deep);
        builder.append(StringUtils.format("vector<{}> {};", typeName, result)).append(LS);

        GenerateProtocolFile.addTab(builder, deep);
        builder.append(StringUtils.format("for (int {} = 0; {} < {}; {}++) {", i, i, size, i)).append(LS);
        var readObject = GenerateCppUtils.cppSerializer(arrayField.getArrayElementRegistration().serializer())
                .readObject(builder, deep + 2, field, arrayField.getArrayElementRegistration());

        var point = StringUtils.EMPTY;
        if (arrayField.getArrayElementRegistration().serializer() == ObjectProtocolSerializer.INSTANCE) {
            point = "*";
        }

        GenerateProtocolFile.addTab(builder, deep + 2);
        builder.append(StringUtils.format("{}.emplace_back({});", result, point, readObject));
        builder.append(LS);
        GenerateProtocolFile.addTab(builder, deep);
        builder.append("}").append(LS);


        return result;
    }
}
