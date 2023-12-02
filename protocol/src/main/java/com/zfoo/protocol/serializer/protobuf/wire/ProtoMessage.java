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
     * 消息内嵌的扩展的消息列表
     */
    private List<Extend> protoExtends;
    /**
     * 消息包含的扩展的列表
     */
    private List<Extensions> extensionses;

    /**
     * 消息内置的枚举类型
     */
    private List<ProtoEnum> enums;
    /**
     * 消息包含的Oneof的列表
     */
    private List<Oneof> oneofs;
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

    public ProtoMessage addEnum(ProtoEnum protoEnum) {
        if (protoEnum == null) {
            return this;
        }
        getEnums().add(protoEnum);
        return this;
    }

    public ProtoMessage addExtensions(Extensions extensions) {
        if (extensions == null) {
            return this;
        }
        getExtensionses().add(extensions);
        return this;
    }

    public ProtoMessage addReserved(Reserved reserved) {
        if (reserved == null) {
            return this;
        }
        getReserveds().add(reserved);
        return this;
    }

    public ProtoMessage addOneof(Oneof oneof) {
        if (oneof == null) {
            return this;
        }
        getOneofs().add(oneof);
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
     * 消息内嵌的扩展的消息列表
     *
     * @return the protoExtends
     */
    public List<Extend> getProtoExtends() {
        if (protoExtends == null) {
            protoExtends = new ArrayList<>();
        }
        return protoExtends;
    }

    public ProtoMessage addProtoExtends(Extend extend) {
        if (extend == null) {
            return this;
        }
        getProtoExtends().add(extend);
        return this;
    }

    /**
     * 消息内嵌的扩展的消息列表
     *
     * @param protoExtends the protoExtends to set
     */
    public ProtoMessage setProtoExtends(List<Extend> protoExtends) {
        this.protoExtends = protoExtends;
        return this;
    }

    /**
     * 消息包含的扩展的列表
     *
     * @return the extensionses
     */
    public List<Extensions> getExtensionses() {
        if (extensionses == null) {
            extensionses = new ArrayList<>();
        }
        return extensionses;
    }

    /**
     * 消息包含的扩展的列表
     *
     * @param extensionses the extensionses to set
     */
    public ProtoMessage setExtensionses(List<Extensions> extensionses) {
        if (extensionses instanceof ArrayList) {
            this.extensionses = extensionses;
        } else {
            this.extensionses = new ArrayList<>();
            this.extensionses.addAll(extensionses);
        }
        return this;
    }

    /**
     * 消息内置的枚举类型
     *
     * @return the enums
     */
    public List<ProtoEnum> getEnums() {
        if (enums == null) {
            enums = new ArrayList<>();
        }
        return enums;
    }

    /**
     * 消息内置的枚举类型
     *
     * @param enums the enums to set
     */
    public ProtoMessage setEnums(List<ProtoEnum> enums) {
        if (enums instanceof ArrayList) {
            this.enums = enums;
        } else {
            getEnums().addAll(enums);
        }
        return this;
    }

    /**
     * 消息包含的Oneof的列表
     *
     * @return the oneofs
     */
    public List<Oneof> getOneofs() {
        if (oneofs == null) {
            oneofs = new ArrayList<>();
        }
        return oneofs;
    }

    /**
     * 消息包含的Oneof的列表
     *
     * @param oneofs the oneofs to set
     */
    public ProtoMessage setOneofs(List<Oneof> oneofs) {
        if (oneofs instanceof ArrayList) {
            this.oneofs = oneofs;
        } else {
            getOneofs().addAll(oneofs);
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