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

package com.zfoo.protocol.registration.field;

import com.zfoo.protocol.serializer.reflect.ISerializer;
import com.zfoo.protocol.serializer.reflect.MapSerializer;

import java.lang.reflect.Type;

/**
 * @author godotg
 */
public class MapField implements IFieldRegistration {

    private IFieldRegistration mapKeyRegistration;
    private IFieldRegistration mapValueRegistration;

    private Type type;

    public static MapField valueOf(IFieldRegistration mapKeyRegistration, IFieldRegistration mapValueRegistration, Type type) {
        MapField mapField = new MapField();
        mapField.mapKeyRegistration = mapKeyRegistration;
        mapField.mapValueRegistration = mapValueRegistration;
        mapField.type = type;
        return mapField;
    }


    @Override
    public ISerializer serializer() {
        return MapSerializer.INSTANCE;
    }

    public IFieldRegistration getMapKeyRegistration() {
        return mapKeyRegistration;
    }

    public IFieldRegistration getMapValueRegistration() {
        return mapValueRegistration;
    }

    public Type getType() {
        return type;
    }
}
