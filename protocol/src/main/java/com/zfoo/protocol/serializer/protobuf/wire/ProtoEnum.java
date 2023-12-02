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
 * protocol buffer的枚举类型的定义
 */
public class ProtoEnum {
    /**
     * 枚举名称
     */
    private String name;
    /**
     * 枚举包含的值的定义
     */
    private List<EnumEntry> entries;
    /**
     * 枚举包含的选项列表
     */
    private List<Option> options;

    /**
     * 枚举项的定义
     */
    public static class EnumEntry {
        /**
         * 枚举值定义的名称
         */
        private String label;
        /**
         * 枚举定义的值
         */
        private int value;
        /**
         * 枚举的选项列表
         */
        private List<Option> options;
        /**
         * 枚举值的注释
         */
        private Comment comment;

        /**
         * 枚举值定义的名称
         *
         * @return the label
         */
        public String getLabel() {
            return label;
        }

        /**
         * 枚举值定义的名称
         *
         * @param label the label to set
         * @return
         */
        public EnumEntry setLabel(String label) {
            this.label = label;
            return this;
        }

        /**
         * 枚举定义的值
         *
         * @return the value
         */
        public int getValue() {
            return value;
        }

        /**
         * 枚举定义的值
         *
         * @param value the value to set
         * @return
         */
        public EnumEntry setValue(int value) {
            this.value = value;
            return this;
        }

        /**
         * 枚举的选项列表
         *
         * @return the options
         */
        public List<Option> getOptions() {
            return options;
        }

        /**
         * 枚举的选项列表
         *
         * @param options the options to set
         * @return
         */
        public EnumEntry setOptions(List<Option> options) {
            this.options = options;
            return this;
        }

        /**
         * 枚举值的注释
         *
         * @return the comment
         */
        public Comment getComment() {
            return comment;
        }

        /**
         * 枚举值的注释
         *
         * @param comment the comment to set
         * @return
         */
        public EnumEntry setComment(Comment comment) {
            this.comment = comment;
            return this;
        }
    }

    /**
     * 枚举名称
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * 枚举名称
     *
     * @param name the name to set
     * @return
     */
    public ProtoEnum setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * 枚举包含的值的定义
     *
     * @return the entries
     */
    public List<EnumEntry> getEntries() {
        return entries;
    }

    /**
     * 枚举包含的值的定义
     *
     * @param entries the entries to set
     * @return
     */
    public ProtoEnum setEntries(List<EnumEntry> entries) {
        this.entries = entries;
        return this;
    }

    /**
     * 枚举包含的选项列表
     *
     * @return the options
     */
    public List<Option> getOptions() {
        return options;
    }

    /**
     * 枚举包含的选项列表
     *
     * @param options the options to set
     * @return
     */
    public ProtoEnum setOptions(List<Option> options) {
        this.options = options;
        return this;
    }
}