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

package com.zfoo.net.util;

import com.zfoo.protocol.model.Pair;
import com.zfoo.protocol.util.ThreadUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author godotg
 * @version 3.0
 */
@Ignore
public class SimpleCacheTest {

    @Test
    public void test() {
        var cache = SimpleCache.build(3000, 6000, 100, new Function<List<String>, List<Pair<String, String>>>() {
            @Override
            public List<Pair<String, String>> apply(List<String> keyList) {
                return keyList.stream().map(it -> new Pair<>(it, "new-" + it + "-value")).collect(Collectors.toList());
            }
        }, key -> "empty");

        cache.put("a", "b");
        cache.get("a");
        ThreadUtils.sleep(3000);
        System.out.println(cache.get("a"));
        ThreadUtils.sleep(1000);
        System.out.println(cache.get("a"));
        ThreadUtils.sleep(1000);
        System.out.println(cache.get("a"));
        ThreadUtils.sleep(1000);
        System.out.println(cache.get("a"));
        ThreadUtils.sleep(1000);
        System.out.println(cache.get("a"));

        ThreadUtils.sleep(Long.MAX_VALUE);
    }

}
