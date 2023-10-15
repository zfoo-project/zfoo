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

package protocol

import (
	"fmt"
	"math"
	"os"
	"testing"
)

func assert(flag bool) {
	if !flag {
		panic("exception happen")
	}
}

func TestByteBuffer(t *testing.T) {
	var buffer = new(ByteBuffer)
	boolTest(buffer)
	byteTest(buffer)
	bytesTest(buffer)
	shortTest(buffer)
	intTest(buffer)
	longTest(buffer)
	floatTest(buffer)
	doubleTest(buffer)
	stringTest(buffer)
	packetTest()
}

func boolTest(buffer *ByteBuffer) {
	var value = true
	buffer.WriteBool(value)
	assert(buffer.ReadBool() == value)
	value = false
	buffer.WriteBool(value)
	assert(buffer.ReadBool() == value)
}

func byteTest(buffer *ByteBuffer) {
	var byteValues = []byte{127, 0, 255}
	for _, value := range byteValues {
		buffer.WriteUByte(value)
		assert(buffer.ReadUByte() == value)
	}

	var int8Values = []int8{127, 0, -128}
	for _, value := range int8Values {
		buffer.WriteByte(value)
		assert(buffer.ReadByte() == value)
	}
}

func bytesTest(buffer *ByteBuffer) {
	var bytes = []byte{127, 0, 255}
	buffer.WriteUBytes(bytes)
	var readBytes = buffer.ReadUBytes(len(bytes))
	for i, value := range bytes {
		assert(readBytes[i] == value)
	}
}

func shortTest(buffer *ByteBuffer) {
	var shortValues = []int16{-32768, -100, -2, -1, 0, 1, 2, 100, 32767}
	for _, value := range shortValues {
		buffer.WriteShort(value)
		var readValue = buffer.ReadShort()
		assert(readValue == value)
	}
}

func intTest(buffer *ByteBuffer) {
	var intValues = []int{math.MinInt32, -99999999, -32768, -100, -2, -1, 0, 1, 2, 100, 32767, 99999999, math.MaxInt32}
	for _, value := range intValues {
		buffer.WriteInt(value)
		var readValue = buffer.ReadInt()
		assert(readValue == value)
	}
}

func longTest(buffer *ByteBuffer) {
	var longValues = []int64{math.MinInt64, -99999999, -32768, -100, -2, -1, 0, 1, 2, 100, 32767, 99999999, math.MaxInt64}
	for _, value := range longValues {
		buffer.WriteLong(value)
		var readValue = buffer.ReadLong()
		assert(readValue == value)
	}
}

func floatTest(buffer *ByteBuffer) {
	var floatValues = []float32{-12345678.12345678, -1234.5678, -100, -2, -1, 0, 1, 2, 100, 1234.5678, math.MaxFloat32}
	for _, value := range floatValues {
		buffer.WriteFloat(value)
		var readValue = buffer.ReadFloat()
		assert(math.Abs(float64(readValue-value)) < 0.01)
	}
}

func doubleTest(buffer *ByteBuffer) {
	var doubleValues = []float64{-12345678.12345678, -1234.5678, -100, -2, -1, 0, 1, 2, 100, 1234.5678, math.MaxFloat64}
	for _, value := range doubleValues {
		buffer.WriteDouble(value)
		var readValue = buffer.ReadDouble()
		assert(math.Abs(float64(readValue-value)) < 0.01)
	}
}

func stringTest(buffer *ByteBuffer) {
	var value = "hello world!"
	buffer.WriteString(value)
	assert(buffer.ReadString() == value)
}

func packetTest() {
	//bytes, _ := os.ReadFile("D:\\github\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-no-compatible.bytes")
	//bytes, _ := os.ReadFile("D:\\github\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-out-compatible.bytes")
	//bytes, _ := os.ReadFile("D:\\github\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-inner-compatible.bytes")
	//bytes, _ := os.ReadFile("D:\\github\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-out-inner-compatible.bytes")
	bytes, _ := os.ReadFile("D:\\github\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-out-inner-inner-compatible.bytes")
	var buffer = new(ByteBuffer)
	buffer.WriteUBytes(bytes)
	var packet = Read(buffer)
	fmt.Println(packet)
	fmt.Println("source size ", buffer.WriteOffset())

	buffer.Clear()
	Write(buffer, packet)
	var newPacket = Read(buffer)
	fmt.Println("target size ", buffer.WriteOffset())
	fmt.Println(newPacket)
}
