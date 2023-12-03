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

package com.zfoo.protocol.serializer.protobuf.wire;

import java.util.ArrayList;
import java.util.List;

/**
 * protocol buffer消息体的结构定义
 */
public class ProtoMessage {
    /**
     * 消息的名称
     */
    private String name;
    /**
     * 消息包含的Fields列表
     */
    private List<PbField> fields = new ArrayList<>();
    /**
     * 消息内嵌的Message列表
     */
    private List<ProtoMessage> protoMessages = new ArrayList<>();
    /**
     * 消息的备注信息
     */
    private List<String> comments = new ArrayList<>();

    public ProtoMessage addMessage(ProtoMessage child) {
        if (child == null) {
            return this;
        }
        getMessages().add(child);
        return this;
    }

    public ProtoMessage addField(PbField field) {
        if (field == null) {
            return this;
        }
        getFields().add(field);
        return this;
    }

    /**
     * 消息的名称
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * 消息的名称
     *
     * @param name the name to set
     */
    public ProtoMessage setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * 消息包含的Fields列表
     *
     * @return the fields
     */
    public List<PbField> getFields() {
        return fields;
    }

    /**
     * 消息包含的Fields列表
     *
     * @param fields the fields to set
     */
    public ProtoMessage setFields(List<PbField> fields) {
        this.fields = fields;
        return this;
    }

    /**
     * 消息内嵌的Message列表
     *
     * @return the messages
     */
    public List<ProtoMessage> getMessages() {
        return protoMessages;
    }

    /**
     * 消息内嵌的Message列表
     *
     * @param protoMessages the messages to set
     */
    public ProtoMessage setMessages(List<ProtoMessage> protoMessages) {
        this.protoMessages = protoMessages;
        return this;
    }

    public List<String> getComments() {
        return comments;
    }

    public void setComments(List<String> comments) {
        this.comments = comments;
    }
}