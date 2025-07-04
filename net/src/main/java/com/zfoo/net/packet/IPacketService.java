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

package com.zfoo.net.packet;

import io.netty.buffer.ByteBuf;
import org.springframework.lang.Nullable;

/**
 * @author godotg
 */
public interface IPacketService {

    void init();

    DecodedPacketInfo read(ByteBuf buffer);

    void write(ByteBuf buffer, Object packet, @Nullable Object attachment);

    void writeHeaderAndBody(ByteBuf buffer, Object packet, @Nullable Object attachment);

    void writeHeaderBefore(ByteBuf buffer);
}
