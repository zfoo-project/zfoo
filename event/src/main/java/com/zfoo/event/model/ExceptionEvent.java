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

import com.zfoo.event.enhance.IEventReceiver;

/**
 * @author godotg
 */
public class ExceptionEvent implements IEvent {

    private IEventReceiver receiver;

    private IEvent event;

    private Throwable throwable;

    public ExceptionEvent(IEventReceiver receiver, IEvent event, Throwable throwable) {
        this.receiver = receiver;
        this.event = event;
        this.throwable = throwable;
    }

    public IEventReceiver getReceiver() {
        return receiver;
    }

    public void setReceiver(IEventReceiver receiver) {
        this.receiver = receiver;
    }

    public IEvent getEvent() {
        return event;
    }

    public void setEvent(IEvent event) {
        this.event = event;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }
}
