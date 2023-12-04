/*
 * Copyright 2021 The edap Project
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

package com.zfoo.protocol.serializer.protobuf;

import java.util.ArrayList;
import java.util.List;

/**
 * protocol buffer消息体的结构定义
 */
public class PbMessage {
    /**
     * 消息的名称
     */
    private String name;
    /**
     * 协议Id
     */
    private short protocolId;
    /**
     * 消息包含的Fields列表
     */
    private List<PbField> fields = new ArrayList<>();
    /**
     * 消息的备注信息
     */
    private List<String> comments = new ArrayList<>();


    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public short getProtocolId() {
        return protocolId;
    }
    public void setProtocolId(short protocolId) {
        this.protocolId = protocolId;
    }
    public List<PbField> getFields() {
        return fields;
    }
    public void setFields(List<PbField> fields) {
        this.fields = fields;
    }
    public List<String> getComments() {
        return comments;
    }
    public void setComments(List<String> comments) {
        this.comments = comments;
    }
}