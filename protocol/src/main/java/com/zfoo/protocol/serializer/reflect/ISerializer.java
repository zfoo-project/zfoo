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

import com.zfoo.protocol.registration.field.IFieldRegistration;
import io.netty.buffer.ByteBuf;

/**
 * @author godotg
 */
public interface ISerializer {

    /**
     * 往buffer中写入这个值是多少，及其反序列化时要用的解析类型是啥
     *
     * @param buffer            buffer缓冲区，存储二进制数控
     * @param object            要写入到buffer中的对象
     * @param fieldRegistration 标记下反序列化时读取这个值要用啥类型方式读取
     */
    void writeObject(ByteBuf buffer, Object object, IFieldRegistration fieldRegistration);

    Object readObject(ByteBuf buffer, IFieldRegistration fieldRegistration);

    Object defaultValue(IFieldRegistration fieldRegistration);

    int predictionLength(IFieldRegistration fieldRegistration);

}
