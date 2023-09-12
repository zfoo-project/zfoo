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

import com.zfoo.storage.anno.Id;
import com.zfoo.storage.anno.Index;
import com.zfoo.storage.anno.Storage;

/**
 * @author godotg
 */
@Storage("StudentCsvResource")
public class StudentCsvResource {

    @Id
    private int id;

    /**
     * 索引，默认为可重复的索引
     */
    @Index
    private String name;

    private int age;
    private float score;
    private String[] courses;
    private User[] users;
    private User user;

    /**
     * 不想映射的字段必须加上transient关键字，这样就不会从Excel中去找对应的列
     */
    private transient String notMapContent;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public float getScore() {
        return score;
    }

    public String[] getCourses() {
        return courses;
    }

    public User[] getUsers() {
        return users;
    }

    public User getUser() {
        return user;
    }

    public String getNotMapContent() {
        return notMapContent;
    }
}
