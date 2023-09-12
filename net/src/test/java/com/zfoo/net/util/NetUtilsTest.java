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

package com.zfoo.net.util;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

/**
 * @author godotg
 */
public class NetUtilsTest {

    @Test
    public void localhostTest() {
        var localHostStr = NetUtils.getLocalhostStr();
        var localHost = NetUtils.getLocalhost();
        Assert.assertEquals(localHostStr, localHost.getHostAddress());
    }

    @Test
    public void ipv4Test() {
        var ipLong = NetUtils.ipv4ToLong(NetUtils.LOCAL_LOOPBACK_IP);
        var ipStr = NetUtils.longToIpv4(ipLong);
        Assert.assertEquals(ipStr, NetUtils.LOCAL_LOOPBACK_IP);
    }

    @Test
    public void isInnerIpTest() {
        Assert.assertTrue(NetUtils.isInnerIP(NetUtils.LOCAL_LOOPBACK_IP));
    }

    @Ignore
    @Test
    public void isNetCatTest() throws IOException {
        NetUtils.netCat(NetUtils.LOCAL_LOOPBACK_IP, 9000, new byte[]{1, 2, 3});
    }

    @Ignore
    @Test
    public void isUsableLocalPortTest() {
        System.out.println(NetUtils.isAvailablePort(2181));
        System.out.println(NetUtils.getAvailablePort(2181));
    }

    @Ignore
    @Test
    public void getAllNetworkInterfaceTest() {
        var set = NetUtils.getAllNetworkInterface();
        set.stream().forEach(it -> System.out.println(it));
    }

    @Ignore
    @Test
    public void getAllAddressTest() {
        var set = NetUtils.getAllAddress();
        set.stream().forEach(it -> System.out.println(it));
    }

    @Ignore
    @Test
    public void localIpv4sTest() {
        var set = NetUtils.localIpv4s();
        set.stream().forEach(it -> System.out.println(it));
    }

}
