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

package com.zfoo.net.consumer.balancer;

import com.zfoo.net.session.Session;
import com.zfoo.net.util.ConsistentHash;
import com.zfoo.net.util.FastTreeMapIntLong;
import com.zfoo.net.util.HashUtils;
import com.zfoo.protocol.ProtocolManager;
import com.zfoo.protocol.collection.HashSetLong;
import com.zfoo.protocol.exception.RunException;
import com.zfoo.protocol.model.Pair;
import com.zfoo.protocol.registration.ProtocolModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * Consistent-hash load balancer: the same argument always routes to the same service provider.
 * The consistent hash is computed from the argument's toString() value.
 *
 * @author godotg
 */
public class ConsistentHashLoadBalancer extends AbstractConsumerLoadBalancer {

    private static final Logger logger = LoggerFactory.getLogger(ConsistentHashLoadBalancer.class);

    public static final ConsistentHashLoadBalancer INSTANCE = new ConsistentHashLoadBalancer();

    // Array indexed by ProtocolModule ID
    private static final AtomicReferenceArray<ConsistentCache> consistentHashMap = new AtomicReferenceArray<>(ProtocolManager.MAX_MODULE_NUM);
    private static final int VIRTUAL_NODE_NUMS = 200;

    public static class ConsistentCache {
        public HashSetLong providerSids;
        public FastTreeMapIntLong treeMap;

        public ConsistentCache(HashSetLong providerSids, FastTreeMapIntLong treeMap) {
            this.providerSids = providerSids;
            this.treeMap = treeMap;
        }
    }

    public ConsistentHashLoadBalancer() {
    }

    public static ConsistentHashLoadBalancer getInstance() {
        return INSTANCE;
    }

    /**
     * Compute the consistent hash from argument.toString(), so the argument should be unique (e.g., a user ID).
     *
     * @param packet   the request packet
     * @param argument a unique parameter such as a user ID
     * @return the selected provider session
     */
    @Override
    public Session selectProvider(List<Session> providers, Object packet, Object argument) {
        if (argument == null) {
            logger.warn("selectProvider:[{}] argument is null and use random load balancer", packet.getClass().getSimpleName());
            return RandomLoadBalancer.getInstance().selectProvider(providers, packet, argument);
        }

        var module = ProtocolManager.moduleByProtocol(packet.getClass());
        var consistentCache = consistentHashMap.get(module.getId());
        if (consistentCache == null) {
            consistentCache = updateModuleToConsistentHash(providers, module);
        }
        var providerSids = consistentCache.providerSids;
        // Update if the provider list has changed since the cache was built
        if (providerSids.size() != providers.size() || providers.stream().anyMatch(it -> !providerSids.contains(it.getSid()))) {
            consistentCache = updateModuleToConsistentHash(providers, module);
        }
        var treeMap = consistentCache.treeMap;
        var nearestIndex = treeMap.indexOfNearestCeilingKey(HashUtils.fnvHash(argument));
        if (nearestIndex < 0) {
            throw new RunException("no service provides the [module:{}]", module);
        }
        var sid = treeMap.getByIndex(nearestIndex);
        // Since we compare sid every time, the session is guaranteed to be found in providers
        return providers.stream().filter(it -> it.getSid() == sid).findFirst().get();
    }

    @Nullable
    private ConsistentCache updateModuleToConsistentHash(List<Session> providers, ProtocolModule module) {
        var sessionStringList = providers.stream()
                .map(session -> new Pair<>(session.getConsumerRegister().toString(), session.getSid()))
                .sorted((a, b) -> a.getKey().compareTo(b.getKey()))
                .toList();

        var consistentHash = new ConsistentHash<>(sessionStringList, VIRTUAL_NODE_NUMS);
        var virtualNodeTreeMap = consistentHash.getVirtualNodeTreeMap();

        var virtualTreeMap = new TreeMap<Integer, Long>();
        for (var entry : virtualNodeTreeMap.entrySet()) {
            virtualTreeMap.put(entry.getKey(), entry.getValue().getValue());
        }

        // Cache the provider sids
        var sidSet = new HashSetLong(16);
        providers.forEach(it -> sidSet.add(it.getSid()));
        // Use a higher-performance tree map
        var fastTreeMap = new FastTreeMapIntLong(virtualTreeMap);

        var consistentCache = new ConsistentCache(sidSet, fastTreeMap);
        consistentHashMap.set(module.getId(), consistentCache);
        return consistentCache;
    }

}
