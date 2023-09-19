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

package com.zfoo.storage.resource;

import com.zfoo.storage.anno.AliasFieldName;
import com.zfoo.storage.anno.Id;
import com.zfoo.storage.anno.Index;
import com.zfoo.storage.anno.Storage;

import java.util.List;

/**
 * @author veione
 */
@Storage
public record TeacherResource(
        @Id
        int id,
        @Index(unique = true)
        String idCard,
        @Index
        String name,
        @AliasFieldName("年龄")
        int age,
        float score,
        String[] courses,
        User[] users,
        List<User> userList,
        User user
) {
}
