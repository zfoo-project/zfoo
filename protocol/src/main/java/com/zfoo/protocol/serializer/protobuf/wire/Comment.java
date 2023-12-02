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
 * proto文件中注释的结构定义
 */
public class Comment {


    public enum CommentType {
        /**
         * 以"//"开头的注释类型
         */
        INLINE,
        /**
         * 以 "/*"开头的注释类型
         */
        MULTILINE,
        /**
         * 以 "/**"开头的注释类型
         */
        DOCUMENT
    }

    /**
     * 注释内容文本行列表
     */
    private List<String> lines;
    /**
     * 注释的类型
     */
    private CommentType type = CommentType.INLINE;

    /**
     * 注释内容文本行列表
     */
    public List<String> getLines() {
        if (lines == null) {
            lines = new ArrayList<>();
        }
        return lines;
    }

    public void setLines(List<String> lines) {
        if (lines instanceof ArrayList) {
            this.lines = lines;
        } else {
            getLines();
            this.lines.addAll(lines);
        }

    }

    /**
     * 注释的类型
     */
    public CommentType getType() {
        return type;
    }

    public void setType(CommentType type) {
        this.type = type;
    }

}