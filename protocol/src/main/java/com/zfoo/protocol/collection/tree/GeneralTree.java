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

package com.zfoo.protocol.collection.tree;

import com.zfoo.protocol.util.StringUtils;

/**
 * 多叉树
 *
 * @author godotg
 */
public class GeneralTree<T> {

    private final TreeNode<T> rootNode = new TreeNode<>(null, null);

    public TreeNode<T> getRootNode() {
        return rootNode;
    }

    public TreeNode<T> getNodeByPath(String path) {
        var current = rootNode;
        var splitPath = splitPath(path);
        for (var nodeName : splitPath) {
            current = current.childByName(nodeName);
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    public void addNode(String path, T data) {
        var current = rootNode;

        var splitPath = splitPath(path);
        for (var nodeName : splitPath) {
            current = current.getOrAddChild(nodeName);
        }
        current.setData(data);
    }

    public void removeNode(String path) {
        var current = rootNode;
        var parent = current.getParent();
        var splitPath = splitPath(path);
        for (var nodeName : splitPath) {
            parent = current;
            current = current.childByName(nodeName);
            if (current == null) {
                return;
            }
        }

        if (parent != null) {
            parent.removeChild(current.getName());
        }
    }


    /**
     * 移除所有数据结点
     */
    public void clear() {
        rootNode.clear();
    }

    private String[] splitPath(String path) {
        if (StringUtils.isBlank(path)) {
            return StringUtils.EMPTY_ARRAY;
        }

        if (!path.contains(StringUtils.PERIOD)) {
            return new String[]{path};
        }

        return path.split(StringUtils.PERIOD_REGEX);
    }

}
