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

package com.zfoo.orm.entity;

import com.zfoo.orm.anno.EntityCache;
import com.zfoo.orm.anno.Id;
import com.zfoo.orm.anno.Index;
import com.zfoo.orm.model.IEntity;

import java.util.Date;


/**
 * @author godotg
 * @version 3.0
 */
@EntityCache
public class MailEntity implements IEntity<String> {

    @Id
    private String id;

    @Index(ascending = true, unique = false)
    private String userName;

    private String content;

//    @Index(ascending = true, unique = false, ttlExpireAfterSeconds = 10)
//    private Date createDate;
    @Index(ascending = true, unique = false, ttlExpireAfterSeconds = 10)
    private Date createDate;

    public static MailEntity valueOf(String id, String userName, String content, Date createDate) {
        var entity = new MailEntity();
        entity.id = id;
        entity.userName = userName;
        entity.content = content;
        entity.createDate = createDate;
        return entity;
    }

    @Override
    public String id() {
        return id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }
}
