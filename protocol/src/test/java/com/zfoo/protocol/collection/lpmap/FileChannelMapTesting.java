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

import java.io.IOException;
import java.util.Set;

/**
 * @author godotg
 */
@Ignore
public class FileChannelMapTesting {

    @Test
    public void test() {
        ProtocolManager.initProtocol(Set.of(MyPacket.class));

        var map = new FileChannelMap<MyPacket>("db", MyPacket.class);
        var myPacket = new MyPacket();
        myPacket.setA(9999);

        map.put(0, myPacket);
        map.put(1, myPacket);
        map.put(2, myPacket);
        map.put(3, myPacket);
    }

    @Test
    public void readTest() {
        ProtocolManager.initProtocol(Set.of(MyPacket.class));

        var map = new FileChannelMap<MyPacket>("db", MyPacket.class);

        System.out.println(map.get(1));
        System.out.println(map.get(2));
        System.out.println(map.get(3));
    }

    @Test
    public void channelHeapTest() {
        ProtocolManager.initProtocol(Set.of(MyPacket.class));

        var map = new FileChannelHeapMap<MyPacket>("db", 1000, MyPacket.class);

        System.out.println(map.get(1));
        System.out.println(map.get(2));
        System.out.println(map.get(3));
    }

    @Test
    public void benchmarkTest() throws IOException {
        ProtocolManager.initProtocol(Set.of(MyPacket.class));

        var map = new FileChannelMap<MyPacket>("db", MyPacket.class);
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
        map.close();
        map = new FileChannelMap<MyPacket>("db", MyPacket.class);
        for (var i = 0; i < count; i++) {
            var myPacket = MyPacket.valueOf(i, String.valueOf(i));
            var packet = map.get(i);
            Assert.assertEquals(myPacket, packet);
        }
    }
}
