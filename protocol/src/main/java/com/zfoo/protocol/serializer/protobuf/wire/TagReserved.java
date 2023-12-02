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

import java.util.List;

/**
 * 按Tag的编号预留的结构定义
 */
public class TagReserved extends Reserved {
    /**
     * 消息保留的tag列表
     */
    private List<Integer> tags;
    /**
     * 保留tag的开始和结束范围列表
     */
    private List<StartEnd> startEnds;


    /**
     * 消息保留的tag列表
     *
     * @return the tags
     */
    public List<Integer> getTags() {
        return tags;
    }

    /**
     * 消息保留的tag列表
     *
     * @param tags the tags to set
     * @return
     */
    public TagReserved setTags(List<Integer> tags) {
        this.tags = tags;
        return this;
    }

    /**
     * 保留tag的开始和结束范围列表
     *
     * @return the startEnds
     */
    public List<StartEnd> getStartEnds() {
        return startEnds;
    }

    /**
     * 保留tag的开始和结束范围列表
     *
     * @param startEnds the startEnds to set
     * @return
     */
    public TagReserved setStartEnds(List<StartEnd> startEnds) {
        this.startEnds = startEnds;
        return this;
    }

    public static class StartEnd {
        private int startTag;
        private int endTag;

        /**
         * @return the startTag
         */
        public int getStartTag() {
            return startTag;
        }

        /**
         * @param startTag the startTag to set
         * @return
         */
        public StartEnd setStartTag(int startTag) {
            this.startTag = startTag;
            return this;
        }

        /**
         * @return the endTag
         */
        public int getEndTag() {
            return endTag;
        }

        /**
         * @param endTag the endTag to set
         * @return
         */
        public StartEnd setEndTag(int endTag) {
            this.endTag = endTag;
            return this;
        }
    }
}