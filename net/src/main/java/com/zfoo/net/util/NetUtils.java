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

import com.zfoo.protocol.collection.ArrayUtils;
import com.zfoo.protocol.util.AssertionUtils;
import com.zfoo.protocol.util.IOUtils;
import com.zfoo.protocol.util.StringUtils;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 网络相关工具
 *
 * @author godotg
 * @version 3.0
 */
public class NetUtils {


    public final static String LOCAL_LOOPBACK_IP = "127.0.0.1";

    /**
     * 默认最小端口，1024
     */
    public static final int PORT_RANGE_MIN = 1024;
    /**
     * 默认最大端口，65535
     */
    public static final int PORT_RANGE_MAX = 0xFFFF;

    /**
     * IP v4
     */
    public final static Pattern IPV4 = Pattern.compile("\\b((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\b");
    /**
     * IP v6
     */
    public final static Pattern IPV6 = Pattern.compile("(([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]))");

    /**
     * 根据long值获取ip v4地址
     *
     * @param longIP IP的long表示形式
     * @return IP V4 地址
     */
    public static String longToIpv4(long longIP) {
        final StringBuilder sb = new StringBuilder();
        // 直接右移24位
        sb.append((longIP / 1_000_000_000));
        sb.append(".");
        // 将高8位置0，然后右移16位
        sb.append(longIP / 1_000_000 % 1_000);
        sb.append(".");
        sb.append(longIP / 1_000 % 1_000);
        sb.append(".");
        sb.append(longIP % 1_000);
        return sb.toString();
    }

    /**
     * 根据ip地址计算出long型的数据
     *
     * @param strIP IP V4 地址
     * @return long值
     */
    public static long ipv4ToLong(String strIP) {
        if (isValidAddress(strIP)) {
            long[] ip = new long[4];
            // 先找到IP地址字符串中.的位置
            int position1 = strIP.indexOf(".");
            int position2 = strIP.indexOf(".", position1 + 1);
            int position3 = strIP.indexOf(".", position2 + 1);
            // 将每个.之间的字符串转换成整型
            ip[0] = Long.parseLong(strIP.substring(0, position1));
            ip[1] = Long.parseLong(strIP.substring(position1 + 1, position2));
            ip[2] = Long.parseLong(strIP.substring(position2 + 1, position3));
            ip[3] = Long.parseLong(strIP.substring(position3 + 1));
            return (ip[0] * 1_000_000_000) + (ip[1] * 1_000_000) + (ip[2] * 1_000) + ip[3];
        }
        return 0;
    }

    public static boolean isValidAddress(String address) {
        return IPV4.matcher(address).matches() || IPV6.matcher(address).matches();
    }

    // 同时支持ipv4和ipv6
    public static long ipToLong(String ip) {
        var splits = ip.split(StringUtils.PERIOD_REGEX);
        var longs = new long[splits.length];
        for (var i = 0; i < splits.length; i++) {
            var b = Long.parseLong(splits[i]);
            longs[i] = b;
        }
        var longList = ArrayUtils.toList(longs);
        Collections.reverse(longList);
        longs = ArrayUtils.longToArray(longList);

        var value = 0L;
        for (var i = 0; i < longs.length; i++) {
            var b = longs[i];
            var shift = i * 8;
            value = value | (b << shift);
        }
        return value;
    }

    public static String longToIp(long ipLong) {
        var longs = new long[8];
        for (var i = 0; i < longs.length; i++) {
            var shift = i * 8;
            var mask = 0xFFL << shift;
            longs[i] = (ipLong & mask) >> shift;
        }
        var longList = ArrayUtils.toList(longs);
        Collections.reverse(longList);
        var ip = StringUtils.joinWith(".", longList.toArray());
        if (ip.startsWith("0.0.0.0.")) {
            return StringUtils.substringAfterLast(ip, "0.0.0.0.");
        }
        return ip;
    }

    /**
     * 检测本地端口可用性<br>
     * 来自org.springframework.util.SocketUtils
     *
     * @param port 被检测的端口
     * @return 是否可用
     */
    public static boolean isAvailablePort(int port) {
        if (!isValidPort(port)) {
            // 给定的IP未在指定端口范围中
            return false;
        }

        // try-with-resources会自动调用close方法
        try (ServerSocket ss = new ServerSocket(port, 0, getLocalhost())) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 是否为有效的端口<br>
     * 此方法并不检查端口是否被占用
     *
     * @param port 端口号
     * @return 是否有效
     */
    public static boolean isValidPort(int port) {
        // 有效端口是0～65535
        return port >= 0 && port <= PORT_RANGE_MAX;
    }

    /**
     * 查找1024~65535范围内的可用端口<br>
     * 此方法只检测给定范围内的随机一个端口，检测65535-1024次<br>
     *
     * @return 可用的端口
     */
    public static int getAvailablePort() {
        return getAvailablePort(PORT_RANGE_MIN);
    }

    /**
     * 查找指定范围内的可用端口，最大值为65535<br>
     * 此方法只检测给定范围内的随机一个端口，检测65535-minPort次<br>
     *
     * @param minPort 端口最小值（包含）
     * @return 可用的端口
     */
    public static int getAvailablePort(int minPort) {
        return getAvailablePort(minPort, PORT_RANGE_MAX);
    }

    /**
     * 查找指定范围内的可用端口<br>
     * 此方法只检测给定范围内的随机一个端口，检测maxPort-minPort次<br>
     *
     * @param minPort 端口最小值（包含）
     * @param maxPort 端口最大值（包含）
     * @return 可用的端口
     */
    public static int getAvailablePort(int minPort, int maxPort) {
        for (int i = minPort; i <= maxPort; i++) {
            if (isAvailablePort(i)) {
                return i;
            }
        }

        throw new IllegalArgumentException(StringUtils
                .format("Could not find an available port in the range [{}, {}] after {} attempts", minPort, maxPort, maxPort - minPort));
    }

    /**
     * 获取多个本地可用端口<br>
     *
     * @param minPort 端口最小值（包含）
     * @param maxPort 端口最大值（包含）
     * @return 可用的端口
     */
    public static TreeSet<Integer> getAvailablePorts(int numRequested, int minPort, int maxPort) {
        final TreeSet<Integer> availablePorts = new TreeSet<>();
        int attemptCount = 0;
        while ((++attemptCount <= numRequested + 100) && availablePorts.size() < numRequested) {
            availablePorts.add(getAvailablePort(minPort, maxPort));
        }

        if (availablePorts.size() != numRequested) {
            throw new IllegalArgumentException(StringUtils
                    .format("Could not find {} available  ports in the range [{}, {}]", numRequested, minPort, maxPort));
        }

        return availablePorts;
    }

    /**
     * 判定是否为内网IP<br>
     * 私有IP：A类 10.0.0.0-10.255.255.255 B类 172.16.0.0-172.31.255.255 C类 192.168.0.0-192.168.255.255 当然，还有127这个网段是环回地址
     *
     * @param ipAddress IP地址
     * @return 是否为内网IP
     */
    public static boolean isInnerIP(String ipAddress) {
        long ipNum = NetUtils.ipv4ToLong(ipAddress);

        long aBegin = NetUtils.ipv4ToLong("10.0.0.0");
        long aEnd = NetUtils.ipv4ToLong("10.255.255.255");

        long bBegin = NetUtils.ipv4ToLong("172.16.0.0");
        long bEnd = NetUtils.ipv4ToLong("172.31.255.255");

        long cBegin = NetUtils.ipv4ToLong("192.168.0.0");
        long cEnd = NetUtils.ipv4ToLong("192.168.255.255");

        boolean isInnerIp = isInner(ipNum, aBegin, aEnd) || isInner(ipNum, bBegin, bEnd) || isInner(ipNum, cBegin, cEnd) || ipAddress.equals(LOCAL_LOOPBACK_IP);
        return isInnerIp;
    }

    /**
     * 指定IP的long是否在指定范围内
     *
     * @param userIp 用户IP
     * @param begin  开始IP
     * @param end    结束IP
     * @return 是否在范围内
     */
    private static boolean isInner(long userIp, long begin, long end) {
        return (userIp >= begin) && (userIp <= end);
    }


    /**
     * 通过域名得到IP
     *
     * @param hostName HOST
     * @return ip address or hostName if UnknownHostException
     */
    public static String getIpByHost(String hostName) {
        try {
            return InetAddress.getByName(hostName).getHostAddress();
        } catch (UnknownHostException e) {
            return hostName;
        }
    }

    /**
     * 获取本机所有网卡
     */
    public static Set<NetworkInterface> getAllNetworkInterface() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            var set = new HashSet<NetworkInterface>();
            while (networkInterfaces.hasMoreElements()) {
                set.add(networkInterfaces.nextElement());
            }
            return set;
        } catch (SocketException e) {
            return Collections.emptySet();
        }
    }

    /**
     * 获取所有满足过滤条件的本地IP地址对象
     *
     * @return 过滤后的地址对象列表
     */
    public static Set<InetAddress> getAllAddress() {
        var networkInterfaces = getAllNetworkInterface();
        var ipSet = new LinkedHashSet<InetAddress>();
        networkInterfaces.forEach(it -> {
            final Enumeration<InetAddress> inetAddresses = it.getInetAddresses();
            while (inetAddresses.hasMoreElements()) {
                final InetAddress inetAddress = inetAddresses.nextElement();
                if (Objects.nonNull(inetAddress)) {
                    ipSet.add(inetAddress);
                }
            }
        });
        return ipSet;
    }

    /**
     * 获得本机的IPv4地址列表<br>
     * 返回的IP列表有序，按照系统设备顺序
     */
    public static Set<String> localIpv4s() {
        var localAddressList = getAllAddress().stream()
                .filter(it -> it instanceof Inet4Address).collect(Collectors.toSet());
        return toIpList(localAddressList);
    }

    /**
     * 获得本机的IPv6地址列表<br>
     * 返回的IP列表有序，按照系统设备顺序
     */
    public static Set<String> localIpv6s() {
        var localAddressList = getAllAddress().stream()
                .filter(it -> it instanceof Inet6Address).collect(Collectors.toSet());
        return toIpList(localAddressList);
    }

    /**
     * 地址列表转换为IP地址列表
     */
    public static Set<String> toIpList(Set<InetAddress> addressList) {
        var ipSet = new LinkedHashSet<String>();
        for (InetAddress address : addressList) {
            ipSet.add(address.getHostAddress());
        }
        return ipSet;
    }

    /**
     * 获得本机的IP地址列表（包括Ipv4和Ipv6）<br>
     */
    public static Set<String> localIps() {
        var localAddressList = getAllAddress();
        return toIpList(localAddressList);
    }


    /**
     * 获取本机网卡IP地址，这个地址为所有网卡中非回路地址的第一个<br>
     * 如果获取失败调用 {@link InetAddress#getLocalHost()}方法获取。<br>
     * 此方法不会抛出异常，获取失败将返回<code>null</code><br>
     * <p>
     * 参考：http://stackoverflow.com/questions/9481865/getting-the-ip-address-of-the-current-machine-using-java
     *
     * @return 本机网卡IP地址，获取失败返回空字符串
     */
    public static String getLocalhostStr() {
        InetAddress localhost = getLocalhost();
        if (null != localhost) {
            return localhost.getHostAddress();
        }
        return StringUtils.EMPTY;
    }

    /**
     * 获取本机网卡IP地址，规则如下：
     *
     * <pre>
     * 1. 查找所有网卡地址，必须非回路（loopback）地址、非局域网地址（siteLocal）、IPv4地址
     * 2. 如果无满足要求的地址，调用 {@link InetAddress#getLocalHost()} 获取地址
     * </pre>
     *
     * @return 本机网卡IP地址，获取失败返回<code>null</code>
     */
    @Nullable
    public static InetAddress getLocalhost() {
        var address = getAllAddress().stream()
                .filter(it -> !it.isLoopbackAddress()
                        // 非地区本地地址，指10.0.0.0 ~ 10.255.255.255、172.16.0.0 ~ 172.31.255.255、192.168.0.0 ~ 192.168.255.255
                        && it.isSiteLocalAddress()
                        // 需为IPV4地址
                        && it instanceof Inet4Address
                        && !it.getHostAddress().contains(":"))
                .findFirst();

        if (address.isPresent()) {
            return address.get();
        }

        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            return null;
        }
    }

    /**
     * 获得本机MAC地址
     *
     * @return 本机MAC地址
     */
    public static String getLocalMacAddress() {
        return getMacAddress(getLocalhost());
    }

    /**
     * 获得指定地址信息中的MAC地址，使用分隔符“-”
     *
     * @param inetAddress {@link InetAddress}
     * @return MAC地址，用-分隔
     */
    public static String getMacAddress(InetAddress inetAddress) {
        return getMacAddress(inetAddress, "-");
    }

    /**
     * 获得指定地址信息中的MAC地址
     *
     * @param inetAddress {@link InetAddress}
     * @param separator   分隔符，推荐使用“-”或者“:”
     * @return MAC地址，用-分隔
     */
    public static String getMacAddress(InetAddress inetAddress, String separator) {
        AssertionUtils.notNull(inetAddress);

        byte[] mac;
        try {
            mac = NetworkInterface.getByInetAddress(inetAddress).getHardwareAddress();
        } catch (SocketException e) {
            throw new IllegalArgumentException(e);
        }
        if (null != mac) {
            final StringBuilder sb = new StringBuilder();
            String s;
            for (int i = 0; i < mac.length; i++) {
                if (i != 0) {
                    sb.append(separator);
                }
                // 字节转换为整数
                s = Integer.toHexString(mac[i] & 0xFF);
                sb.append(s.length() == 1 ? 0 + s : s);
            }
            return sb.toString();
        }
        return null;
    }

    /**
     * 使用普通Socket发送数据
     *
     * @param host Server主机
     * @param port Server端口
     * @param data 数据
     * @throws IOException IO异常
     */
    public static void netCat(String host, int port, byte[] data) throws IOException {
        OutputStream out = null;
        try {
            Socket socket = new Socket(host, port);
            out = socket.getOutputStream();
            out.write(data);
            out.flush();
        } finally {
            IOUtils.closeIO(out);
        }
    }

}
