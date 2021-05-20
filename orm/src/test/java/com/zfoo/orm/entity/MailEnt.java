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

import com.zfoo.orm.model.anno.EntityCache;
import com.zfoo.orm.model.anno.Id;
import com.zfoo.orm.model.anno.Index;
import com.zfoo.orm.model.anno.Persister;
import com.zfoo.orm.model.entity.IEntity;


/**
 * @author jaysunxiao
 * @version 3.0
 */
@EntityCache(cacheStrategy = "tenThousand", persister = @Persister("time30s"))
public class MailEnt implements IEntity<String> {

    @Id
    private String mailId;

    @Index(ascending = true, unique = false)
    private String userName;

    private String content;

    public MailEnt() {
    }

    public MailEnt(String mailId, String userName, String content) {
        this.mailId = mailId;
        this.userName = userName;
        this.content = content;
    }

    @Override
    public String id() {
        return mailId;
    }

    public String getMailId() {
        return mailId;
    }

    public void setMailId(String mailId) {
        this.mailId = mailId;
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

    @Override
    public String toString() {
        return "MailEnt{" +
                "mailId='" + mailId + '\'' +
                ", playName='" + userName + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
