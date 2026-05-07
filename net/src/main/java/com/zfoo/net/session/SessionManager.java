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

import java.util.function.Consumer;

/**
 * @author godotg
 */
public class SessionManager implements ISessionManager {

    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);

    /**
     * Sessions established when this node acts as a server (connected by other clients).
     * <p>
     * For example, when acting as a gateway, a large number of client connections will be stored here.
     * This map should be used for global message broadcasting.
     */
    private final ConcurrentHashMapLongObject<Session> serverSessionMap = new ConcurrentHashMapLongObject<>(128);

    /**
     * Sessions established when this node acts as a client (connected to other servers).
     * For example, when configured as a Consumer, a TcpClient will be created to connect to a Provider,
     * and the resulting session will be stored here.
     */
    private final ConcurrentHashMapLongObject<Session> clientSessionMap = new ConcurrentHashMapLongObject<>(8);

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

}
