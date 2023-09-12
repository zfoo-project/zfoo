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
     * 创建树的结点
     *
     * @param name   数据结点名称
     * @param parent 父数据结点
     */

    TreeNode(String name, TreeNode<T> parent) {
        this.name = name;
        this.parent = parent;
    }

    /**
     * 检测数据结点名称是否合法
     */
    private static void checkName(String name) {
        if (StringUtils.isBlank(name) || name.contains(StringUtils.PERIOD)) {
            throw new RuntimeException("Name of tree node is invalid.");
        }
    }


    /**
     * 获取数据结点的完整名称。
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
     * 根据名称检查是否存在子数据结点
     *
     * @param name 子数据结点名称
     * @return 是否存在子数据结点
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
     * 根据名称获取子数据结点
     *
     * @param name 子数据结点名称
     * @return 指定名称的子数据结点，如果没有找到，则返回空
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
     * 获取所有子节点，包括父节点和子节点的子节点
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
     * 根据名称获取或增加子数据结点
     *
     * @param name 子数据结点名称
     * @return 指定名称的子数据结点，如果对应名称的子数据结点已存在，则返回已存在的子数据结点，否则增加子数据结点
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
     * 根据名称移除子数据结点
     *
     * @param name 子数据结点名称
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
