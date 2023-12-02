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
    private List<Field> fields;
    /**
     * 消息内嵌的Message列表
     */
    private List<ProtoMessage> protoMessages;
    /**
     * 保留字段的设置
     */
    private List<Reserved> reserveds;

    /**
     * 消息的备注信息
     */
    private Comment comment;

    public ProtoMessage addMessage(ProtoMessage child) {
        if (child == null) {
            return this;
        }
        getMessages().add(child);
        return this;
    }

    public ProtoMessage addField(Field field) {
        if (field == null) {
            return this;
        }
        getFields().add(field);
        return this;
    }

    public ProtoMessage addReserved(Reserved reserved) {
        if (reserved == null) {
            return this;
        }
        getReserveds().add(reserved);
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
    public List<Field> getFields() {
        if (fields == null) {
            fields = new ArrayList<>();
        }
        return fields;
    }

    /**
     * 消息包含的Fields列表
     *
     * @param fields the fields to set
     */
    public ProtoMessage setFields(List<Field> fields) {
        if (fields instanceof ArrayList) {
            this.fields = fields;
        } else {
            this.fields = new ArrayList<>();
            this.fields.addAll(fields);
        }
        return this;
    }

    /**
     * 消息内嵌的Message列表
     *
     * @return the messages
     */
    public List<ProtoMessage> getMessages() {
        if (protoMessages == null) {
            protoMessages = new ArrayList<>();
        }
        return protoMessages;
    }

    /**
     * 消息内嵌的Message列表
     *
     * @param protoMessages the messages to set
     */
    public ProtoMessage setMessages(List<ProtoMessage> protoMessages) {
        if (protoMessages instanceof ArrayList) {
            this.protoMessages = protoMessages;
        } else {
            this.protoMessages = new ArrayList<>();
            this.protoMessages.addAll(protoMessages);
        }
        return this;
    }

    /**
     * 保留字段的设置
     *
     * @return the reserveds
     */
    public List<Reserved> getReserveds() {
        if (reserveds == null) {
            reserveds = new ArrayList<>();
        }
        return reserveds;
    }

    /**
     * 保留字段的设置
     *
     * @param reserveds the reserveds to set
     */
    public ProtoMessage setReserveds(List<Reserved> reserveds) {
        if (reserveds instanceof ArrayList) {
            this.reserveds = reserveds;
        } else {
            this.reserveds = new ArrayList<>();
            this.reserveds.addAll(reserveds);
        }
        return this;
    }

    /**
     * 消息的备注信息
     *
     * @return the comment
     */
    public Comment getComment() {
        return comment;
    }

    /**
     * 消息的备注信息
     *
     * @param comment the comment to set
     */
    public ProtoMessage setComment(Comment comment) {
        this.comment = comment;
        return this;
    }
}