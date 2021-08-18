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
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author jaysunxiao
 * @version 3.0
 */
@Ignore
public class HeapMapTest {

    @Test
    public void test() {
        var map = new HeapMap<MyPacket>(10);
        var myPacket = new MyPacket();

        var packet = map.put(1, myPacket);
        Assert.assertNull(packet);

        packet = map.put(3, myPacket);
        Assert.assertNull(packet);

        packet = map.delete(4);
        Assert.assertNull(packet);

        packet = map.delete(3);
        Assert.assertEquals(packet, myPacket);
    }

}
