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

package com.zfoo.orm.text;

import com.mongodb.client.model.Filters;
import com.zfoo.orm.OrmContext;
import com.zfoo.orm.entity.UserEntity;
import com.zfoo.protocol.util.ThreadUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * @author godotg
 * @version 3.0
 */
@Ignore
public class IndexTextTest {

    @Test
    public void insertTest() {
        var context = new ClassPathXmlApplicationContext("application.xml");
        var listUser = new ArrayList<UserEntity>();
        var userEntity = new UserEntity(1, (byte) 1, (short) 1, 1, true, "两个黄鹂鸣翠柳，一行白鹭上青天。窗含西岭千秋雪，门泊东吴万里船。", null);
        listUser.add(userEntity);
        userEntity = new UserEntity(2, (byte) 2, (short) 2, 2, true, "床前明月光，疑是地上霜。 举头望明月，低头思故乡。", null);
        listUser.add(userEntity);
        userEntity = new UserEntity(3, (byte) 3, (short) 3, 3, true, "吴丝蜀桐张高秋，空山凝云颓不流。 江娥啼竹素女愁，李凭中国弹箜篌。 昆山玉碎凤凰叫，芙蓉泣露香兰笑。 十二门前融冷光，二十三丝动紫皇" +
                "。 女娲炼石补天处，石破天惊逗秋雨。 梦入神山教神妪，老鱼跳波瘦蛟舞。 吴质不眠倚桂树，露脚斜飞湿寒兔。", null);
        listUser.add(userEntity);
        OrmContext.getAccessor().batchInsert(listUser);
        ThreadUtils.sleep(Long.MAX_VALUE);
    }

    @Test
    public void queryTest() {
        var context = new ClassPathXmlApplicationContext("application.xml");
        var collection = OrmContext.getOrmManager().getCollection(UserEntity.class);
        collection.find(Filters.text("窗含西岭千秋雪")).forEach(new Consumer<UserEntity>() {
            @Override
            public void accept(UserEntity userEntity) {
                System.out.println(userEntity);
            }
        });
        ThreadUtils.sleep(Long.MAX_VALUE);
    }

}
