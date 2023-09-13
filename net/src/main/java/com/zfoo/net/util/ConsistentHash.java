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
 * 带虚拟节点的一致性Hash算法，参考：http://www.zsythink.net/archives/1182
 *
 * @author godotg
 */

public class ConsistentHash<K, V> {

    // 真实结点列表,考虑到服务器上线、下线的场景，即添加、删除的场景会比较频繁，这里使用LinkedList会更好
    private List<Pair<K, V>> realNodes = new ArrayList<>();

    // 虚拟节点，key表示虚拟节点的hash值，value表示虚拟节点的名称
    private TreeMap<Integer, Pair<K, V>> virtualNodeTreeMap = new TreeMap<>();

    // 虚拟节点的数目，数量越大约均匀
    private int virtualNodes = 0;

    public ConsistentHash(List<Pair<K, V>> realNodes, int virtualNodes) {
        // 先把原始的服务器添加到真实结点列表中
        this.realNodes.addAll(realNodes);
        this.virtualNodes = virtualNodes;

        // 初始化
        // 再添加虚拟节点，遍历LinkedList使用foreach循环效率会比较高
        for (var realNode : realNodes) {
            addNode(realNode);
        }
    }

    public void addNode(Pair<K, V> realNode) {
        for (var i = 0; i < this.virtualNodes; i++) {
            var virtualNode = realNode.getKey().toString() + "&&VN" + i;
            var hash = HashUtils.fnvHash(virtualNode);
            virtualNodeTreeMap.put(hash, realNode);
        }
    }


    // 得到应当路由到的结点
    public Pair<K, V> getRealNode(Object key) {
        // 得到该key的hash值
        var hash = HashUtils.fnvHash(key);
        // 第一个Key就是顺时针过去离node最近的那个结点
        var entry = virtualNodeTreeMap.ceilingEntry(hash);
        if (Objects.isNull(entry)) {
            // 如果没有比该key的hash值大的，则从第一个node开始
            return virtualNodeTreeMap.firstEntry().getValue();
        }
        return entry.getValue();
    }

    public TreeMap<Integer, Pair<K, V>> getVirtualNodeTreeMap() {
        return virtualNodeTreeMap;
    }
}
