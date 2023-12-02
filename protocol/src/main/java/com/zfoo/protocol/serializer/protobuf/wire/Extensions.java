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

import com.zfoo.protocol.serializer.protobuf.wire.parser.ProtoParser;

/**
 * protocol buffer 扩展tag的结构定义
 */
public class Extensions {

    /**
     * 扩展开始的Tag编号
     */
    private int startTag;
    /**
     * 扩展结束的Tag编号
     */
    private int endTag;

    public Extensions setEndTag(int endTag) {
        this.endTag = endTag;
        return this;
    }

    public int getEndTag() {
        return endTag;
    }

    public Extensions setStartTag(int startTag) {
        this.startTag = startTag;
        return this;
    }

    public int getStartTag() {
        return startTag;
    }

    public static Extensions parseExtensions(String expression) {
        String exp = expression.trim();
        if (exp.isEmpty()) {
            throw new RuntimeException("extensions expression is empty");
        }
        int index = exp.indexOf("to");
        if (index < 0) {
            throw new RuntimeException("extensions expression must be start to end");
        }
        String sstart = exp.substring(0, index).trim();
        if (sstart.isEmpty()) {
            throw new RuntimeException("extensions expression start empty");
        }
        int start = ProtoParser.parseInt(sstart);
        if (start < 1) {
            throw new RuntimeException("extensions expression start cann't be " + start);
        }
        String sEnd = exp.substring(index + 2).trim();
        if (sEnd.isEmpty()) {
            throw new RuntimeException("extensions expression end empty");
        }
        int end;
        if ("max".equalsIgnoreCase(sEnd)) {
            end = WireFormat.MAX_TAG_VALUE;
        } else {
            end = ProtoParser.parseInt(sEnd);
        }
        if (end <= start) {
            throw new RuntimeException("extensions start tag <= end tag");
        }
        Extensions ext = new Extensions();
        ext.setStartTag(start).setEndTag(end);
        return ext;
    }
}