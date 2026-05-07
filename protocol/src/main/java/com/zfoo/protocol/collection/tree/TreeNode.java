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

import com.zfoo.protocol.collection.CollectionUtils;
import com.zfoo.protocol.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author godotg
 */
public class TreeNode<T> {

    private String name;
    private T data;
    private TreeNode<T> parent;
    private List<TreeNode<T>> children;


    /**
     * Create a tree node
     *
     * @param name   node name
     * @param parent parent node
     */

    TreeNode(String name, TreeNode<T> parent) {
        this.name = name;
        this.parent = parent;
    }

    /**
     * Validate the node name
     */
    private static void checkName(String name) {
        if (StringUtils.isBlank(name) || name.contains(StringUtils.PERIOD)) {
            throw new RuntimeException("Name of tree node is invalid.");
        }
    }


    /**
     * Get the full name of the data node.
     */
    public String fullName() {
        if (parent == null) {
            return name;
        }

        var parentName = parent.fullName();
        if (parentName == null) {
            return name;
        }

        return StringUtils.format("{}{}{}", parentName, StringUtils.PERIOD, name);
    }


    /**
     * Check if a child node with the given name exists
     *
     * @param name child node name
     * @return true if the child node exists
     */
    public boolean hasChild(String name) {
        checkName(name);

        if (children == null) {
            return false;
        }

        for (var child : children) {
            if (child.name.equals(name)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get the child node by name
     *
     * @param name child node name
     * @return the child node with the given name; null if not found
     */
    public TreeNode<T> childByName(String name) {
        checkName(name);

        if (children == null) {
            return null;
        }

        for (var child : children) {
            if (child.name.equals(name)) {
                return child;
            }
        }

        return null;
    }

    /**
     * Get all child nodes recursively, including descendants
     */
    public List<TreeNode<T>> flatTreeNodes() {
        var result = new ArrayList<TreeNode<T>>();
        result.add(this);

        if (CollectionUtils.isEmpty(children)) {
            return result;
        }

        var queue = new LinkedList<>(children);
        result.addAll(queue);
        while (!queue.isEmpty()) {
            var childTreeNode = queue.poll();
            var childChildren = childTreeNode.getChildren();
            if (CollectionUtils.isEmpty(childChildren)) {
                continue;
            }
            for (var subClassId : childTreeNode.getChildren()) {
                result.add(subClassId);
                queue.offer(subClassId);
            }
        }

        return result;
    }


    /**
     * Get or create the child node with the given name
     *
     * @param name child node name
     * @return the existing or newly created child node with the given name
     */
    public TreeNode<T> getOrAddChild(String name) {
        var node = childByName(name);
        if (node != null) {
            return node;
        }

        node = new TreeNode<>(name, this);

        if (children == null) {
            children = new ArrayList<>();
        }

        children.add(node);

        return node;
    }


    /**
     * Remove the child node with the given name
     *
     * @param name child node name
     */
    public void removeChild(String name) {
        var node = childByName(name);
        if (node == null) {
            return;
        }

        children.remove(node);
    }

    public void clear() {
        name = null;
        data = null;
        parent = null;
        children = null;
    }

    public int childCount() {
        if (CollectionUtils.isEmpty(children)) {
            return 0;
        }
        return children.size();
    }


    public String getName() {
        return name;
    }

    public List<TreeNode<T>> getChildren() {
        return children;
    }

    public TreeNode<T> getParent() {
        return parent;

    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return StringUtils.format("[{}]:[{}]", fullName(), data);
    }
}
