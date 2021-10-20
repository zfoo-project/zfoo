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

package com.zfoo.net.router.attachment;

import com.zfoo.protocol.IPacket;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public interface IAttachment extends IPacket {

    AttachmentType packetType();

    /**
     * 用来确定这条消息在哪一个线程处理
     *
     * @return 一致性hashId
     */
    int executorConsistentHash();

}
