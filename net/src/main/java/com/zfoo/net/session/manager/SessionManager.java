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

package com.zfoo.net.session.manager;

import com.zfoo.net.session.model.Session;
import com.zfoo.net.util.SessionUtils;
import com.zfoo.util.security.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public class SessionManager implements ISessionManager {

    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);

    /**
     * 作为服务器，被别的客户端连接的Session
     * 如：自己作为网关，那肯定有一大堆客户端连接，他们连接上来后，就会保存下来这些信息。
     * 因此：要全局消息广播，其实要用这个Map
     */
    private final Map<Long, Session> serverSessionMap = new ConcurrentHashMap<>();


    /**
     * 作为客户端，连接别的服务器上后，保存下来的Session
     * 如：自己配置了Consumer，说明自己作为消费者将要消费远程接口，就会创建一个TcpClient去连接Provider，那么连接上后，就会保存下来到这个Map中
     */
    private final Map<Long, Session> clientSessionMap = new ConcurrentHashMap<>();

    private volatile int clientSessionChangeId = IdUtils.getLocalIntId();


    @Override
    public void addServerSession(Session session) {
        if (serverSessionMap.containsKey(session.getSid())) {
            logger.error("server收到重复的[session:{}]", SessionUtils.sessionInfo(session));
            return;
        }
        serverSessionMap.put(session.getSid(), session);
    }

    @Override
    public void removeServerSession(Session session) {
        if (!serverSessionMap.containsKey(session.getSid())) {
            logger.error("SessionManager中的serverSession没有包含[session:{}]，所以无法移除", SessionUtils.sessionInfo(session));
            return;
        }
        serverSessionMap.remove(session.getSid());
        session.close();
    }

    @Override
    public Session getServerSession(Long id) {
        return serverSessionMap.get(id);
    }

    @Override
    public Map<Long, Session> getServerSessionMap() {
        return Collections.unmodifiableMap(serverSessionMap);
    }

    @Override
    public void addClientSession(Session session) {
        if (clientSessionMap.containsKey(session.getSid())) {
            logger.error("client收到重复的[session:{}]", SessionUtils.sessionInfo(session));
            return;
        }
        clientSessionMap.put(session.getSid(), session);
        clientSessionChangeId = IdUtils.getLocalIntId();
    }

    @Override
    public void removeClientSession(Session session) {
        if (!clientSessionMap.containsKey(session.getSid())) {
            logger.error("SessionManager中的clientSession没有包含[session:{}]，所以无法移除", SessionUtils.sessionInfo(session));
            return;
        }
        clientSessionMap.remove(session.getSid());
        session.close();
        clientSessionChangeId = IdUtils.getLocalIntId();
    }

    @Override
    public Session getClientSession(Long id) {
        return clientSessionMap.get(id);
    }

    @Override
    public Map<Long, Session> getClientSessionMap() {
        return Collections.unmodifiableMap(clientSessionMap);
    }

    @Override
    public int getClientSessionChangeId() {
        return clientSessionChangeId;
    }

}
