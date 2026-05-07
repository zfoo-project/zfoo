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
 * Network-related utility methods.
 *
 * @author godotg
 */
public class NetUtils {


    public final static String LOCAL_LOOPBACK_IP = "127.0.0.1";

    /**
     * Default minimum port: 1024
     */
    public static final int PORT_RANGE_MIN = 1024;
    /**
     * Default maximum port: 65535
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
     * Convert a long value to an IPv4 address string.
     *
     * @param longIP the long representation of an IP address
     * @return the IPv4 address string
     */
    public static String longToIpv4(long longIP) {
        final StringBuilder sb = new StringBuilder();
        // Shift right 24 bits to get the first octet
        sb.append((longIP / 1_000_000_000));
        sb.append(".");
        // Clear the top 8 bits and shift right 16 bits for the second octet
        sb.append(longIP / 1_000_000 % 1_000);
        sb.append(".");
        sb.append(longIP / 1_000 % 1_000);
        sb.append(".");
        sb.append(longIP % 1_000);
        return sb.toString();
    }

    /**
     * Convert an IPv4 address string to a long value.
     *
     * @param strIP the IPv4 address string
     * @return the long representation
     */
    public static long ipv4ToLong(String strIP) {
        if (isValidAddress(strIP)) {
            long[] ip = new long[4];
            // Find the positions of '.' in the IP address string
            int position1 = strIP.indexOf(".");
            int position2 = strIP.indexOf(".", position1 + 1);
            int position3 = strIP.indexOf(".", position2 + 1);
            // Parse each octet between dots as a long
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

    // Supports both IPv4 and IPv6
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
     * Checks whether a local port is available.<br>
     * Inspired by org.springframework.util.SocketUtils.
     *
     * @param port the port to check
     * @return {@code true} if the port is available
     */
    public static boolean isAvailablePort(int port) {
        if (!isValidPort(port)) {
            // Port number is outside the valid range
            return false;
        }

        // try-with-resources automatically calls close()
        try (ServerSocket ss = new ServerSocket(port, 0, getLocalhost())) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks whether a port number is valid.<br>
     * This method does NOT check whether the port is already in use.
     *
     * @param port the port number
     * @return {@code true} if the port is in the range [0, 65535]
     */
    public static boolean isValidPort(int port) {
        // Valid port range: 0 ~ 65535
        return port >= 0 && port <= PORT_RANGE_MAX;
    }

    /**
     * Finds an available port in the range [1024, 65535].<br>
     * Iterates sequentially up to (65535 - 1024) times.<br>
     *
     * @return an available port number
     */
    public static int getAvailablePort() {
        return getAvailablePort(PORT_RANGE_MIN);
    }

    /**
     * Finds an available port in the range [{@code minPort}, 65535].<br>
     * Iterates sequentially up to (65535 - minPort) times.<br>
     *
     * @param minPort the minimum port number (inclusive)
     * @return an available port number
     */
    public static int getAvailablePort(int minPort) {
        return getAvailablePort(minPort, PORT_RANGE_MAX);
    }

    /**
     * Finds an available port in the range [{@code minPort}, {@code maxPort}].<br>
     * Iterates sequentially up to (maxPort - minPort) times.<br>
     *
     * @param minPort the minimum port number (inclusive)
     * @param maxPort the maximum port number (inclusive)
     * @return an available port number
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
     * Retrieves multiple available local ports.<br>
     *
     * @param numRequested the number of ports needed
     * @param minPort      the minimum port number (inclusive)
     * @param maxPort      the maximum port number (inclusive)
     * @return a sorted set of available port numbers
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
     * Determines whether an IP address belongs to a private (intranet) range.<br>
     * Private ranges: Class A 10.0.0.0–10.255.255.255,
     * Class B 172.16.0.0–172.31.255.255, Class C 192.168.0.0–192.168.255.255,
     * and the loopback range 127.x.x.x.
     *
     * @param ipAddress the IP address string
     * @return {@code true} if the address is a private/loopback address
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
     * Checks whether the numeric representation of an IP address falls within [begin, end].
     *
     * @param userIp the numeric IP to test
     * @param begin  the lower bound (inclusive)
     * @param end    the upper bound (inclusive)
     * @return {@code true} if {@code userIp} is within the range
     */
    private static boolean isInner(long userIp, long begin, long end) {
        return (userIp >= begin) && (userIp <= end);
    }


    /**
     * Resolves a hostname to its IP address string.
     *
     * @param hostName the hostname or domain name
     * @return the resolved IP address, or the original hostName if resolution fails
     */
    public static String getIpByHost(String hostName) {
        try {
            return InetAddress.getByName(hostName).getHostAddress();
        } catch (UnknownHostException e) {
            return hostName;
        }
    }

    /**
     * Returns all network interfaces on the local machine.
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
     * Returns all local {@link InetAddress} objects across all network interfaces.
     *
     * @return a set of local addresses
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
     * Returns all local IPv4 addresses.<br>
     * The list is ordered by system device order.
     */
    public static Set<String> localIpv4s() {
        var localAddressList = getAllAddress().stream()
                .filter(it -> it instanceof Inet4Address).collect(Collectors.toSet());
        return toIpList(localAddressList);
    }

    /**
     * Returns all local IPv6 addresses.<br>
     * The list is ordered by system device order.
     */
    public static Set<String> localIpv6s() {
        var localAddressList = getAllAddress().stream()
                .filter(it -> it instanceof Inet6Address).collect(Collectors.toSet());
        return toIpList(localAddressList);
    }

    /**
     * Converts a set of {@link InetAddress} objects to a set of IP address strings.
     */
    public static Set<String> toIpList(Set<InetAddress> addressList) {
        var ipSet = new LinkedHashSet<String>();
        for (InetAddress address : addressList) {
            ipSet.add(address.getHostAddress());
        }
        return ipSet;
    }

    /**
     * Returns all local IP addresses (both IPv4 and IPv6).
     */
    public static Set<String> localIps() {
        var localAddressList = getAllAddress();
        return toIpList(localAddressList);
    }


    /**
     * Returns the local machine's primary NIC IP address string —
     * the first non-loopback address among all network cards.<br>
     * Falls back to {@link InetAddress#getLocalHost()} on failure.<br>
     * Never throws; returns an empty string if the address cannot be determined.<br>
     * <p>
     * Reference: http://stackoverflow.com/questions/9481865/getting-the-ip-address-of-the-current-machine-using-java
     *
     * @return the local NIC IP address string, or an empty string on failure
     */
    public static String getLocalhostStr() {
        InetAddress localhost = getLocalhost();
        if (null != localhost) {
            return localhost.getHostAddress();
        }
        return StringUtils.EMPTY;
    }

    /**
     * Returns the local machine's primary NIC {@link InetAddress}, following these rules:
     *
     * <pre>
     * 1. Search all NIC addresses; must be non-loopback, site-local, and IPv4.
     * 2. If no address meets the criteria, fall back to {@link InetAddress#getLocalHost()}.
     * </pre>
     *
     * @return the local NIC address, or {@code null} on failure
     */
    @Nullable
    public static InetAddress getLocalhost() {
        var address = getAllAddress().stream()
                .filter(it -> !it.isLoopbackAddress()
                        // Site-local: 10.0.0.0–10.255.255.255, 172.16.0.0–172.31.255.255, 192.168.0.0–192.168.255.255
                        && it.isSiteLocalAddress()
                        // Must be an IPv4 address
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
     * Returns the MAC address of the local machine's primary NIC.
     *
     * @return the MAC address string
     */
    public static String getLocalMacAddress() {
        return getMacAddress(getLocalhost());
    }

    /**
     * Returns the MAC address of the NIC bound to the given address, using {@code "-"} as separator.
     *
     * @param inetAddress the {@link InetAddress} whose NIC MAC is needed
     * @return the MAC address string separated by {@code "-"}
     */
    public static String getMacAddress(InetAddress inetAddress) {
        return getMacAddress(inetAddress, "-");
    }

    /**
     * Returns the MAC address of the NIC bound to the given address.
     *
     * @param inetAddress the {@link InetAddress} whose NIC MAC is needed
     * @param separator   the separator character; {@code "-"} or {@code ":"} are recommended
     * @return the MAC address string
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
                // Convert each byte to its hex integer representation
                s = Integer.toHexString(mac[i] & 0xFF);
                sb.append(s.length() == 1 ? 0 + s : s);
            }
            return sb.toString();
        }
        return null;
    }

    /**
     * Sends raw bytes to a remote host using a plain TCP socket.
     *
     * @param host the target server host
     * @param port the target server port
     * @param data the data to send
     * @throws IOException if an I/O error occurs
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

    /**
     * Checks whether an IP address falls within a given CIDR range.
     *
     * @param ip   the IP address to test
     * @param cidr the CIDR notation (e.g. "192.168.1.0/24")
     * @return {@code true} if the IP is within the CIDR range
     */
    public static boolean isInRange(String ip, String cidr) {
        if (StringUtils.isBlank(ip)) {
            return false;
        }
        if (StringUtils.isBlank(cidr)) {
            return false;
        }
        var ips = ip.split(StringUtils.PERIOD_REGEX);
        var ipAddr = (Integer.parseInt(ips[0]) << 24)
                | (Integer.parseInt(ips[1]) << 16)
                | (Integer.parseInt(ips[2]) << 8) | Integer.parseInt(ips[3]);
        var type = Integer.parseInt(cidr.replaceAll(".*/", ""));
        var mask = 0xFFFFFFFF << (32 - type);
        var cidrIp = cidr.replaceAll("/.*", "");
        var cidrIps = cidrIp.split(StringUtils.PERIOD_REGEX);

        var cidrIpaddr = (Integer.parseInt(cidrIps[0]) << 24)
                | (Integer.parseInt(cidrIps[1]) << 16)
                | (Integer.parseInt(cidrIps[2]) << 8)
                | Integer.parseInt(cidrIps[3]);

        return (ipAddr & mask) == (cidrIpaddr & mask);
    }
}
