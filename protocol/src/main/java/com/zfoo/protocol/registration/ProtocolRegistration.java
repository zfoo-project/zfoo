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

package com.zfoo.protocol.registration;

import com.zfoo.protocol.anno.Compatible;
import com.zfoo.protocol.buffer.ByteBufUtils;
import com.zfoo.protocol.registration.field.IFieldRegistration;
import com.zfoo.protocol.serializer.reflect.ISerializer;
import com.zfoo.protocol.util.ReflectionUtils;
import io.netty.buffer.ByteBuf;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

/**
 * @author godotg
 * @version 3.0
 */
public class ProtocolRegistration implements IProtocolRegistration {

    private short id;
    private byte module;
    private Constructor<?> constructor;
    private Field[] fields;

    /**
     * 所有的协议里的发送顺序都是按字段名称排序
     */
    private IFieldRegistration[] fieldRegistrations;

    public ProtocolRegistration() {

    }

    @Override
    public short protocolId() {
        return id;
    }

    @Override
    public byte module() {
        return module;
    }

    @Override
    public Constructor<?> protocolConstructor() {
        return constructor;
    }


    @Override
    public void write(ByteBuf buffer, Object packet) {
        if (packet == null) {
            ByteBufUtils.writeBoolean(buffer, false);
            return;
        }

        ByteBufUtils.writeBoolean(buffer, true);

        for (int i = 0, length = fields.length; i < length; i++) {
            Field field = fields[i];
            IFieldRegistration packetFieldRegistration = fieldRegistrations[i];
            ISerializer serializer = packetFieldRegistration.serializer();
            Object fieldValue = ReflectionUtils.getField(field, packet);
            serializer.writeObject(buffer, fieldValue, packetFieldRegistration);
        }
    }

    @Override
    public Object read(ByteBuf buffer) {
        if (!ByteBufUtils.readBoolean(buffer)) {
            return null;
        }
        Object object = ReflectionUtils.newInstance(constructor);

        for (int i = 0, length = fields.length; i < length; i++) {
            Field field = fields[i];
            // 协议向后兼容
            if (field.isAnnotationPresent(Compatible.class) && !buffer.isReadable()) {
                break;
            }
            IFieldRegistration packetFieldRegistration = fieldRegistrations[i];
            ISerializer serializer = packetFieldRegistration.serializer();
            Object fieldValue = serializer.readObject(buffer, packetFieldRegistration);
            ReflectionUtils.setField(field, object, fieldValue);
        }
        return object;
    }


    public short getId() {
        return id;
    }

    public void setId(short id) {
        this.id = id;
    }

    public byte getModule() {
        return module;
    }

    public void setModule(byte module) {
        this.module = module;
    }

    public Field[] getFields() {
        return fields;
    }

    public void setFields(Field[] fields) {
        this.fields = fields;
    }

    public IFieldRegistration[] getFieldRegistrations() {
        return fieldRegistrations;
    }

    public void setFieldRegistrations(IFieldRegistration[] fieldRegistrations) {
        this.fieldRegistrations = fieldRegistrations;
    }

    public Constructor<?> getConstructor() {
        return constructor;
    }

    public void setConstructor(Constructor<?> constructor) {
        this.constructor = constructor;
    }

}
