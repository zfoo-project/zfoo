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

package com.zfoo.protocol.serializer.protobuf.parser;

import com.zfoo.protocol.collection.CollectionUtils;
import com.zfoo.protocol.serializer.protobuf.wire.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * proto文件的结构定义
 */
public class Proto {

    /**
     * Proto描述文件的名称
     */
    private String name;
    /**
     * Proto的可选信息
     */
    private List<Option> options = new ArrayList<>();
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
    private List<ProtoMessage> protoMessages = new ArrayList<>();
    /**
     * 文档导入的文件的路径列表
     */
    private List<String> imports = new ArrayList<>();
    /**
     * 文档的注释
     */
    private String comment;
    /**
     * proto文件的单行注释列表
     */
    private List<String> comments = new ArrayList<>();
    /**
     * 该文件的来源的File对象
     */
    private File file;

    public Proto setSyntax(Syntax syntax) {
        this.syntax = syntax;
        return this;
    }

    public Proto addOption(Option option) {
        getOptions().add(option);
        return this;
    }

    public Proto addImport(String value) {
        getImports().add(value);
        return this;
    }

    public Proto addMsg(ProtoMessage msg) {
        getMessages().add(msg);
        return this;
    }

    /**
     * Proto描述文件的名称
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Proto描述文件的名称
     *
     * @param name the name to set
     * @return
     */
    public Proto setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Proto的可选信息
     *
     * @return the options
     */
    public List<Option> getOptions() {
        return options;
    }

    /**
     * 文档版本没有指定默认为proto2
     *
     * @return the syntax
     */
    public Syntax getSyntax() {
        return syntax;
    }

    /**
     * proto文件的包名
     *
     * @return the protoPackage
     */
    public String getProtoPackage() {
        return protoPackage;
    }

    /**
     * proto文件的包名
     *
     * @param protoPackage the protoPackage to set
     * @return
     */
    public Proto setProtoPackage(String protoPackage) {
        this.protoPackage = protoPackage;
        return this;
    }

    /**
     * 定义的消息列表
     *
     * @return the messages
     */
    public List<ProtoMessage> getMessages() {
        return protoMessages;
    }

    /**
     * 文档导入的文件的路径列表
     *
     * @return the imports
     */
    public List<String> getImports() {
        return imports;
    }

    /**
     * 文档的注释
     *
     * @return the comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * 文档的注释
     *
     * @param comment the comment to set
     * @return
     */
    public Proto setComment(String comment) {
        this.comment = comment;
        return this;
    }

    /**
     * proto文件的单行注释列表
     *
     * @param comments the comments to set
     */
    public void addComments(List<String> comments) {
        if (CollectionUtils.isEmpty(comments)) {
            return;
        }
        this.comments.addAll(comments);
    }


    /**
     * @return the file
     */
    public File getFile() {
        return file;
    }

    /**
     * @param file the file to set
     */
    public void setFile(File file) {
        this.file = file;
    }

}