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

package com.zfoo.protocol.collection.lpmap;

import com.zfoo.protocol.collection.lpmap.model.MyPacket;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author godotg
 */
@Ignore
public class HeapMapTesting {

    @Test
    public void test() {
        var map = new HeapMap<MyPacket>();
        var myPacket = new MyPacket();

        var packet = map.put(0, myPacket);
        Assert.assertNull(packet);

        packet = map.put(3, myPacket);
        Assert.assertNull(packet);

        packet = map.delete(4);
        Assert.assertNull(packet);

        packet = map.delete(3);
        Assert.assertEquals(packet, myPacket);
    }

    @Test
    public void benchmarkTest() {
        var map = new HeapMap<MyPacket>();

        var count = 1000_0000;
        for (var i = 0; i < count; i++) {
            var myPacket = MyPacket.valueOf(i, String.valueOf(i));
            map.put(i, myPacket);
        }

        for (var i = 0; i < count; i++) {
            var myPacket = MyPacket.valueOf(i, String.valueOf(i));
            var packet = map.get(i);
            Assert.assertEquals(myPacket, packet);
        }
    }

}
