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
package znet

import (
	"fmt"
	protocol "gonet/goProtocol"
	"net"
	"testing"
	"time"
)

func TestServer(t *testing.T) {
	var host = "127.0.0.1:9000"

	var server = NewServer(host)
	server.onMessage = HandleMessage
	server.onConnect = HandleConnect
	server.onDisconnect = HandleDisconnect

	server.Start()
}

func TestClient(t *testing.T) {
	host := "127.0.0.1:9000"
	tcpAddr, _ := net.ResolveTCPAddr("tcp", host)

	conn, _ := net.DialTCP("tcp", nil, tcpAddr)

	var packet = new(protocol.TcpHelloRequest)
	packet.Message = "Hello, This is Golang Client"

	fmt.Println("send message")

	var buffer = Encode(packet)
	conn.Write(buffer.ToBytes())

	time.Sleep(time.Millisecond * 5000)
}

func HandleMessage(session *Session, packet any) {
	fmt.Println("receive packet")
	fmt.Println(packet)

	session.SendMessage(packet)
}

func HandleDisconnect(session *Session, err error) {
	fmt.Println("disconnect")
	fmt.Println(session.sid)
}

func HandleConnect(session *Session) {
	fmt.Println("connected.")
	fmt.Println(session.sid)
}

