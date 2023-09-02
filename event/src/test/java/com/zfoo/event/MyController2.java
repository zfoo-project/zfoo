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

import com.zfoo.event.anno.Bus;
import com.zfoo.event.anno.EventReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author godotg
 * @version 3.0
 */
@Component
public class MyController2 {

    private static final Logger logger = LoggerFactory.getLogger(MyController2.class);

    /**
     * 同一个事件可以被重复注册和接受
     *
     * 异步事件会被不会立刻执行，注意日志打印的线程号
     */
    @EventReceiver(Bus.AsyncThread)
    public void onMyNoticeEvent(MyNoticeEvent event) {
        logger.info("方法2异步执行事件：" + event.getMessage());
    }

}
