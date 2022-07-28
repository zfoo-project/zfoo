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

package com.zfoo.net.session.model;

import com.zfoo.protocol.util.StringUtils;
import io.netty.channel.Channel;

import java.io.Closeable;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public class Session implements Closeable {

    private static final AtomicLong ATOMIC_LONG = new AtomicLong(0);

    /**
     * session的id
     */
    private long sid;

    private Channel channel;

    /**
     * Session附带的属性参数
     */
    private Map<AttributeType, Object> attributes = new EnumMap<>(AttributeType.class);


    public Session(Channel channel) {
        if (channel == null) {
            throw new IllegalArgumentException("channel不能为空");
        }
        this.sid = ATOMIC_LONG.getAndIncrement();
        this.channel = channel;
    }


    @Override
    public String toString() {
        return StringUtils.format("[sid:{}] [channel:{}] [attributes:{}]", sid, channel, attributes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Session session = (Session) o;
        return sid == session.sid;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sid);
    }

    @Override
    public void close() {
        channel.close();
    }

    public long getSid() {
        return sid;
    }

    public void setSid(long sid) {
        this.sid = sid;
    }

    public synchronized void putAttribute(AttributeType key, Object value) {
        attributes.put(key, value);
    }

    public synchronized void removeAttribute(AttributeType key) {
        attributes.remove(key);
    }


    public <T> T getAttribute(AttributeType key) {
        return (T) attributes.get(key);
    }

    public Channel getChannel() {
        return channel;
    }

}
