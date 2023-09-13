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
import com.zfoo.protocol.ProtocolManager;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Set;

/**
 * @author godotg
 */
@Ignore
public class FileHeapMapTesting {

    @Test
    public void test() {
        ProtocolManager.initProtocol(Set.of(MyPacket.class));

        var map = new FileHeapMap<MyPacket>("db", MyPacket.class);
        var myPacket = new MyPacket();
        myPacket.setA(9999);

        var packet = map.put(1, myPacket);
        Assert.assertNull(packet);

        packet = map.put(2, myPacket);
        Assert.assertNull(packet);

        packet = map.delete(4);
        Assert.assertNull(packet);

        packet = map.delete(2);
        Assert.assertEquals(packet, myPacket);

        map.put(1, myPacket);
        map.put(2, myPacket);
        map.put(3, myPacket);
        map.put(4, myPacket);
        map.put(5, myPacket);
        map.save();
    }

    @Test
    public void benchmarkTest() {
        ProtocolManager.initProtocol(Set.of(MyPacket.class));

        var map = new FileHeapMap<MyPacket>("db", MyPacket.class);
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
        map.save();
    }

    @Test
    public void loadTest() {
        ProtocolManager.initProtocol(Set.of(MyPacket.class));

        var map = new FileHeapMap<MyPacket>("db", MyPacket.class);
        var count = 1000_0000;

        for (var i = 0; i < count; i++) {
            var myPacket = MyPacket.valueOf(i, String.valueOf(i));
            var packet = map.get(i);
            Assert.assertEquals(myPacket, packet);
        }
    }
}
