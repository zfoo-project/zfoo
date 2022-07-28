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

package com.zfoo.util.math.lexer;

import org.junit.Test;

/**
 * @author godotg
 * @version 3.0
 */
public class LexicalAnalysisTest {
    private static final String str = "    void main() {\n" +
            "        if (a == 8) {\n" +
            "            c = c +2;\n" +
            "            b = 1.2;\n" +
            "            c = c + 3;\n" +
            "            d = \"hello world\";\n" +
            "        }\n" +
            "    }";

    @Test
    public void test() {
        var lexical = new LexicalAnalysis();
        lexical.analyze(str.toCharArray());
    }
}
