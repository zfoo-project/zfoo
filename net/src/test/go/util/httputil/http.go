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
package httputil

import (
	"bytes"
	"encoding/binary"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net"
	"net/http"
	"net/url"
	"strings"
	"time"
)

func PostWithHeader(url string, msg []byte, headers map[string]string) (string, error) {
	client := &http.Client{}

	req, err := http.NewRequest("POST", url, strings.NewReader(string(msg)))
	if err != nil {
		return "", err
	}
	for key, header := range headers {
		req.Header.Set(key, header)
	}
	resp, err := client.Do(req)
	defer resp.Body.Close()
	body, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		return "", err
	}
	return string(body), nil
}

func PostWithAuthorization(url, authorization string, msg []byte) (string, error) {
	headers := make(map[string]string)
	headers["Authorization"] = authorization
	headers["Content-Type"] = "application/json"
	return PostWithHeader(url, msg, headers)
}



const (
	CONN_TIME_OUT = time.Second * 2
)

func Get(apiURL string, params url.Values) (resData string, e error) {
	var (
		Url *url.URL
		err error
	)
	Url, err = url.Parse(apiURL)
	if err != nil {
		return "", err
	}
	Url.RawQuery = params.Encode()
	resp, err := http.Get(Url.String())
	if err != nil {
		return "", err
	}
	defer resp.Body.Close()
	res, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		return "", err
	}
	return string(res), nil
}

/**
Get with TimeOut
Code == 200 则返回,其他请打印错误
第三个参数设置超时  单位:秒 【0:则默认两秒】
*/
func SendGetWithTimeOut(apiUrl string, params url.Values, time_out int) (resData string, e error) {
	var (
		Url *url.URL
		err error
		b   []byte
	)
	Url, err = url.Parse(apiUrl)
	if err != nil {
		return "", err
	}
	Url.RawQuery = params.Encode()
	client := _httpClient(time_out)
	resp, err := client.Get(Url.String())
	if err != nil || resp == nil {
		return "", err
	}
	defer resp.Body.Close()
	if resp.StatusCode != http.StatusOK {
		return "", nil
	}
	if resp.Body != nil {
		b, err = ioutil.ReadAll(resp.Body)
		return string(b), nil
	}
	return "", err
}

func _httpClient(time_out int) http.Client {
	var (
		_sec time.Duration
	)
	_sec = CONN_TIME_OUT
	if time_out > 0 {
		_sec = time.Second * time.Duration(time_out)
	}
	return http.Client{
		Transport: &http.Transport{
			Dial: func(netw, addr string) (net.Conn, error) {
				conn, err := net.DialTimeout(netw, addr, _sec)
				if err != nil {
					return nil, err
				}
				conn.SetDeadline(time.Now().Add(_sec))
				return conn, nil
			},
			ResponseHeaderTimeout: _sec,
		},
	}
}

/**
网络请求POST
*/
func Post(apiURL string, params url.Values) (resData string, err error) {
	resp, err := http.PostForm(apiURL, params)
	if err != nil {
		return "", err
	}
	defer resp.Body.Close()
	res, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		return "", err
	}
	return string(res), nil
}

/**
Post with TimeOut
Code == 200 则返回,其他请打印错误
第三个参数设置超时  单位:秒 【0:则默认两秒】
*/
func SendPostWithTimeOut(apiUrl string, params url.Values, time_out int) (resData string, e error) {
	var (
		err error
		b   []byte
	)
	client := _httpClient(time_out)
	resp, err := client.PostForm(apiUrl, params)
	if err != nil || resp == nil {
		return "", err
	}
	defer resp.Body.Close()
	if resp.StatusCode != http.StatusOK {
		return "", nil
	}
	if resp.Body != nil {
		b, err = ioutil.ReadAll(resp.Body)
		return string(b), nil
	}
	return "", err
}



//OnPostJSON 发送修改密码
func OnPostJSON(url, jsonstr string) []byte {
	//解析这个 URL 并确保解析没有出错。
	body := bytes.NewBuffer([]byte(jsonstr))
	resp, err := http.Post(url, "application/json;charset=utf-8", body)
	if err != nil {
		return []byte("")
	}
	defer resp.Body.Close()
	body1, err1 := ioutil.ReadAll(resp.Body)
	if err1 != nil {
		return []byte("")
	}

	return body1
}

//OnGetJSON 发送get 请求
func OnGetJSON(url, params string) string {
	//解析这个 URL 并确保解析没有出错。
	var urls = url
	if len(params) > 0 {
		urls += "?" + params
	}
	resp, err := http.Get(urls)
	if err != nil {
		return ""
	}
	defer resp.Body.Close()
	body1, err1 := ioutil.ReadAll(resp.Body)
	if err1 != nil {
		return ""
	}

	return string(body1)
}

//SendGet 发送get 请求 返回对象
func SendGet(url, params string, obj interface{}) bool {
	//解析这个 URL 并确保解析没有出错。
	var urls = url
	if len(params) > 0 {
		urls += "?" + params
	}
	resp, err := http.Get(urls)
	if err != nil {
		return false
	}
	defer resp.Body.Close()
	body, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		return false
	}
	//log.Println((string(body)))
	err = json.Unmarshal([]byte(body), &obj)
	if err != nil {
		return false
	}

	return true
}

//SendGetEx 发送GET请求
func SendGetEx(url string, reponse interface{}) bool {
	resp, e := http.Get(url)
	if e != nil {
		return false
	}
	defer resp.Body.Close()
	body, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		return false
	}
	err = json.Unmarshal(body, &reponse)
	if err != nil {
		return false
	}

	return true
}

//OnPostForm form 方式发送post请求
func OnPostForm(url string, data url.Values) (body []byte) {
	resp, err := http.PostForm(url, data)
	if err != nil {
		return
	}
	defer resp.Body.Close()
	body, err = ioutil.ReadAll(resp.Body)
	if err != nil {
		return
	}

	return
}

//SendPost 发送POST请求
func SendPost(requestBody interface{}, responseBody interface{}, url string) bool {
	postData, err := json.Marshal(requestBody)
	client := &http.Client{}
	req, _ := http.NewRequest("POST", url, bytes.NewReader(postData))
	req.Header.Add("Accept", "application/json")
	req.Header.Add("Content-Type", "application/json;charset=utf-8")
	//	req.Header.Add("Authorization", authorization)
	resp, e := client.Do(req)
	if e != nil {
		return false
	}
	defer resp.Body.Close()

	body, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		return false
	}
	//	result := string(body)

	err = json.Unmarshal(body, &responseBody)
	if err != nil {
		return false
	}

	return true
}

//WriteJSON  像指定client 发送json 包
//msg message.MessageBody
func WriteJSON(w http.ResponseWriter, msg interface{}) {
	w.Header().Set("Content-Type", "application/json; charset=utf-8")
	js, err := json.Marshal(msg)
	if err != nil {
		panic(err)
	}
	fmt.Fprintf(w, string(js))
}


// --------------------------------------------------------------------------------------------------------
const (
	XForwardedFor = "X-Forwarded-For"
	XRealIP       = "X-Real-IP"
)

// RemoteIp 返回远程客户端的 IP，如 192.168.1.1
func RemoteIp(req *http.Request) string {
	remoteAddr := req.RemoteAddr
	if ip := req.Header.Get(XRealIP); ip != "" {
		remoteAddr = ip
	} else if ip = req.Header.Get(XForwardedFor); ip != "" {
		remoteAddr = ip
	} else {
		remoteAddr, _, _ = net.SplitHostPort(remoteAddr)
	}

	if remoteAddr == "::1" {
		remoteAddr = "127.0.0.1"
	}

	return remoteAddr
}

// Ip2long 将 IPv4 字符串形式转为 uint32
func Ip2long(ipstr string) uint32 {
	ip := net.ParseIP(ipstr)
	if ip == nil {
		return 0
	}
	ip = ip.To4()
	return binary.BigEndian.Uint32(ip)
}

// 获取本机网卡IP
func GetLocalIP() (ipv4 string, err error) {
	var (
		addrs   []net.Addr
		addr    net.Addr
		ipNet   *net.IPNet // IP地址
		isIpNet bool
	)
	// 获取所有网卡
	if addrs, err = net.InterfaceAddrs(); err != nil {
		return
	}
	// 取第一个非lo的网卡IP
	for _, addr = range addrs {
		// 这个网络地址是IP地址: ipv4, ipv6
		if ipNet, isIpNet = addr.(*net.IPNet); isIpNet && !ipNet.IP.IsLoopback() {
			// 跳过IPV6
			if ipNet.IP.To4() != nil {
				ipv4 = ipNet.IP.String() // 192.168.1.1
				return
			}
		}
	}

	return
}
