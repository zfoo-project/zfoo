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
	"encoding/binary"
)

// Encode from Packet to []byte
func Encode(msg *Packet) ([]byte, error) {
	buffer := new(bytes.Buffer)

	err := binary.Write(buffer, binary.LittleEndian, msg.length)
	if err != nil {
		return nil, err
	}
	err = binary.Write(buffer, binary.LittleEndian, msg.protocolId)
	if err != nil {
		return nil, err
	}
	err = binary.Write(buffer, binary.LittleEndian, msg.data)
	if err != nil {
		return nil, err
	}
	return buffer.Bytes(), nil
}

// Decode from []byte to Packet
func Decode(data []byte) (*Packet, error) {
	bufReader := bytes.NewReader(data)

	dataSize := len(data)
	// 读取消息ID
	var protocolId int16
	err := binary.Read(bufReader, binary.LittleEndian, &protocolId)
	if err != nil {
		return nil, err
	}

	// 读取数据
	dataBufLength := dataSize - 2 - 4
	dataBuf := make([]byte, dataBufLength)
	err = binary.Read(bufReader, binary.LittleEndian, &dataBuf)
	if err != nil {
		return nil, err
	}

	message := &Packet{}
	message.length = int32(dataSize)
	message.protocolId = protocolId
	message.data = dataBuf

	return message, nil
}
