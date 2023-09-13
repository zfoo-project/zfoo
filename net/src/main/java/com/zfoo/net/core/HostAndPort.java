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

package com.zfoo.net.core;

import com.zfoo.protocol.collection.CollectionUtils;
import com.zfoo.protocol.util.StringUtils;

import java.util.*;

/**
 * @author godotg
 */
public class HostAndPort {

    private String host;
    private int port;

    public static HostAndPort valueOf(String host, int port) {
        HostAndPort hostAndPort = new HostAndPort();
        hostAndPort.host = host;
        hostAndPort.port = port;
        return hostAndPort;
    }

    /**
     * @param hostAndPort example -> localhost:port
     */
    public static HostAndPort valueOf(String hostAndPort) {
        var split = hostAndPort.trim().split(StringUtils.COLON_REGEX);
        return valueOf(split[0].trim(), Integer.parseInt(split[1].trim()));
    }

    /**
     * @param hostAndPorts example -> localhost:port,localhost:port,localhost:port
     */
    public static List<HostAndPort> toHostAndPortList(String hostAndPorts) {
        if (StringUtils.isEmpty(hostAndPorts)) {
            return Collections.emptyList();
        }

        var hostAndPortSplits = hostAndPorts.split(StringUtils.COMMA_REGEX);
        var hostAndPortList = new ArrayList<HostAndPort>();
        for (var hostAndPort : hostAndPortSplits) {
            hostAndPortList.add(valueOf(hostAndPort));
        }
        return hostAndPortList;
    }

    public static List<HostAndPort> toHostAndPortList(Collection<String> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        var hostAndPortList = new ArrayList<HostAndPort>();
        list.forEach(it -> hostAndPortList.addAll(toHostAndPortList(it)));
        return hostAndPortList;
    }

    public static String toHostAndPortListStr(Collection<HostAndPort> list) {
        var urlList = list.stream()
                .map(it -> it.toHostAndPortStr())
                .toList();
        return StringUtils.joinWith(StringUtils.COMMA, urlList.toArray());
    }


    public String toHostAndPortStr() {
        return StringUtils.format("{}:{}", this.host.trim(), this.port);
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HostAndPort that = (HostAndPort) o;
        return port == that.port && Objects.equals(host, that.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port);
    }

    @Override
    public String toString() {
        return StringUtils.format("[{}]", toHostAndPortStr());
    }
}
