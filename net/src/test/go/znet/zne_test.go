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
	protocol "gonet/goProtocol"
	"fmt"
	"net"
	"testing"
	"time"
)

func TestService(t *testing.T) {
	host := "127.0.0.1:18787"

	ss, err := NewSocketService(host)
	if err != nil {
		return
	}

	// ss.SetHeartBeat(5*time.Second, 30*time.Second)

	ss.RegMessageHandler(HandleMessage)
	ss.RegConnectHandler(HandleConnect)
	ss.RegDisconnectHandler(HandleDisconnect)

	go NewClientConnect()

	timer := time.NewTimer(time.Second * 1)
	go func() {
		<-timer.C
		ss.Stop("stop service")
		t.Log("service stoped")
	}()

	t.Log("service running on " + host)
	ss.Serv()
}

func HandleMessage(s *Session, msg *Packet) {
	fmt.Println("receive protocolId:", msg)
	fmt.Println("receive data:", string(msg.data))
}

func HandleDisconnect(s *Session, err error) {
	fmt.Println(s.conn.GetName() + " lost.")
}

func HandleConnect(s *Session) {
	fmt.Println(s.conn.GetName() + " connected.")
}

func NewClientConnect() {
	host := "127.0.0.1:18787"
	tcpAddr, err := net.ResolveTCPAddr("tcp", host)
	if err != nil {
		return
	}

	conn, err := net.DialTCP("tcp", nil, tcpAddr)
	if err != nil {
		return
	}

	var buffer = new(protocol.ByteBuffer)
	var packet = new(protocol.TcpHelloRequest)


	msg := NewMessage(1, []byte("Hello Zero!"))
	data, err := Encode(msg)
	if err != nil {
		return
	}
	conn.Write(data)
}
