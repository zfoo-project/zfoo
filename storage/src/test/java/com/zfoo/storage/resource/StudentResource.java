/*
 * Copyright (C) 2020 The zfoo Authors
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

package com.zfoo.storage.resource;

import com.zfoo.storage.anno.AliasFieldName;
import com.zfoo.storage.anno.Id;
import com.zfoo.storage.anno.Index;
import com.zfoo.storage.anno.Storage;
import com.zfoo.storage.utils.json.ArrayValue;
import com.zfoo.storage.utils.json.ObjectValue;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * @author godotg
 */
@Storage
@Getter
public class StudentResource {

    @Getter
    @Id
    private int id;

    /**
     * 索引，默认为可重复的索引
     */
    @Getter
    @Index
    private String name;
    @Getter
    @Index
    @AliasFieldName("年龄")
    private int age;
    @Getter
    private float score;
    @Getter
    private String[] courses;
    @Getter
    private User[] users;
    @Getter
    private List<User> userList;
    @Getter
    private User user;
    @Getter
    private ArrayValue arrayValue;
    private ObjectValue objectValue;
    /**
     * 不想映射的字段必须加上transient关键字，这样就不会从Excel中去找对应的列
     */
    @Getter
    private transient String notMapContent;

    @Override
    public String toString() {
        return "StudentResource{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", score=" + score +
                ", courses=" + Arrays.toString(courses) +
                ", users=" + Arrays.toString(users) +
                ", userList=" + userList +
                ", user=" + user +
                ", arrayValue=" + arrayValue +
                ", objectValue=" + objectValue +
                ", notMapContent='" + notMapContent + '\'' +
                '}';
    }
}
