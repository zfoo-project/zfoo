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
import org.junit.Test;

import java.util.Set;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public class ConcurrentHeapMapTest {

    @Test
    public void putIfAbsentTest() {
        ProtocolManager.initProtocol(Set.of(MyPacket.class));
        var myPacket = new MyPacket();
        myPacket.setA(1);

        var map = new ConcurrentHeapMap<MyPacket>();

        var previous1 = map.put(1, myPacket);
        var previous2 = map.put(2, myPacket);
        var previous3 = map.put(2, new MyPacket());

        Assert.assertNull(previous1);
        Assert.assertNull(previous2);
        Assert.assertEquals(previous3, myPacket);
    }

}
