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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * protocol buffer协议消息体属性数据类型定义
 */
public class PbField {
    /**
     * 属性是否赋值类型
     */
    public enum Cardinality {
        /**
         * 未知类型
         */
        UNKNOWN("unknown"),
        /**
         * 必须赋值
         */
        REQUIRED("required"),
        /**
         * 可以不赋值
         */
        OPTIONAL("optional"),
        /**
         * 可以赋多个值
         */
        REPEATED("repeated");

        private final String value;

        public String getValue() {
            return this.value;
        }

        Cardinality(String value) {
            this.value = value;
        }

        private static final Map<String, Cardinality> map = new HashMap<>();

        static {
            for (var ele : Cardinality.values()) {
                map.put(ele.value, ele);
            }
        }

        public static Cardinality cardinalityOf(String str) {
            return map.get(str);
        }
    }

    /**
     * 消息属性的规则
     */
    private Cardinality cardinality;
    /**
     * 消息属性名称
     */
    private String name;
    /**
     * 消息属性值的类型
     */
    private String type;
    /**
     * 属性的顺序标记一个消息内不允许重复
     */
    private int tag;
    /**
     * 消息属性的注释信息
     */
    private List<String> comments = new ArrayList<>();


    public Cardinality getCardinality() {
        return cardinality;
    }

    public void setCardinality(Cardinality cardinality) {
        this.cardinality = cardinality;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    public List<String> getComments() {
        return comments;
    }

    public void setComments(List<String> comments) {
        this.comments = comments;
    }
}