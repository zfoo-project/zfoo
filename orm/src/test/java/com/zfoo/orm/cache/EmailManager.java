package com.zfoo.orm.cache;

import com.zfoo.orm.anno.EntityCacheAutowired;
import com.zfoo.orm.entity.MailEntity;
import org.springframework.stereotype.Component;

/**
 * @Author：awakeyoyoyo
 * @Date：2024/5/23 16:32
 */
@Component
public class EmailManager {

    @EntityCacheAutowired
    public IEntityCache<String, MailEntity> mailEntityCaches;
}
