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
package com.zfoo.orm.codec;


import org.bson.codecs.Codec;
import org.bson.codecs.pojo.PropertyCodecProvider;
import org.bson.codecs.pojo.PropertyCodecRegistry;
import org.bson.codecs.pojo.TypeWithTypeParameters;

import java.util.Map;

/**
 * MongoDB官方驱动Map的key只能为String，为了支持key为其它基础类型的Map有了MapCodecProvider
 * java的8种基础类型目前全部支持（char，boolean，byte，short，int，long，float，double）
 *
 * @author lqh
 */
public class MapCodecProvider implements PropertyCodecProvider {
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T> Codec<T> get(TypeWithTypeParameters<T> type, PropertyCodecRegistry registry) {
        if (!Map.class.isAssignableFrom(type.getType())) {
            return null;
        }
        var typeParameters = type.getTypeParameters();
        if (type.getTypeParameters().size() != 2) {
            return null;
        }
        var keyType = typeParameters.get(0);
        var valueType = typeParameters.get(1);
        if (MapKeyCodecEnum.containsKeyDecode(keyType.getType())) {
            return new MapCodec(type.getType(), registry.get(keyType), registry.get(valueType));
        }
        return null;
    }
}