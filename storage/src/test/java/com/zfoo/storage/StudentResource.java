package com.zfoo.storage;

import com.zfoo.storage.anno.AliasFieldName;
import com.zfoo.storage.anno.Id;
import com.zfoo.storage.anno.Index;
import com.zfoo.storage.anno.Storage;
import com.zfoo.storage.resource.User;

import java.util.List;

/**
 * @author veione
 */
@Storage
public record StudentResource(
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
