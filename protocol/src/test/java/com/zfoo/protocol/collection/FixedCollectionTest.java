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

import org.junit.Assert;
import org.junit.Test;

/**
 * @author godotg
 * @version 3.0
 */
public class FixedCollectionTest {

    @Test
    public void testFixedSizeListInt() {
        var list = new FixedSizeListInt(3);
        list.set(0, 1);
        list.set(1, 2);
        list.set(2, 3);
        for (int i = 0; i < list.size(); i++) {
            for (var ele : list) {
                // test iterator
            }
        }
    }

    @Test
    public void testFixedSizeList() {
        var list = new FixedSizeList(3);
        list.set(0, "1");
        list.set(1, "2");
        list.set(2, "3");
        for (int i = 0; i < list.size(); i++) {
            for (var ele : list) {
                // test iterator
            }
        }
    }


    @Test
    public void testHashIntSet() {
        var set = new HashIntSet(3);
        set.add(1);
        set.add(2);
        set.add(3);
        for (int i = 0; i < set.size(); i++) {
            for (var ele : set) {
                // test iterator
            }
        }

        Assert.assertTrue(set.contains(3));
    }

}
