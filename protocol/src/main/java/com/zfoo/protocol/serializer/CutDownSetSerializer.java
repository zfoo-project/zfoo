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
import com.zfoo.protocol.util.StringUtils;

import java.lang.reflect.Field;

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

        // 直接在字节码里调用方法是为了减小生成字节码的体积，下面的代码去掉也不会有任何影响
        switch (setField.getType().getTypeName()) {
            case "java.util.Set<java.lang.Integer>":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}.writeIntSet($1, (Set){});", EnhanceUtils.byteBufUtils, objectStr));
                        return true;
                }
                break;
            case "java.util.Set<java.lang.Long>":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}.writeLongSet($1, (Set){});", EnhanceUtils.byteBufUtils, objectStr));
                        return true;
                }
                break;
            case "java.util.Set<java.lang.String>":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}.writeStringSet($1, (Set){});", EnhanceUtils.byteBufUtils, objectStr));
                        return true;
                }
                break;
            default:
        }

        // Set<IPacket>
        if (setField.getSetElementRegistration() instanceof ObjectProtocolField) {
            var objectProtocolField = (ObjectProtocolField) setField.getSetElementRegistration();
            switch (language) {
                case Enhance:
                    builder.append(StringUtils.format("{}.writePacketSet($1, (Set){}, {});", EnhanceUtils.byteBufUtils, objectStr, EnhanceUtils.getProtocolRegistrationFieldNameByProtocolId(objectProtocolField.getProtocolId())));
                    return true;
            }
        }

        return false;
    }

    @Override
    public String readObject(StringBuilder builder, Field field, IFieldRegistration fieldRegistration, CodeLanguage language) {
        var setField = (SetField) fieldRegistration;
        var set = "set" + GenerateProtocolFile.index.getAndIncrement();

        switch (setField.getType().getTypeName()) {
            case "java.util.Set<java.lang.Integer>":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("Set {} = {}.readIntSet($1);", set, EnhanceUtils.byteBufUtils));
                        return set;
                }
                break;
            case "java.util.Set<java.lang.Long>":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("Set {} = {}.readLongSet($1);", set, EnhanceUtils.byteBufUtils));
                        return set;
                }
                break;
            case "java.util.Set<java.lang.String>":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("Set {} = {}.readStringSet($1);", set, EnhanceUtils.byteBufUtils));
                        return set;
                }
                break;
            default:
        }

        if (setField.getSetElementRegistration() instanceof ObjectProtocolField) {
            var objectProtocolField = (ObjectProtocolField) setField.getSetElementRegistration();
            switch (language) {
                case Enhance:
                    builder.append(StringUtils.format("Set {} = {}.readPacketSet($1, {});", set, EnhanceUtils.byteBufUtils, EnhanceUtils.getProtocolRegistrationFieldNameByProtocolId(objectProtocolField.getProtocolId())));
                    return set;
            }
            return set;
        }

        GenerateProtocolFile.index.getAndDecrement();
        return null;
    }
}
