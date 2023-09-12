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

package com.zfoo.orm.query;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author godotg
 */
public class PageTest {

    @Test
    public void pageTest() {
        var list = new ArrayList<Integer>();
        for (var i = 1; i <= 105; i++) {
            list.add(i);
        }

        var page = Page.valueOf(11, 10, list.size());
        var result = page.currentPageList(list).toArray();

        Assert.assertArrayEquals(List.of(101, 102, 103, 104, 105).toArray(), result);
    }

}
