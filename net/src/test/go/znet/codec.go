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
)

// Encode from Packet to []byte
func Encode(packet any) *protocol.ByteBuffer {
	var buffer = new(protocol.ByteBuffer)
	buffer.WriteRawInt32(0)
	protocol.Write(buffer, packet)
	var writeOffset = buffer.WriteOffset()
	buffer.SetWriteOffset(0)
	buffer.WriteRawInt32(int32(writeOffset - 4))
	buffer.SetWriteOffset(writeOffset)
	return buffer
}

// Decode from []byte to Packet
func Decode(data []byte) any {
	var buffer = new(protocol.ByteBuffer)
	buffer.WriteUBytes(data)
	var packet = protocol.Read(buffer)
	return packet
}
