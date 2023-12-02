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
 * Oneof结构体定义，oneof定义几个属性必须有一个赋值的功能
 */
public class Oneof {
    /**
     * Oneof标示的名称
     */
    private String name;
    /**
     * Oneof包含的属性列表
     */
    private List<Field> fields;
    /**
     * Oneof的注释
     */
    private Comment comment;

    public Oneof setName(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    public Oneof setComment(Comment comment) {
        this.comment = comment;
        return this;
    }

    public Comment getComment() {
        return comment;
    }

    public void addField(Field field) {
        if (getFields() == null) {
            setFields(new ArrayList<>());
        }
        getFields().add(field);
    }

    /**
     * Oneof包含的属性列表
     *
     * @return the fields
     */
    public List<Field> getFields() {
        return fields;
    }

    /**
     * Oneof包含的属性列表
     *
     * @param fields the fields to set
     */
    public void setFields(List<Field> fields) {
        this.fields = fields;
    }
}