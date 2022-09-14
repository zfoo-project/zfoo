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

package net

import (
	"bytes"
	"context"
	"encoding/binary"
	"io"
	"net"
	"time"
)

// Conn wrap net.Conn
type Conn struct {
	sid        string
	rawConn    net.Conn
	sendCh     chan []byte
	done       chan error
	hbTimer    *time.Timer
	name       string
	messageCh  chan *Message
	hbInterval time.Duration
	hbTimeout  time.Duration
}

// GetName Get conn name
func (c *Conn) GetName() string {
	return c.name
}

// NewConn create new conn
func NewConn(c net.Conn, hbInterval time.Duration, hbTimeout time.Duration) *Conn {
	conn := &Conn{
		rawConn:    c,
		sendCh:     make(chan []byte, 100),
		done:       make(chan error),
		messageCh:  make(chan *Message, 100),
		hbInterval: hbInterval,
		hbTimeout:  hbTimeout,
	}

	conn.name = c.RemoteAddr().String()
	conn.hbTimer = time.NewTimer(conn.hbInterval)

	if conn.hbInterval == 0 {
		conn.hbTimer.Stop()
	}

	return conn
}

// Close close connection
func (c *Conn) Close() {
	c.hbTimer.Stop()
	c.rawConn.Close()
}

// SendMessage send message
func (c *Conn) SendMessage(msg *Message) error {
	pkg, err := Encode(msg)
	if err != nil {
		return err
	}

	c.sendCh <- pkg
	return nil
}

// writeCoroutine write coroutine
func (c *Conn) writeCoroutine(ctx context.Context) {
	hbData := make([]byte, 0)

	for {
		select {
		case <-ctx.Done():
			return

		case pkt := <-c.sendCh:

			if pkt == nil {
				continue
			}

			if _, err := c.rawConn.Write(pkt); err != nil {
				c.done <- err
			}

		case <-c.hbTimer.C:
			hbMessage := NewMessage(MsgHeartbeat, hbData)
			c.SendMessage(hbMessage)
			// 设置心跳timer
			if c.hbInterval > 0 {
				c.hbTimer.Reset(c.hbInterval)
			}
		}
	}
}

// readCoroutine read coroutine
func (c *Conn) readCoroutine(ctx context.Context) {

	for {
		select {
		case <-ctx.Done():
			return

		default:
			// 设置超时
			if c.hbInterval > 0 {
				err := c.rawConn.SetReadDeadline(time.Now().Add(c.hbTimeout))
				if err != nil {
					c.done <- err
					continue
				}
			}
			// 读取长度
			buf := make([]byte, 4)
			_, err := io.ReadFull(c.rawConn, buf)
			if err != nil {
				c.done <- err
				continue
			}

			bufReader := bytes.NewReader(buf)

			var dataSize int32
			err = binary.Read(bufReader, binary.LittleEndian, &dataSize)
			if err != nil {
				c.done <- err
				continue
			}

			// 读取数据
			databuf := make([]byte, dataSize)
			_, err = io.ReadFull(c.rawConn, databuf)
			if err != nil {
				c.done <- err
				continue
			}

			// 解码
			msg, err := Decode(databuf)
			if err != nil {
				c.done <- err
				continue
			}

			if msg.GetID() == MsgHeartbeat {
				continue
			}

			c.messageCh <- msg
		}
	}
}
