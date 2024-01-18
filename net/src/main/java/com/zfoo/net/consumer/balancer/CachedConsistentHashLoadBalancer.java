package com.zfoo.net.consumer.balancer;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.zfoo.net.NetContext;
import com.zfoo.net.session.Session;
import com.zfoo.protocol.ProtocolManager;
import com.zfoo.scheduler.util.TimeUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 记忆化一致性hash
 *
 * @author qmr
 */
public class CachedConsistentHashLoadBalancer extends AbstractConsumerLoadBalancer {

    public static final CachedConsistentHashLoadBalancer INSTANCE = new CachedConsistentHashLoadBalancer();
    private static final long EXPIRED_ACCESS_DURATION = 10 * TimeUtils.MILLIS_PER_MINUTE;
    private static final long MAX_CACHE_SIZE = 10_0000;

    /**
     * cache the sid after load balancer，key:[argument + protocolModuleId] -> value:[sid]
     */
    private Cache<Long, Long> cache = Caffeine.newBuilder()
            .expireAfterAccess(EXPIRED_ACCESS_DURATION, TimeUnit.MILLISECONDS)
            .maximumSize(MAX_CACHE_SIZE)
            .build();

    private CachedConsistentHashLoadBalancer() {
    }

    public static CachedConsistentHashLoadBalancer getInstance() {
        return INSTANCE;
    }

    @Override
    public Session selectProvider(List<Session> providers, Object packet, Object argument) {
        if (argument == null) {
            return RandomLoadBalancer.getInstance().selectProvider(providers, packet, argument);
        }

        if (!(argument instanceof Number)) {
            return ConsistentHashLoadBalancer.getInstance().selectProvider(providers, packet, argument);
        }

        var arg = (Number) argument;
        var protocolModuleId = (long) ProtocolManager.moduleByProtocol(packet.getClass()).getId();
        // 8 Byte cachedKey = 7 byte of argument + 1 byte of protocolModuleId
        var cachedKey = arg.longValue() << 8 | protocolModuleId;
        var sid = cache.getIfPresent(cachedKey);
        if (sid == null) {
            var providerSession = ConsistentHashLoadBalancer.getInstance().selectProvider(providers, packet, argument);
            cache.put(cachedKey, providerSession.getSid());
            return providerSession;
        }

        var session = NetContext.getSessionManager().getClientSession(sid);
        if (session == null) {
            session = ConsistentHashLoadBalancer.getInstance().selectProvider(providers, packet, argument);
            cache.put(cachedKey, session.getSid());
        }
        return session;
    }

}
