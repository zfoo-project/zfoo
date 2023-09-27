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
 *
 */

package com.zfoo.protocol.serializer.reflect;

import com.zfoo.protocol.buffer.ByteBufUtils;
import com.zfoo.protocol.registration.field.IFieldRegistration;
import io.netty.buffer.ByteBuf;

/**
 * @author godotg
 */
public class DoubleSerializer implements ISerializer {

    public static final DoubleSerializer INSTANCE = new DoubleSerializer();

    @Override
    public void writeObject(ByteBuf buffer, Object object, IFieldRegistration fieldRegistration) {
        ByteBufUtils.writeDoubleBox(buffer, (Double) object);
    }

    @Override
    public Object readObject(ByteBuf buffer, IFieldRegistration fieldRegistration) {
        return ByteBufUtils.readDoubleBox(buffer);
    }


    @Override
    public Object defaultValue(IFieldRegistration fieldRegistration) {
        return ByteBufUtils.ZERO_DOUBLE;
    }

    @Override
    public int predictionLength(IFieldRegistration fieldRegistration) {
        return 8;
    }
}
