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

package com.zfoo.net.consumer.event;

import com.zfoo.event.model.IEvent;
import com.zfoo.net.core.HostAndPort;

/**
 * @author godotg
 */
public class ProviderStartEvent implements IEvent {

    private HostAndPort hostAndPort;

    public static ProviderStartEvent valueOf(HostAndPort hostAndPort) {
        var event = new ProviderStartEvent();
        event.hostAndPort = hostAndPort;
        return event;
    }

    public HostAndPort getHostAndPort() {
        return hostAndPort;
    }

    public void setHostAndPort(HostAndPort hostAndPort) {
        this.hostAndPort = hostAndPort;
    }
}
