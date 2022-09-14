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
	"encoding/binary"
	"fmt"
	"hash/adler32"
)

// Message struct
type Message struct {
	msgSize  int32
	msgID    int32
	data     []byte
	checksum uint32
}

// NewMessage create a new message
func NewMessage(msgID int32, data []byte) *Message {
	msg := &Message{
		msgSize: int32(len(data)) + 4 + 4,
		msgID:   msgID,
		data:    data,
	}

	msg.checksum = msg.calcChecksum()
	return msg
}

// GetData get message data
func (msg *Message) GetData() []byte {
	return msg.data
}

// GetID get message ID
func (msg *Message) GetID() int32 {
	return msg.msgID
}

// Verify verify checksum
func (msg *Message) Verify() bool {
	return msg.checksum == msg.calcChecksum()
}

func (msg *Message) calcChecksum() uint32 {
	if msg == nil {
		return 0
	}

	data := new(bytes.Buffer)

	err := binary.Write(data, binary.LittleEndian, msg.msgID)
	if err != nil {
		return 0
	}
	err = binary.Write(data, binary.LittleEndian, msg.data)
	if err != nil {
		return 0
	}

	checksum := adler32.Checksum(data.Bytes())
	return checksum
}

func (msg *Message) String() string {
	return fmt.Sprintf("Size=%d ID=%d DataLen=%d Checksum=%d", msg.msgSize, msg.GetID(), len(msg.GetData()), msg.checksum)
}
