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

package com.zfoo.net.router.answer;

import com.zfoo.net.packet.IPacket;
import com.zfoo.net.router.attachment.SignalAttachment;

/**
 * @author godotg
 * @version 3.0
 */
public interface ISyncAnswer<T extends IPacket> {

    /**
     * The return packet for the synchronization request
     */
    T packet();

    /**
     * attachment for synchronous and asynchronous request
     */
    SignalAttachment attachment();

}
