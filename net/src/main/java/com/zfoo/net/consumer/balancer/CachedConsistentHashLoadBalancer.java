package com.zfoo.net.consumer.balancer;

import com.zfoo.net.NetContext;
import com.zfoo.net.session.Session;
import com.zfoo.protocol.ProtocolManager;
import com.zfoo.scheduler.util.LazyCache;
import com.zfoo.scheduler.util.TimeUtils;

import java.util.List;

/**
 * 记忆化一致性hash
 *
 * @author qmr
 */
public class CachedConsistentHashLoadBalancer extends AbstractConsumerLoadBalancer {

    public static final CachedConsistentHashLoadBalancer INSTANCE = new CachedConsistentHashLoadBalancer();
    private static final long EXPIRED_ACCESS_DURATION = 10 * TimeUtils.MILLIS_PER_MINUTE;
    private static final int MAX_CACHE_SIZE = 10_0000;

    /**
     * cache the sid after load balancer，key:[argument + protocolModuleId] -> value:[sid]
     */
    private LazyCache<Long, Long> cache = new LazyCache<>(MAX_CACHE_SIZE, EXPIRED_ACCESS_DURATION, 5 * TimeUtils.MILLIS_PER_MINUTE, null);

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
        var sid = cache.get(cachedKey);
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
