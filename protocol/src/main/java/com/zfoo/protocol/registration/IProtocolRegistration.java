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

import io.netty.buffer.ByteBuf;

import java.lang.reflect.Constructor;

/**
 * @author godotg
 * @version 3.0
 */
public interface IProtocolRegistration {

    short protocolId();

    byte module();

    Constructor<?> protocolConstructor();

    /**
     * 序列化
     */
    void write(ByteBuf buffer, Object packet);

    /**
     * 反序列化
     */
    Object read(ByteBuf buffer);

}
