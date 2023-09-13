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

package com.zfoo.protocol.collection;

import com.zfoo.protocol.collection.tree.GeneralTree;
import org.junit.Test;

/**
 * @author godotg
 */
public class GeneralTreeTest {


    @Test
    public void test() {
        var generalTree = new GeneralTree<String>();
        generalTree.addNode("a", "hi");
        generalTree.addNode("a.b", "hello");
        generalTree.addNode("a.b.c", "world1");
        generalTree.addNode("a.b.d", "world2");
        generalTree.addNode("a.b.e", "world3");

        System.out.println(generalTree.getNodeByPath("a.b").getData());
        System.out.println(generalTree.getNodeByPath("a.b").getChildren());
    }

}
