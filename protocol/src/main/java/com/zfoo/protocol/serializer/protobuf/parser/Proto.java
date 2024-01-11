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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * proto文件的结构定义
 */
public class Proto {

    /**
     * 协议的起始Id
     */
    private short startProtocolId;
    /**
     * Proto描述文件的名称
     */
    private String name;
    /**
     * Proto的可选信息
     */
    private List<PbOption> options = new ArrayList<>();
    /**
     * 文档版本没有指定默认为proto3
     */
    private Syntax syntax = Syntax.PROTO_3;
    /**
     * proto文件的包名
     */
    private String protoPackage;
    /**
     * 定义的消息列表
     */
    private List<PbMessage> pbMessages = new ArrayList<>();
    /**
     * 文档导入的文件的路径列表
     */
    private List<String> imports = new ArrayList<>();
    /**
     * proto文件的单行注释列表
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