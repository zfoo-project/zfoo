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
 * Application startup event. This uses Spring's built-in event mechanism; the custom event system is used only for business logic.
 * <p>
 * Startup order: AppStartBeforeEvent -> AppStartEvent -> AppStartAfterEvent
 *
 * @author godotg
 */
public class AppStartAfterEvent extends ApplicationContextEvent {

    public AppStartAfterEvent(ApplicationContext context) {
        super(context);
    }

}
