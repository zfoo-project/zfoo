package com.zfoo.net.consumer.balancer;

import com.zfoo.net.NetContext;
import com.zfoo.net.session.Session;
import org.apache.curator.shaded.com.google.common.util.concurrent.AtomicLongMap;

import java.util.List;
import java.util.Map;

/**
 * 记忆化一致性hash
 *
 * @author qmr
 * @date 2024/1/6
 */
public class CachedConsistentHashLoadBalancer extends ConsistentHashLoadBalancer {

    public static final CachedConsistentHashLoadBalancer INSTANCE = new CachedConsistentHashLoadBalancer();

    /**
     * 存储已经负载后的sid
     */
    private static final AtomicLongMap<Long> uid2sidMap = AtomicLongMap.create();

    private CachedConsistentHashLoadBalancer() {
    }

    public static CachedConsistentHashLoadBalancer getInstance() {
        return INSTANCE;
    }

    @Override
    public Session selectProvider(List<Session> providers, Object packet, Object argument) {

        if (argument instanceof Long) {
            long sid = uid2sidMap.get((Long) argument);
            if (sid > 0L) {
                Session memorySession = NetContext.getSessionManager().getClientSession(sid);
                if (null != memorySession) {
                    return memorySession;
                } else {
                    uid2sidMap.remove((Long) argument);
                }
            }
        }
        Session loadBalancer = super.selectProvider(providers, packet, argument);
        if (argument instanceof Long){
            uid2sidMap.put((Long) argument, loadBalancer.getSid());
        }
        return loadBalancer;
    }

    public static Map<Long, Long> getUid2sidMap(){
        return uid2sidMap.asMap();
    }
}
