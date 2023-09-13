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

package com.zfoo.net.session;

import com.zfoo.net.consumer.registry.RegisterVO;
import com.zfoo.protocol.util.StringUtils;
import io.netty.channel.Channel;

import java.io.Closeable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author godotg
 */
public class Session implements Closeable {

    private static final AtomicLong ATOMIC_LONG = new AtomicLong(0);

    /**
     * The globally unique ID of the session and the negative sid are allowed
     */
    private long sid = ATOMIC_LONG.incrementAndGet();

    private Channel channel;

    // ------------------------------------------------------------------------------------------------------------
    // The following are extra parameters, add them yourself if necessary（下面都是额外参数，有需要的自己添加）
    /**
     * EN:The default user ID is an ID greater than 0, or equal 0 if there is no login, user extra parameters
     * CN:默认用户的id都是大于0的id，如果没有登录则等于0，用户额外参数
     */
    private long uid = 0;

    /**
     * EN:Session extra parameters
     * CN:Session附带的属性参数，消费者的属性
     */
    private RegisterVO consumerAttribute = null;

    public Session(Channel channel) {
        if (channel == null) {
            throw new IllegalArgumentException("channel cannot be empty");
        }
        this.channel = channel;
    }


    @Override
    public String toString() {
        return StringUtils.format("[sid:{}] [uid:{}] [channel:{}]", sid, uid, channel);
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
        return (int) sid;
    }

    @Override
    public void close() {
        channel.close();
    }

    public long getSid() {
        return sid;
    }

    public Channel getChannel() {
        return channel;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public RegisterVO getConsumerAttribute() {
        return consumerAttribute;
    }

    public void setConsumerAttribute(RegisterVO consumerAttribute) {
        this.consumerAttribute = consumerAttribute;
    }
}
