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

package com.zfoo.net.packet.model;

/**
 * 附加包对业务层透明，禁止在业务层使用
 *
 * @author jaysunxiao
 * @version 3.0
 */
public class NoAnswerAttachment implements IPacketAttachment {

    public static final transient short PROTOCOL_ID = 4;

    /**
     * 用来在TaskBus中计算一致性hash的参数
     */
    private int executorConsistentHash;

    public static NoAnswerAttachment valueOf(int executorConsistentHash) {
        var attachment = new NoAnswerAttachment();
        attachment.executorConsistentHash = executorConsistentHash;
        return attachment;
    }

    @Override
    public PacketAttachmentType packetType() {
        return PacketAttachmentType.NO_ANSWER_PACKET;
    }

    @Override
    public int executorConsistentHash() {
        return executorConsistentHash;
    }

    @Override
    public short protocolId() {
        return PROTOCOL_ID;
    }


    public int getExecutorConsistentHash() {
        return executorConsistentHash;
    }

    public void setExecutorConsistentHash(int executorConsistentHash) {
        this.executorConsistentHash = executorConsistentHash;
    }
}
