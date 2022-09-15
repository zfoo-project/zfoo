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
	"bytes"
	"context"
	"encoding/binary"
	"io"
	"net"
	"sync/atomic"
)

// Session struct
type Session struct {
	sid      uint64
	uid      uint64

	rawConn    net.Conn
	sendCh     chan []byte
	messageCh  chan any
	done       chan error
}

var uuid uint64

// NewSession create a new session
func NewSession(conn net.Conn) *Session {
	var suuid = atomic.AddUint64(&uuid, 1)

	session := &Session{
		sid:      suuid,
		uid:      0, // 可以为用户的id

		rawConn:    conn,
		sendCh:     make(chan []byte, 100),
		done:       make(chan error),
		messageCh:  make(chan any, 100),
	}

	return session
}



// Close close connection
func (session *Session) Close() {
	session.rawConn.Close()
}

// SendMessage send message
func (session *Session) SendMessage(msg any) error {
	var buffer = Encode(msg)
	session.sendCh <- buffer.ToBytes()
	return nil
}

// writeCoroutine write coroutine
func (session *Session) writeCoroutine(ctx context.Context) {
	for {
		select {
		case <-ctx.Done():
			return

		case pkt := <-session.sendCh:

			if pkt == nil {
				continue
			}

			if _, err := session.rawConn.Write(pkt); err != nil {
				session.done <- err
			}
		}
	}
}

// readCoroutine read coroutine
func (session *Session) readCoroutine(ctx context.Context) {

	for {
		select {
		case <-ctx.Done():
			return

		default:
			// 读取长度
			buf := make([]byte, 4)
			_, err := io.ReadFull(session.rawConn, buf)
			if err != nil {
				session.done <- err
				continue
			}

			bufReader := bytes.NewReader(buf)

			var dataSize int32
			err = binary.Read(bufReader, binary.BigEndian, &dataSize)
			if err != nil {
				session.done <- err
				continue
			}

			// 读取数据
			var bytes = make([]byte, dataSize)
			_, err = io.ReadFull(session.rawConn, bytes)
			if err != nil {
				session.done <- err
				continue
			}

			// 解码
			var packet = Decode(bytes)
			session.messageCh <- packet
		}
	}
}
