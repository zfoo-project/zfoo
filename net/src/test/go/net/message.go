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
	"fmt"
)

// Message struct
type Message struct {
	msgSize  int32
	msgID    int32
	data     []byte
}

// NewMessage create a new message
func NewMessage(msgID int32, data []byte) *Message {
	msg := &Message{
		msgSize: int32(len(data)) + 4 + 4,
		msgID:   msgID,
		data:    data,
	}
	return msg
}


func (msg *Message) String() string {
	return fmt.Sprintf("Size=%d ID=%d DataLen=%d", msg.msgSize, msg.msgID, len(msg.data))
}
