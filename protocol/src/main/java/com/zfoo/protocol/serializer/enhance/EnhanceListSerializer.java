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
import com.zfoo.protocol.registration.field.IFieldRegistration;
import com.zfoo.protocol.registration.field.ListField;
import com.zfoo.protocol.serializer.CodeLanguage;
import com.zfoo.protocol.serializer.CutDownListSerializer;
import com.zfoo.protocol.util.StringUtils;

import java.lang.reflect.Field;

/**
 * @author godotg
 */
public class EnhanceListSerializer implements IEnhanceSerializer {

    @Override
    public void writeObject(StringBuilder builder, String objectStr, Field field, IFieldRegistration fieldRegistration) {
        if (CutDownListSerializer.getInstance().writeObject(builder, objectStr, field, fieldRegistration, CodeLanguage.Enhance)) {
            return;
        }

        var listField = (ListField) fieldRegistration;

        var list = "list" + GenerateProtocolFile.index.getAndIncrement();
        builder.append(StringUtils.format("List {} = (List){};", list, objectStr));

        builder.append(StringUtils.format("{}.writeInt($1, CollectionUtils.size({}));", EnhanceUtils.byteBufUtils, list));

        var iterator = "iterator" + GenerateProtocolFile.index.getAndIncrement();
        builder.append(StringUtils.format("Iterator {} = CollectionUtils.iterator({});", iterator, list));
        builder.append(StringUtils.format("while({}.hasNext()){", iterator));

        var element = "element" + GenerateProtocolFile.index.getAndIncrement();
        builder.append(StringUtils.format("Object {}={}.next();", element, iterator));
        EnhanceUtils.enhanceSerializer(listField.getListElementRegistration().serializer())
                .writeObject(builder, element, field, listField.getListElementRegistration());
        builder.append("}");

    }

    @Override
    public String readObject(StringBuilder builder, Field field, IFieldRegistration fieldRegistration) {
        var cutDown = CutDownListSerializer.getInstance().readObject(builder, field, fieldRegistration, CodeLanguage.Enhance);
        if (cutDown != null) {
            return cutDown;
        }

        var listField = (ListField) fieldRegistration;
        var list = "list" + GenerateProtocolFile.index.getAndIncrement();

        var size = "size" + GenerateProtocolFile.index.getAndIncrement();
        builder.append(StringUtils.format("int {}={}.readInt($1);", size, EnhanceUtils.byteBufUtils));

        builder.append(StringUtils.format("List {} = CollectionUtils.newList({});", list, size));

        var i = "i" + GenerateProtocolFile.index.getAndIncrement();

        builder.append(StringUtils.format("for(int {}=0; {}<{}; {}++){", i, i, size, i));
        var readObject = EnhanceUtils.enhanceSerializer(listField.getListElementRegistration().serializer()).readObject(builder, field, listField.getListElementRegistration());
        builder.append(StringUtils.format("{}.add({});}", list, readObject));
        return list;
    }

    @Override
    public String defaultValue(StringBuilder builder, Field field, IFieldRegistration fieldRegistration) {
        var listField = (ListField) fieldRegistration;
        var list = "list" + GenerateProtocolFile.index.getAndIncrement();
        builder.append(StringUtils.format("List {} = CollectionUtils.newList(0);", list));
        return list;
    }

}
