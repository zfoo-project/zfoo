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
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.util.Set;

/**
 * @author jaysunxiao
 * @version 3.0
 */
@Ignore
public class FileHeapMapTest {

    @Test
    public void test() {
        ProtocolManager.initProtocol(Set.of(MyPacket.class));

        var map = new FileHeapMap<MyPacket>(new File("myPacket.db"), 10, MyPacket.class);
        var myPacket = new MyPacket();
        myPacket.setA(9999);

        var key = map.insert(myPacket);
        Assert.assertEquals(key, 1L);

        var packet = map.put(1, myPacket);
        Assert.assertEquals(packet, myPacket);

        packet = map.put(3, myPacket);
        Assert.assertNull(packet);

        key = map.insert(myPacket);
        Assert.assertEquals(key, 2L);

        packet = map.delete(4);
        Assert.assertNull(packet);

        packet = map.delete(3);
        Assert.assertEquals(packet, myPacket);

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
    public void readTest() {
        ProtocolManager.initProtocol(Set.of(MyPacket.class));
        var map = new FileHeapMap<MyPacket>(new File("myPacket.db"), 10, MyPacket.class);
    }
}
