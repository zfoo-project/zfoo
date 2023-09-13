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

package com.zfoo.event.model;

import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ApplicationContextEvent;

/**
 * 应用启动事件，这个使用spring自带的事件机制，自研的event事件仅用在业务逻辑
 * <p>
 * 启动顺序为：AppStartBeforeEvent -> AppStartEvent -> AppStartAfterEvent
 *
 * @author godotg
 */
public class AppStartBeforeEvent extends ApplicationContextEvent {

    public AppStartBeforeEvent(ApplicationContext context) {
        super(context);
    }

}
