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
import com.zfoo.protocol.util.ThreadUtils;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author godotg
 */
public class ApplicationTest {

    // Event Bus tutorial, the core idea is the observer pattern.(核心思想是设计模式中的观察者模式)
    @Test
    public void startEventTest() {
        // load the configuration file which must import event.(加载配置文件，配置文件中必须引入event)
        var context = new ClassPathXmlApplicationContext("application.xml");

        // see receiver method of MyController1 and MyController2
        EventBus.post(MyNoticeEvent.valueOf("我的事件"));

        ThreadUtils.sleep(1000);
    }

}
