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

package com.zfoo.util.math.dfa;

import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

/**
 * @author godotg
 * @version 3.0
 */
@Ignore
public class WordTreeTest {

    @Test
    public void test() {
        WordTree tree = new WordTree();
        tree.addWord("大");
        tree.addWord("大土豆");
        tree.addWord("土豆");
        tree.addWord("刚出锅");
        tree.addWord("出锅");
        tree.addWord("fuck");

        // 正文
        String text = "text asdff asdf afucksdf ";
        List<String> matchAll = tree.matchAll(text, -1, true, false);
        System.out.println(matchAll);
    }

}
