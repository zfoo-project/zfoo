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

package com.zfoo.net.util;

import com.zfoo.protocol.model.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Consistent Hash algorithm with virtual nodes, reference: http://www.zsythink.net/archives/1182
 *
 * @author godotg
 */

public class ConsistentHash<K, V> {

    // List of real nodes. Since nodes may be added or removed frequently (server up/down), LinkedList would be more efficient here.
    private List<Pair<K, V>> realNodes = new ArrayList<>();

    // Virtual nodes: key is the hash of the virtual node, value is the corresponding real node
    private TreeMap<Integer, Pair<K, V>> virtualNodeTreeMap = new TreeMap<>();

    // Number of virtual nodes per real node; the more virtual nodes, the more uniform the distribution
    private int virtualNodes = 0;

    public ConsistentHash(List<Pair<K, V>> realNodes, int virtualNodes) {
        // First add all real nodes to the real node list
        this.realNodes.addAll(realNodes);
        this.virtualNodes = virtualNodes;

        // Then add virtual nodes; using foreach loop for traversal is more efficient with LinkedList
        for (var realNode : realNodes) {
            for (var i = 0; i < this.virtualNodes; i++) {
                var virtualNode = realNode.getKey().toString() + "&&VN" + i;
                var hash = HashUtils.fnvHash(virtualNode);
                virtualNodeTreeMap.put(hash, realNode);
            }
        }
    }


    // Get the real node that the given key should be routed to
    public Pair<K, V> getRealNode(Object key) {
        // Compute the hash value of the key
        var hash = HashUtils.fnvHash(key);
        // The first key in the ceiling entry is the closest node clockwise
        var entry = virtualNodeTreeMap.ceilingEntry(hash);
        if (Objects.isNull(entry)) {
            // If no hash value is larger than the key's hash, wrap around to the first node
            return virtualNodeTreeMap.firstEntry().getValue();
        }
        return entry.getValue();
    }

    public TreeMap<Integer, Pair<K, V>> getVirtualNodeTreeMap() {
        return virtualNodeTreeMap;
    }
}
