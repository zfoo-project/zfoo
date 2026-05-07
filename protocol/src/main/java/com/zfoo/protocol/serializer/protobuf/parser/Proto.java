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

package com.zfoo.protocol.serializer.protobuf.parser;

import com.zfoo.protocol.serializer.protobuf.PbMessage;
import com.zfoo.protocol.serializer.protobuf.PbOption;
import com.zfoo.protocol.serializer.protobuf.Syntax;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines the structure of a proto file
 */
public class Proto {

    /**
     * Starting protocol ID
     */
    private short startProtocolId;
    /**
     * Proto file name
     */
    private String name;
    /**
     * Optional proto settings
     */
    private List<PbOption> options = new ArrayList<>();
    /**
     * Defaults to proto3 if no syntax version is specified
     */
    private Syntax syntax = Syntax.PROTO_3;
    /**
     * Package name of the proto file
     */
    private String protoPackage;
    /**
     * List of defined messages
     */
    private List<PbMessage> pbMessages = new ArrayList<>();
    /**
     * List of imported file paths
     */
    private List<String> imports = new ArrayList<>();
    /**
     * List of single-line comments in the proto file
     */
    private List<String> comments = new ArrayList<>();


    public short getStartProtocolId() {
        return startProtocolId;
    }

    public void setStartProtocolId(short startProtocolId) {
        this.startProtocolId = startProtocolId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PbOption> getOptions() {
        return options;
    }

    public void setOptions(List<PbOption> options) {
        this.options = options;
    }

    public Syntax getSyntax() {
        return syntax;
    }

    public void setSyntax(Syntax syntax) {
        this.syntax = syntax;
    }

    public String getProtoPackage() {
        return protoPackage;
    }

    public void setProtoPackage(String protoPackage) {
        this.protoPackage = protoPackage;
    }

    public List<PbMessage> getPbMessages() {
        return pbMessages;
    }

    public void setPbMessages(List<PbMessage> pbMessages) {
        this.pbMessages = pbMessages;
    }

    public List<String> getImports() {
        return imports;
    }

    public void setImports(List<String> imports) {
        this.imports = imports;
    }

    public List<String> getComments() {
        return comments;
    }

    public void setComments(List<String> comments) {
        this.comments = comments;
    }

}