package com.zfoo.orm.entity;

import com.zfoo.orm.anno.EntityCache;
import com.zfoo.orm.anno.Id;
import com.zfoo.orm.anno.Index;
import com.zfoo.orm.model.IEntity;
import org.bson.codecs.pojo.annotations.BsonId;

/**
 * 用法有问题的entity
 *
 * @author Sando
 * @version 1.0
 * @since 2024/7/30
 */
@EntityCache
public class WrongEntity implements IEntity<String> {
    @BsonId
    private String userName;
    @Id
    private String mailId;

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
}
