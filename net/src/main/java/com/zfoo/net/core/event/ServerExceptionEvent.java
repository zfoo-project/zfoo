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

package com.zfoo.net.core.event;

import com.zfoo.event.model.IEvent;
import com.zfoo.net.session.Session;

/**
 * @author meiw
 */
public class ServerExceptionEvent implements IEvent {

    private Session session;
    private Object packet;
    private Object attachment;
    private Exception exception;

    public static ServerExceptionEvent valueOf(Session session, Object packet, Object attachment, Exception exception) {
        var event = new ServerExceptionEvent();
        event.session = session;
        event.packet = packet;
        event.attachment = attachment;
        event.exception = exception;
        return event;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public Object getPacket() {
        return packet;
    }

    public void setPacket(Object packet) {
        this.packet = packet;
    }

    public Object getAttachment() {
        return attachment;
    }

    public void setAttachment(Object attachment) {
        this.attachment = attachment;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }
}
