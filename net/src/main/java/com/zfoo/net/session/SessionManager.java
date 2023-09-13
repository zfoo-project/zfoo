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

import com.zfoo.net.util.SessionUtils;
import com.zfoo.protocol.collection.concurrent.ConcurrentHashMapLongObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * @author godotg
 */
public class SessionManager implements ISessionManager {

    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);

    private static final AtomicInteger CLIENT_ATOMIC = new AtomicInteger(0);

    /**
     * 作为服务器，被别的客户端连接的Session
     * 如：自己作为网关，那肯定有一大堆客户端连接，他们连接上来后，就会保存下来这些信息。
     * 因此：要全局消息广播，其实要用这个Map
     */
    private final ConcurrentHashMapLongObject<Session> serverSessionMap = new ConcurrentHashMapLongObject<>(64);


    /**
     * 作为客户端，连接别的服务器上后，保存下来的Session
     * 如：自己配置了Consumer，说明自己作为消费者将要消费远程接口，就会创建一个TcpClient去连接Provider，那么连接上后，就会保存下来到这个Map中
     */
    private final ConcurrentHashMapLongObject<Session> clientSessionMap = new ConcurrentHashMapLongObject<>(8);

    private volatile int clientSessionChangeId = CLIENT_ATOMIC.incrementAndGet();


    @Override
    public void addServerSession(Session session) {
        if (serverSessionMap.containsKey(session.getSid())) {
            logger.error("Server received duplicate [session:{}]", SessionUtils.sessionInfo(session));
            return;
        }
        serverSessionMap.put(session.getSid(), session);
    }

    @Override
    public void removeServerSession(Session session) {
        if (!serverSessionMap.containsKey(session.getSid())) {
            logger.error("[session:{}] does not exist", SessionUtils.sessionInfo(session));
            return;
        }
        try (session) {
            serverSessionMap.remove(session.getSid());
        }
    }

    @Override
    public Session getServerSession(long sid) {
        return serverSessionMap.get(sid);
    }

    @Override
    public int serverSessionSize() {
        return serverSessionMap.size();
    }

    @Override
    public void forEachServerSession(Consumer<Session> consumer) {
        serverSessionMap.forEachPrimitive(it -> consumer.accept(it.value()));
    }

    @Override
    public void addClientSession(Session session) {
        if (clientSessionMap.containsKey(session.getSid())) {
            logger.error("client received duplicate [session:{}]", SessionUtils.sessionInfo(session));
            return;
        }
        clientSessionMap.put(session.getSid(), session);
        clientSessionChangeId = CLIENT_ATOMIC.incrementAndGet();
    }

    @Override
    public void removeClientSession(Session session) {
        if (!clientSessionMap.containsKey(session.getSid())) {
            logger.error("[session:{}] does not exist", SessionUtils.sessionInfo(session));
            return;
        }
        try (session) {
            clientSessionMap.remove(session.getSid());
        }
        clientSessionChangeId = CLIENT_ATOMIC.incrementAndGet();
    }

    @Override
    public Session getClientSession(long sid) {
        return clientSessionMap.get(sid);
    }

    @Override
    public void forEachClientSession(Consumer<Session> consumer) {
        clientSessionMap.forEachPrimitive(it -> consumer.accept(it.value()));
    }

    @Override
    public int clientSessionSize() {
        return clientSessionMap.size();
    }

    @Override
    public int getClientSessionChangeId() {
        return clientSessionChangeId;
    }

}
