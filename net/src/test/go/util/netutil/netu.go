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

package netutil

import (
	"bufio"
	"encoding/binary"
	"fmt"
	"io"
	"net"
	"os"
	"regexp"
	"strconv"
	"strings"
	"unsafe"
)

// IP ip struct info.
type IP struct {
	Begin        uint32
	End          uint32
	ISPCode      int
	ISP          string
	CountryCode  int
	Country      string
	ProvinceCode int
	Province     string
	CityCode     int
	City         string
	DistrictCode int
	District     string
	Latitude     float64
	Longitude    float64
}

// Zone ip struct info.
type Zone struct {
	ID          int64   `json:"id"`
	Addr        string  `json:"addr"`
	ISP         string  `json:"isp"`
	Country     string  `json:"country"`
	Province    string  `json:"province"`
	City        string  `json:"city"`
	Latitude    float64 `json:"latitude"`
	Longitude   float64 `json:"longitude"`
	CountryCode int     `json:"country_code,omitempty"`
}

// List struct info list.
type List struct {
	IPs []*IP
}

// New create Xip instance and return.
func New(path string) (list *List, err error) {
	var (
		ip   *IP
		file *os.File
		line []byte
	)
	list = new(List)
	if file, err = os.Open(path); err != nil {
		return
	}
	defer file.Close()
	reader := bufio.NewReader(file)
	for {
		if line, _, err = reader.ReadLine(); err != nil {
			if err == io.EOF {
				err = nil
				break
			}
			continue
		}
		lines := strings.Fields(string(line))
		if len(lines) < 13 {
			continue
		}
		// lines[2]:country  lines[3]:province  lines[4]:city  lines[5]:unit
		if lines[3] == "香港" || lines[3] == "澳门" || lines[3] == "台湾" {
			lines[2] = lines[3]
			lines[3] = lines[4]
			lines[4] = "*"
		}
		// ex.: from 中国 中国 *  to 中国 ”“ ”“
		if lines[2] == lines[3] || lines[3] == "*" {
			lines[3] = ""
			lines[4] = ""
		} else if lines[3] == lines[4] || lines[4] == "*" {
			// ex.: from 中国 北京 北京  to 中国 北京 ”“
			lines[4] = ""
		}
		ip = &IP{
			Begin:    InetAtoN(lines[0]),
			End:      InetAtoN(lines[1]),
			Country:  lines[2],
			Province: lines[3],
			City:     lines[4],
			ISP:      lines[6],
		}
		ip.Latitude, _ = strconv.ParseFloat(lines[7], 64)
		ip.Longitude, _ = strconv.ParseFloat(lines[8], 64)
		ip.CountryCode, _ = strconv.Atoi(lines[12])
		list.IPs = append(list.IPs, ip)
	}
	return
}

// IP ip zone info by ip
func (l *List) IP(ipStr string) (ip *IP) {
	addr := InetAtoN(ipStr)
	i, j := 0, len(l.IPs)
	for i < j {
		h := i + (j-i)/2 // avoid overflow when computing h
		ip = l.IPs[h]
		// i ≤ h < j
		if addr < ip.Begin {
			j = h
		} else if addr > ip.End {
			i = h + 1
		} else {
			break
		}
	}
	return
}

// All return ipInfos.
func (l *List) All() []*IP {
	return l.IPs
}

// ExternalIP get external ip.
func ExternalIP() (res []string) {
	inters, err := net.Interfaces()
	if err != nil {
		return
	}
	for _, inter := range inters {
		if !strings.HasPrefix(inter.Name, "lo") {
			addrs, err := inter.Addrs()
			if err != nil {
				continue
			}
			for _, addr := range addrs {
				if ipnet, ok := addr.(*net.IPNet); ok {
					if ipnet.IP.IsLoopback() || ipnet.IP.IsLinkLocalMulticast() || ipnet.IP.IsLinkLocalUnicast() {
						continue
					}
					if ip4 := ipnet.IP.To4(); ip4 != nil {
						switch true {
						case ip4[0] == 10:
							continue
						case ip4[0] == 172 && ip4[1] >= 16 && ip4[1] <= 31:
							continue
						case ip4[0] == 192 && ip4[1] == 168:
							continue
						default:
							res = append(res, ipnet.IP.String())
						}
					}
				}
			}
		}
	}
	return
}

// InternalIP get internal ip.
func InternalIP() string {
	inters, err := net.Interfaces()
	if err != nil {
		return ""
	}
	for _, inter := range inters {
		if !strings.HasPrefix(inter.Name, "lo") {
			addrs, err := inter.Addrs()
			if err != nil {
				continue
			}
			for _, addr := range addrs {
				if ipnet, ok := addr.(*net.IPNet); ok && !ipnet.IP.IsLoopback() {
					if ipnet.IP.To4() != nil {
						return ipnet.IP.String()
					}
				}
			}
		}
	}
	return ""
}

// InetAtoN conver ip addr to uint32.
func InetAtoN(s string) (sum uint32) {
	ip := net.ParseIP(s)
	if ip == nil {
		return
	}
	ip = ip.To4()
	if ip == nil {
		return
	}
	sum += uint32(ip[0]) << 24
	sum += uint32(ip[1]) << 16
	sum += uint32(ip[2]) << 8
	sum += uint32(ip[3])
	return sum
}

// InetNtoA conver uint32 to ip addr.
func InetNtoA(sum uint32) string {
	ip := make(net.IP, net.IPv4len)
	ip[0] = byte((sum >> 24) & 0xFF)
	ip[1] = byte((sum >> 16) & 0xFF)
	ip[2] = byte((sum >> 8) & 0xFF)
	ip[3] = byte(sum & 0xFF)
	return ip.String()
}




var (
	nativeEndian binary.ByteOrder

	ipv4PrivateCIDRString = []string{
		"0.0.0.0/8", "10.0.0.0/8", "100.64.0.0/10", "127.0.0.0/8", "169.254.0.0/16",
		"172.16.0.0/12", "192.0.0.0/24", "192.0.2.0/24", "192.88.99.0/24", "192.168.0.0/16",
		"198.18.0.0/15", "198.51.100.0/24", "203.0.113.0/24", "224.0.0.0/4", "240.0.0.0/4", "255.255.255.255/32",
	}
	ipv6PrivateCIDRString = []string{
		"::1/128", "::/128", "64:ff9b::/96", "::ffff:0:0/96", "100::/64", "2001::/23",
		"2001::/32", "2001:2::/48", "2001:db8::/32", "2001:10::/28", "2002::/16", "fc00::/7", "fe80::/10",
		"2001:20::/28", "ff00::/8",
	}

	ipv4PrivateCIDR []*net.IPNet
	ipv6PrivateCIDR []*net.IPNet
)

func init() {
	if nativeEndian == nil {
		var x uint32 = 0x01020304
		if *(*byte)(unsafe.Pointer(&x)) == 0x01 {
			nativeEndian = binary.BigEndian
		} else {
			nativeEndian = binary.LittleEndian
		}
	}

	for _, v := range ipv4PrivateCIDRString {
		_, n, _ := net.ParseCIDR(v)
		ipv4PrivateCIDR = append(ipv4PrivateCIDR, n)
	}

	for _, v := range ipv6PrivateCIDRString {
		_, n, _ := net.ParseCIDR(v)
		ipv6PrivateCIDR = append(ipv6PrivateCIDR, n)
	}
}

// IsReservedIP reports whether ip is private.
// Support ipv4/ipv6, refer rfc6890.
// Return <0 ip is invalid, =0 ip is public, >0 ip is private.
func IsReservedIP(ip string) int {
	addr := net.ParseIP(ip)
	if addr == nil {
		return -1
	}

	if addr.IsLoopback() || addr.IsMulticast() || addr.IsLinkLocalMulticast() || addr.IsLinkLocalUnicast() {
		return 1
	}

	if addr.To4() != nil {
		for _, v := range ipv4PrivateCIDR {
			if v.Contains(addr) {
				return 1
			}
		}
	} else {
		for _, v := range ipv6PrivateCIDR {
			if v.Contains(addr) {
				return 1
			}
		}
	}
	return 0
}

// Swap16 swap a 16 bit value if aren't big endian
func Swap16(i uint16) uint16 {
	return (i&0xff00)>>8 | (i&0xff)<<8
}

// Swap32 swap a 32 bit value if aren't big endian
func Swap32(i uint32) uint32 {
	return (i&0xff000000)>>24 | (i&0xff0000)>>8 | (i&0xff00)<<8 | (i&0xff)<<24
}

// Htons convert uint16 from host byte order to network byte order
func Htons(i uint16) uint16 {
	if GetNativeEndian() == binary.BigEndian {
		return i
	}
	// 大端模式, 高位放在低地址
	// 0x1234
	// 0x12 0x34
	return Swap16(i)
}

// Htonl convert uint32 from host byte order to network byte order.
func Htonl(i uint32) uint32 {
	if GetNativeEndian() == binary.BigEndian {
		return i
	}
	// 大端模式, 高位放在低地址
	// 0x12345678
	// 0x12 0x34 0x56 0x78
	return Swap32(i)
}

// Ntohs convert uint16 from network byte order to host byte order.
func Ntohs(i uint16) uint16 {
	if GetNativeEndian() == binary.BigEndian {
		return i
	}
	// 小端模式, 低位放在低地址
	// 0x1234
	// 0x34 0x12
	return Swap16(i)
}

// Ntohl convert uint32 from network byte order to host byte order.
func Ntohl(i uint32) uint32 {
	if GetNativeEndian() == binary.BigEndian {
		return i
	}
	// 小端模式, 低位放在低地址
	// 0x12345678
	// 0x78 0x56 0x34 0x12
	return Swap32(i)
}

// IPv4ToU32 convert ipv4(a.b.c.d) to uint32 in host byte order.
func IPv4ToU32(ip net.IP) uint32 {
	if ip == nil {
		return 0
	}
	a := uint32(ip[12])
	b := uint32(ip[13])
	c := uint32(ip[14])
	d := uint32(ip[15])
	return uint32(a<<24 | b<<16 | c<<8 | d)
}

// U32ToIPv4 convert uint32 to ipv4(a.b.c.d) in host byte order.
func U32ToIPv4(ip uint32) net.IP {
	a := byte((ip >> 24) & 0xFF)
	b := byte((ip >> 16) & 0xFF)
	c := byte((ip >> 8) & 0xFF)
	d := byte(ip & 0xFF)
	return net.IPv4(a, b, c, d)
}

// IPv4StrToU32 convert IPv4 string to uint32 in host byte order.
func IPv4StrToU32(s string) (ip uint32) {
	r := `^(\d{1,3})\.(\d{1,3})\.(\d{1,3})\.(\d{1,3})`
	reg, err := regexp.Compile(r)
	if err != nil {
		return
	}
	ips := reg.FindStringSubmatch(s)
	if ips == nil {
		return
	}

	ip1, _ := strconv.Atoi(ips[1])
	ip2, _ := strconv.Atoi(ips[2])
	ip3, _ := strconv.Atoi(ips[3])
	ip4, _ := strconv.Atoi(ips[4])

	if ip1 > 255 || ip2 > 255 || ip3 > 255 || ip4 > 255 {
		return
	}

	ip += uint32(ip1 * 0x1000000)
	ip += uint32(ip2 * 0x10000)
	ip += uint32(ip3 * 0x100)
	ip += uint32(ip4)
	return
}

// U32ToIPv4Str convert uint32 to IPv4 string in host byte order.
func U32ToIPv4Str(ip uint32) string {
	return fmt.Sprintf("%d.%d.%d.%d", ip>>24, ip<<8>>24, ip<<16>>24, ip<<24>>24)
}

// GetNativeEndian gets byte order for the current system.
func GetNativeEndian() binary.ByteOrder {
	return nativeEndian
}

// IsLittleEndian determines whether the host byte order is little endian.
func IsLittleEndian() bool {
	n := 0x1234
	return *(*byte)(unsafe.Pointer(&n)) == 0x34
}
