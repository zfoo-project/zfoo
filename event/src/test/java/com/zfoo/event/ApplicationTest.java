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

package com.zfoo.event;

import com.zfoo.event.manager.EventBus;
import com.zfoo.util.ThreadUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author godotg
 * @version 3.0
 */
@Ignore
public class ApplicationTest {

    // 事件总线教程，核心思想是设计模式中的观察者模式
    @Test
    public void startEventTest() {
        // 加载配置文件，配置文件中必须引入event
        var context = new ClassPathXmlApplicationContext("application.xml");

        // 事件的接受需要在被Spring管理的bean的方法上加上@EventReceiver注解，即可自动注册事件的监听
        // 参考MyController1中的标准注册方法

        EventBus.submit(MyNoticeEvent.valueOf("处理事件"));

        // 睡眠3秒，等待异步事件执行完
        ThreadUtils.sleep(3000);
    }

}
