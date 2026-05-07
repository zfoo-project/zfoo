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
 * Defines the structure of a protobuf message
 */
public class PbMessage {
    /**
     * Message name
     */
    private String name;
    /**
     * Protocol ID
     */
    private short protocolId;
    /**
     * List of fields in the message
     */
    private List<PbField> fields = new ArrayList<>();
    /**
     * Message documentation comment
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