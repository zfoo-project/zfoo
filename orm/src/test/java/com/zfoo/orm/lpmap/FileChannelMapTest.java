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

package com.zfoo.orm.lpmap;

import com.zfoo.orm.lpmap.model.MyPacket;
import com.zfoo.protocol.ProtocolManager;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Set;

/**
 * @author jaysunxiao
 * @version 3.0
 */
@Ignore
public class FileChannelMapTest {

    @Test
    public void test() {
        ProtocolManager.initProtocol(Set.of(MyPacket.class));

        var map = new FileChannelMap<MyPacket>("db", MyPacket.class);
        var myPacket = new MyPacket();
        myPacket.setA(9999);

        map.insert(myPacket);
        map.insert(myPacket);
        map.insert(myPacket);
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
}
