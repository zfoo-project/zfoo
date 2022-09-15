package protocol

import (
	"bytes"
	"encoding/binary"
	"fmt"
	"math"
)

const initSize int = 128
const maxSize int = 655537

var initArray []byte = make([]byte, initSize, initSize)

type ByteBuffer struct {
	buffer     []byte
	writeIndex int
	readIndex  int
}

// -------------------------------------------------get/set-------------------------------------------------
func (byteBuffer *ByteBuffer) WriteOffset() int {
	return byteBuffer.writeIndex
}

func (byteBuffer *ByteBuffer) SetWriteOffset(writeIndex int) {
	if writeIndex > len(byteBuffer.buffer) {
		var error = fmt.Sprintf("writeIndex:[{%d}] out of bounds exception: readerIndex:[{%d}] , writerIndex:[{%d}] (expected: 0 <= readerIndex <= writerIndex <= capacity:[{%d}])", writeIndex, byteBuffer.readIndex, byteBuffer.writeIndex, len(byteBuffer.buffer))
		panic(error)
	}
	byteBuffer.writeIndex = writeIndex
}

func (byteBuffer *ByteBuffer) SetReadOffset(readIndex int) {
	if readIndex > byteBuffer.writeIndex {
		var error = fmt.Sprintf("readIndex:[{%d}] out of bounds exception: readerIndex:[{%d}] , writerIndex:[{%d}] (expected: 0 <= readerIndex <= writerIndex <= capacity:[{%d}])", readIndex, byteBuffer.readIndex, byteBuffer.writeIndex, len(byteBuffer.buffer))
		panic(error)
	}
	byteBuffer.readIndex = readIndex
}

func (byteBuffer *ByteBuffer) ToBytes() []byte {
	return byteBuffer.buffer[0:byteBuffer.writeIndex]
}

func (byteBuffer *ByteBuffer) ToString() string {
	return fmt.Sprintf("writeIndex:[{%d}], readIndex:[{%d}], len:[{%d}], cap:[{%d}]", byteBuffer.writeIndex, byteBuffer.readIndex, len(byteBuffer.buffer), cap(byteBuffer.buffer))
}

func (byteBuffer *ByteBuffer) GetCapacity() int {
	return len(byteBuffer.buffer) - byteBuffer.writeIndex
}

func (byteBuffer *ByteBuffer) EnsureCapacity(capacity int) {
	for {
		if byteBuffer.GetCapacity() > capacity {
			break
		}

		byteBuffer.buffer = append(byteBuffer.buffer, initArray...)

		if len(byteBuffer.buffer) > maxSize {
			panic("Bytebuf max size is [655537], out of memory error")
		}
	}
}

func (byteBuffer *ByteBuffer) Clear() {
	byteBuffer.writeIndex = 0
	byteBuffer.readIndex = 0
}

func (byteBuffer *ByteBuffer) IsReadable() bool {
	return byteBuffer.writeIndex > byteBuffer.readIndex
}

// -------------------------------------------------write/read-------------------------------------------------

// 整形转换成字节
func IntToBytes(n int) []byte {
	var x = int32(n)
	bytesBuffer := bytes.NewBuffer([]byte{})
	binary.Write(bytesBuffer, binary.BigEndian, x)
	return bytesBuffer.Bytes()
}

// 字节转换成整形
func BytesToInt(b []byte) int {
	bytesBuffer := bytes.NewBuffer(b)
	var x int32
	binary.Read(bytesBuffer, binary.BigEndian, &x)
	return int(x)
}

func (byteBuffer *ByteBuffer) WriteBool(value bool) {
	byteBuffer.EnsureCapacity(1)
	if value {
		byteBuffer.buffer[byteBuffer.writeIndex] = 1
	} else {
		byteBuffer.buffer[byteBuffer.writeIndex] = 0
	}
	byteBuffer.writeIndex++
}

func (byteBuffer *ByteBuffer) ReadBool() bool {
	var byteValue = byteBuffer.buffer[byteBuffer.readIndex]
	byteBuffer.readIndex++
	return byteValue == 1
}

func (byteBuffer *ByteBuffer) WriteByte(value int8) {
	byteBuffer.EnsureCapacity(1)
	byteBuffer.buffer[byteBuffer.writeIndex] = byte(value)
	byteBuffer.writeIndex++
}

func (byteBuffer *ByteBuffer) ReadByte() int8 {
	var byteValue = byteBuffer.buffer[byteBuffer.readIndex]
	byteBuffer.readIndex++
	return int8(byteValue)
}

func (byteBuffer *ByteBuffer) WriteUByte(value byte) {
	byteBuffer.EnsureCapacity(1)
	byteBuffer.buffer[byteBuffer.writeIndex] = value
	byteBuffer.writeIndex++
}

func (byteBuffer *ByteBuffer) ReadUByte() byte {
	var byteValue = byteBuffer.buffer[byteBuffer.readIndex]
	byteBuffer.readIndex++
	return byteValue
}

func (byteBuffer *ByteBuffer) WriteUBytes(bytes []byte) {
	var length = len(bytes)
	byteBuffer.EnsureCapacity(length)
	copy(byteBuffer.buffer[byteBuffer.writeIndex:], bytes)
	byteBuffer.writeIndex += length
}

func (byteBuffer *ByteBuffer) ReadUBytes(length int) []byte {
	var readOffset = byteBuffer.readIndex
	var endOffset = byteBuffer.readIndex + length
	var bytes = byteBuffer.buffer[readOffset:endOffset]
	byteBuffer.readIndex += length
	return bytes
}

func (byteBuffer *ByteBuffer) WriteShort(value int16) {
	byteBuffer.EnsureCapacity(2)
	var bytesBuffer = bytes.NewBuffer([]byte{})
	binary.Write(bytesBuffer, binary.BigEndian, value)
	var byteArray = bytesBuffer.Bytes()
	byteBuffer.WriteUBytes(byteArray)
}

func (byteBuffer *ByteBuffer) ReadShort() int16 {
	var byteArray = byteBuffer.ReadUBytes(2)
	bytesBuffer := bytes.NewBuffer(byteArray)
	var value int16
	binary.Read(bytesBuffer, binary.BigEndian, &value)
	return value
}

func (byteBuffer *ByteBuffer) WriteRawInt32(value int32) {
	byteBuffer.EnsureCapacity(4)
	var bytesBuffer = bytes.NewBuffer([]byte{})
	binary.Write(bytesBuffer, binary.BigEndian, value)
	var byteArray = bytesBuffer.Bytes()
	byteBuffer.WriteUBytes(byteArray)
}

func (byteBuffer *ByteBuffer) ReadRawInt32() int32 {
	var byteArray = byteBuffer.ReadUBytes(4)
	bytesBuffer := bytes.NewBuffer(byteArray)
	var value int32
	binary.Read(bytesBuffer, binary.BigEndian, &value)
	return value
}

func (byteBuffer *ByteBuffer) WriteInt(intValue int) {
	if intValue < math.MinInt32 || intValue > math.MaxInt32 {
		panic("intValue must range between math.MinInt32:-2147483648 and math.MaxInt32:2147483647")
	}
	byteBuffer.WriteInt32(int32(intValue))
}

func (byteBuffer *ByteBuffer) ReadInt() int {
	return int(byteBuffer.ReadInt32())
}

func (byteBuffer *ByteBuffer) WriteInt32(intValue int32) {
	var value uint32 = uint32(((intValue << 1) ^ (intValue >> 31)))
	// 右移操作>>是带符号右移
	if value>>7 == 0 {
		byteBuffer.WriteUByte(byte(value))
		return
	}

	if value>>14 == 0 {
		byteBuffer.WriteUByte(byte(value | 0x80))
		byteBuffer.WriteUByte(byte(value >> 7))
		return
	}

	if value>>21 == 0 {
		byteBuffer.WriteUByte(byte(value | 0x80))
		byteBuffer.WriteUByte(byte((value >> 7) | 0x80))
		byteBuffer.WriteUByte(byte(value >> 14))
		return
	}

	if value>>28 == 0 {
		byteBuffer.WriteUByte(byte(value | 0x80))
		byteBuffer.WriteUByte(byte((value >> 7) | 0x80))
		byteBuffer.WriteUByte(byte((value >> 14) | 0x80))
		byteBuffer.WriteUByte(byte(value >> 21))
		return
	}

	byteBuffer.WriteUByte(byte(value | 0x80))
	byteBuffer.WriteUByte(byte((value >> 7) | 0x80))
	byteBuffer.WriteUByte(byte((value >> 14) | 0x80))
	byteBuffer.WriteUByte(byte((value >> 21) | 0x80))
	byteBuffer.WriteUByte(byte(value >> 28))
}

func (byteBuffer *ByteBuffer) ReadInt32() int32 {
	var b byte = byteBuffer.ReadUByte()
	var value uint32 = uint32(b & 0x7F)
	if (b & 0x80) != 0 {
		b = byteBuffer.ReadUByte()
		value |= uint32(b&0x7F) << 7
		if (b & 0x80) != 0 {
			b = byteBuffer.ReadUByte()
			value |= uint32(b&0x7F) << 14
			if (b & 0x80) != 0 {
				b = byteBuffer.ReadUByte()
				value |= uint32(b&0x7F) << 21
				if (b & 0x80) != 0 {
					b = byteBuffer.ReadUByte()
					value |= uint32(b&0x7F) << 28
				}
			}
		}
	}

	return int32(value>>1) ^ -(int32(value & 1))
}

func (byteBuffer *ByteBuffer) WriteLong(longValue int64) {
	var value uint64 = uint64(((longValue << 1) ^ (longValue >> 63)))

	if value>>7 == 0 {
		byteBuffer.WriteUByte(byte(value))
		return
	}

	if value>>14 == 0 {
		byteBuffer.WriteUByte(byte(value | 0x80))
		byteBuffer.WriteUByte(byte(value >> 7))
		return
	}

	if value>>21 == 0 {
		byteBuffer.WriteUByte(byte(value | 0x80))
		byteBuffer.WriteUByte(byte((value >> 7) | 0x80))
		byteBuffer.WriteUByte(byte(value >> 14))
		return
	}

	if value>>28 == 0 {
		byteBuffer.WriteUByte(byte(value | 0x80))
		byteBuffer.WriteUByte(byte((value >> 7) | 0x80))
		byteBuffer.WriteUByte(byte(value>>14) | 0x80)
		byteBuffer.WriteUByte(byte(value >> 21))
		return
	}

	if value>>35 == 0 {
		byteBuffer.WriteUByte(byte(value | 0x80))
		byteBuffer.WriteUByte(byte((value >> 7) | 0x80))
		byteBuffer.WriteUByte(byte((value >> 14) | 0x80))
		byteBuffer.WriteUByte(byte((value >> 21) | 0x80))
		byteBuffer.WriteUByte(byte(value >> 28))
		return
	}

	if value>>42 == 0 {
		byteBuffer.WriteUByte(byte(value | 0x80))
		byteBuffer.WriteUByte(byte(value>>7) | 0x80)
		byteBuffer.WriteUByte(byte((value >> 14) | 0x80))
		byteBuffer.WriteUByte(byte((value >> 21) | 0x80))
		byteBuffer.WriteUByte(byte((value >> 28) | 0x80))
		byteBuffer.WriteUByte(byte(value >> 35))
		return
	}

	if value>>49 == 0 {
		byteBuffer.WriteUByte(byte(value | 0x80))
		byteBuffer.WriteUByte(byte((value >> 7) | 0x80))
		byteBuffer.WriteUByte(byte((value >> 14) | 0x80))
		byteBuffer.WriteUByte(byte((value >> 21) | 0x80))
		byteBuffer.WriteUByte(byte((value >> 28) | 0x80))
		byteBuffer.WriteUByte(byte((value >> 35) | 0x80))
		byteBuffer.WriteUByte(byte(value >> 42))
		return
	}

	if (value >> 56) == 0 {
		byteBuffer.WriteUByte(byte(value | 0x80))
		byteBuffer.WriteUByte(byte((value >> 7) | 0x80))
		byteBuffer.WriteUByte(byte((value >> 14) | 0x80))
		byteBuffer.WriteUByte(byte((value >> 21) | 0x80))
		byteBuffer.WriteUByte(byte((value >> 28) | 0x80))
		byteBuffer.WriteUByte(byte((value >> 35) | 0x80))
		byteBuffer.WriteUByte(byte((value >> 42) | 0x80))
		byteBuffer.WriteUByte(byte(value >> 49))
		return
	}

	byteBuffer.WriteUByte(byte(value | 0x80))
	byteBuffer.WriteUByte(byte((value >> 7) | 0x80))
	byteBuffer.WriteUByte(byte((value >> 14) | 0x80))
	byteBuffer.WriteUByte(byte((value >> 21) | 0x80))
	byteBuffer.WriteUByte(byte((value >> 28) | 0x80))
	byteBuffer.WriteUByte(byte((value >> 35) | 0x80))
	byteBuffer.WriteUByte(byte((value >> 42) | 0x80))
	byteBuffer.WriteUByte(byte((value >> 49) | 0x80))
	byteBuffer.WriteUByte(byte(value >> 56))
}

func (byteBuffer *ByteBuffer) ReadLong() int64 {
	var b byte = byteBuffer.ReadUByte()
	var value uint64 = uint64(b & 0x7F)
	if (b & 0x80) != 0 {
		b = byteBuffer.ReadUByte()
		value |= uint64(b&0x7F) << 7
		if (b & 0x80) != 0 {
			b = byteBuffer.ReadUByte()
			value |= uint64(b&0x7F) << 14
			if (b & 0x80) != 0 {
				b = byteBuffer.ReadUByte()
				value |= uint64(b&0x7F) << 21
				if (b & 0x80) != 0 {
					b = byteBuffer.ReadUByte()
					value |= uint64(b&0x7F) << 28
					if (b & 0x80) != 0 {
						b = byteBuffer.ReadUByte()
						value |= uint64(b&0x7F) << 35
						if (b & 0x80) != 0 {
							b = byteBuffer.ReadUByte()
							value |= uint64(b&0x7F) << 42
							if (b & 0x80) != 0 {
								b = byteBuffer.ReadUByte()
								value |= uint64(b&0x7F) << 49
								if (b & 0x80) != 0 {
									b = byteBuffer.ReadUByte()
									value |= uint64(b) << 56
								}
							}
						}
					}
				}
			}
		}
	}

	return int64(value>>1) ^ -(int64(value & 1))
}

func (byteBuffer *ByteBuffer) WriteFloat(value float32) {
	byteBuffer.EnsureCapacity(4)
	var bytesBuffer = bytes.NewBuffer([]byte{})
	binary.Write(bytesBuffer, binary.BigEndian, value)
	var byteArray = bytesBuffer.Bytes()
	byteBuffer.WriteUBytes(byteArray)
}

func (byteBuffer *ByteBuffer) ReadFloat() float32 {
	var byteArray = byteBuffer.ReadUBytes(4)
	bytesBuffer := bytes.NewBuffer(byteArray)
	var value float32
	binary.Read(bytesBuffer, binary.BigEndian, &value)
	return value
}

func (byteBuffer *ByteBuffer) WriteDouble(value float64) {
	byteBuffer.EnsureCapacity(8)
	var bytesBuffer = bytes.NewBuffer([]byte{})
	binary.Write(bytesBuffer, binary.BigEndian, value)
	var byteArray = bytesBuffer.Bytes()
	byteBuffer.WriteUBytes(byteArray)
}

func (byteBuffer *ByteBuffer) ReadDouble() float64 {
	var byteArray = byteBuffer.ReadUBytes(8)
	bytesBuffer := bytes.NewBuffer(byteArray)
	var value float64
	binary.Read(bytesBuffer, binary.BigEndian, &value)
	return value
}

func (byteBuffer *ByteBuffer) WriteString(value string) {
	var bytes []byte = []byte(value)
	var length = len(bytes)
	byteBuffer.EnsureCapacity(length)
	byteBuffer.WriteInt(length)
	byteBuffer.WriteUBytes(bytes)
}

func (byteBuffer *ByteBuffer) ReadString() string {
	var length = byteBuffer.ReadInt()
	var bytes = byteBuffer.ReadUBytes(length)
	return string(bytes[:])
}

func (byteBuffer *ByteBuffer) WriteChar(value string) {
	// 如果为空则写入一个默认的字符0
	if len(value) == 0 {
		byteBuffer.WriteInt(0)
		byteBuffer.WriteUByte(0)
		return
	}
	var char = value[0:1]
	byteBuffer.WriteString(char)
}

func (byteBuffer *ByteBuffer) ReadChar() string {
	return byteBuffer.ReadString()
}

func (byteBuffer *ByteBuffer) WritePacketFlag(packet any) bool {
	var flag = packet == nil
	byteBuffer.WriteBool(!flag)
	return flag
}

func (byteBuffer *ByteBuffer) WritePacket(packet any, protocolId int16) {
	var protocolRegistration = GetProtocol(protocolId)
	protocolRegistration.write(byteBuffer, packet)
}

func (byteBuffer *ByteBuffer) ReadPacket(protocolId int16) any {
	var protocolRegistration = GetProtocol(protocolId)
	return protocolRegistration.read(byteBuffer)
}

// -------------------------------------------------IProtocolRegistration-------------------------------------------------
type IProtocolRegistration interface {
	ProtocolId() int16

	write(buffer *ByteBuffer, packet any)

	read(buffer *ByteBuffer) any
}

// protocol map
var Protocols = make(map[int16]IProtocolRegistration)

func GetProtocol(protocolId int16) IProtocolRegistration {
	return Protocols[protocolId]
}

func Write(buffer *ByteBuffer, packet any) {
	var protocolId = packet.(IProtocolRegistration).ProtocolId()
	buffer.WriteShort(protocolId)
	var protocolRegistration = GetProtocol(protocolId)
	protocolRegistration.write(buffer, packet)
}

func Read(buffer *ByteBuffer) any {
	var protocolId = buffer.ReadShort()
	return GetProtocol(protocolId).read(buffer)
}

// -------------------------------------------------CutDown-------------------------------------------------
func (byteBuffer *ByteBuffer) WriteBooleanArray(array []bool) {
	if array == nil {
		byteBuffer.WriteInt(0)
	} else {
		byteBuffer.WriteInt(len(array))
		for _, value := range array {
			byteBuffer.WriteBool(value)
		}
	}
}

func (byteBuffer *ByteBuffer) ReadBooleanArray() []bool {
	var size = byteBuffer.ReadInt()
	var array = make([]bool, size)
	if size > 0 {
		for i := 0; i < size; i++ {
			array[i] = byteBuffer.ReadBool()
		}
	}
	return array
}

func (byteBuffer *ByteBuffer) WriteByteArray(array []int8) {
	if array == nil {
		byteBuffer.WriteInt(0)
	} else {
		byteBuffer.WriteInt(len(array))
		for _, value := range array {
			byteBuffer.WriteByte(value)
		}
	}
}

func (byteBuffer *ByteBuffer) ReadByteArray() []int8 {
	var size = byteBuffer.ReadInt()
	var array = make([]int8, size)
	if size > 0 {
		for i := 0; i < size; i++ {
			array[i] = byteBuffer.ReadByte()
		}
	}
	return array
}

func (byteBuffer *ByteBuffer) WriteShortArray(array []int16) {
	if array == nil {
		byteBuffer.WriteInt(0)
	} else {
		byteBuffer.WriteInt(len(array))
		for _, value := range array {
			byteBuffer.WriteShort(value)
		}
	}
}

func (byteBuffer *ByteBuffer) ReadShortArray() []int16 {
	var size = byteBuffer.ReadInt()
	var array = make([]int16, size)
	if size > 0 {
		for i := 0; i < size; i++ {
			array[i] = byteBuffer.ReadShort()
		}
	}
	return array
}

func (byteBuffer *ByteBuffer) WriteIntArray(array []int) {
	if array == nil {
		byteBuffer.WriteInt(0)
	} else {
		byteBuffer.WriteInt(len(array))
		for _, value := range array {
			byteBuffer.WriteInt(value)
		}
	}
}

func (byteBuffer *ByteBuffer) ReadIntArray() []int {
	var size = byteBuffer.ReadInt()
	var array = make([]int, size)
	if size > 0 {
		for i := 0; i < size; i++ {
			array[i] = byteBuffer.ReadInt()
		}
	}
	return array
}

func (byteBuffer *ByteBuffer) WriteLongArray(array []int64) {
	if array == nil {
		byteBuffer.WriteInt(0)
	} else {
		byteBuffer.WriteInt(len(array))
		for _, value := range array {
			byteBuffer.WriteLong(value)
		}
	}
}

func (byteBuffer *ByteBuffer) ReadLongArray() []int64 {
	var size = byteBuffer.ReadInt()
	var array = make([]int64, size)
	if size > 0 {
		for i := 0; i < size; i++ {
			array[i] = byteBuffer.ReadLong()
		}
	}
	return array
}

func (byteBuffer *ByteBuffer) WriteFloatArray(array []float32) {
	if array == nil {
		byteBuffer.WriteInt(0)
	} else {
		byteBuffer.WriteInt(len(array))
		for _, value := range array {
			byteBuffer.WriteFloat(value)
		}
	}
}

func (byteBuffer *ByteBuffer) ReadFloatArray() []float32 {
	var size = byteBuffer.ReadInt()
	var array = make([]float32, size)
	if size > 0 {
		for i := 0; i < size; i++ {
			array[i] = byteBuffer.ReadFloat()
		}
	}
	return array
}

func (byteBuffer *ByteBuffer) WriteDoubleArray(array []float64) {
	if array == nil {
		byteBuffer.WriteInt(0)
	} else {
		byteBuffer.WriteInt(len(array))
		for _, value := range array {
			byteBuffer.WriteDouble(value)
		}
	}
}

func (byteBuffer *ByteBuffer) ReadDoubleArray() []float64 {
	var size = byteBuffer.ReadInt()
	var array = make([]float64, size)
	if size > 0 {
		for i := 0; i < size; i++ {
			array[i] = byteBuffer.ReadDouble()
		}
	}
	return array
}

func (byteBuffer *ByteBuffer) WriteCharArray(array []string) {
	if array == nil {
		byteBuffer.WriteInt(0)
	} else {
		byteBuffer.WriteInt(len(array))
		for _, value := range array {
			byteBuffer.WriteChar(value)
		}
	}
}

func (byteBuffer *ByteBuffer) ReadCharArray() []string {
	var size = byteBuffer.ReadInt()
	var array = make([]string, size)
	if size > 0 {
		for i := 0; i < size; i++ {
			array[i] = byteBuffer.ReadChar()
		}
	}
	return array
}

func (byteBuffer *ByteBuffer) WriteStringArray(array []string) {
	if array == nil {
		byteBuffer.WriteInt(0)
	} else {
		byteBuffer.WriteInt(len(array))
		for _, value := range array {
			byteBuffer.WriteString(value)
		}
	}
}

func (byteBuffer *ByteBuffer) ReadStringArray() []string {
	var size = byteBuffer.ReadInt()
	var array = make([]string, size)
	if size > 0 {
		for i := 0; i < size; i++ {
			array[i] = byteBuffer.ReadString()
		}
	}
	return array
}

func (byteBuffer *ByteBuffer) WriteIntIntMap(m map[int]int) {
	if m == nil {
		byteBuffer.WriteInt(0)
	} else {
		byteBuffer.WriteInt(len(m))
		for key, value := range m {
			byteBuffer.WriteInt(key)
			byteBuffer.WriteInt(value)
		}
	}
}

func (byteBuffer *ByteBuffer) ReadIntIntMap() map[int]int {
	var size = byteBuffer.ReadInt()
	var m = make(map[int]int, size)
	if size > 0 {
		for i := 0; i < size; i++ {
			var key = byteBuffer.ReadInt()
			var value = byteBuffer.ReadInt()
			m[key] = value
		}
	}
	return m
}

func (byteBuffer *ByteBuffer) WriteIntLongMap(m map[int]int64) {
	if m == nil {
		byteBuffer.WriteInt(0)
	} else {
		byteBuffer.WriteInt(len(m))
		for key, value := range m {
			byteBuffer.WriteInt(key)
			byteBuffer.WriteLong(value)
		}
	}
}

func (byteBuffer *ByteBuffer) ReadIntLongMap() map[int]int64 {
	var size = byteBuffer.ReadInt()
	var m = make(map[int]int64, size)
	if size > 0 {
		for i := 0; i < size; i++ {
			var key = byteBuffer.ReadInt()
			var value = byteBuffer.ReadLong()
			m[key] = value
		}
	}
	return m
}

func (byteBuffer *ByteBuffer) WriteIntStringMap(m map[int]string) {
	if m == nil {
		byteBuffer.WriteInt(0)
	} else {
		byteBuffer.WriteInt(len(m))
		for key, value := range m {
			byteBuffer.WriteInt(key)
			byteBuffer.WriteString(value)
		}
	}
}

func (byteBuffer *ByteBuffer) ReadIntStringMap() map[int]string {
	var size = byteBuffer.ReadInt()
	var m = make(map[int]string, size)
	if size > 0 {
		for i := 0; i < size; i++ {
			var key = byteBuffer.ReadInt()
			var value = byteBuffer.ReadString()
			m[key] = value
		}
	}
	return m
}

func (byteBuffer *ByteBuffer) WriteLongIntMap(m map[int64]int) {
	if m == nil {
		byteBuffer.WriteInt(0)
	} else {
		byteBuffer.WriteInt(len(m))
		for key, value := range m {
			byteBuffer.WriteLong(key)
			byteBuffer.WriteInt(value)
		}
	}
}

func (byteBuffer *ByteBuffer) ReadLongIntMap() map[int64]int {
	var size = byteBuffer.ReadInt()
	var m = make(map[int64]int, size)
	if size > 0 {
		for i := 0; i < size; i++ {
			var key = byteBuffer.ReadLong()
			var value = byteBuffer.ReadInt()
			m[key] = value
		}
	}
	return m
}

func (byteBuffer *ByteBuffer) WriteLongLongMap(m map[int64]int64) {
	if m == nil {
		byteBuffer.WriteInt(0)
	} else {
		byteBuffer.WriteInt(len(m))
		for key, value := range m {
			byteBuffer.WriteLong(key)
			byteBuffer.WriteLong(value)
		}
	}
}

func (byteBuffer *ByteBuffer) ReadLongLongMap() map[int64]int64 {
	var size = byteBuffer.ReadInt()
	var m = make(map[int64]int64, size)
	if size > 0 {
		for i := 0; i < size; i++ {
			var key = byteBuffer.ReadLong()
			var value = byteBuffer.ReadLong()
			m[key] = value
		}
	}
	return m
}

func (byteBuffer *ByteBuffer) WriteLongStringMap(m map[int64]string) {
	if m == nil {
		byteBuffer.WriteInt(0)
	} else {
		byteBuffer.WriteInt(len(m))
		for key, value := range m {
			byteBuffer.WriteLong(key)
			byteBuffer.WriteString(value)
		}
	}
}

func (byteBuffer *ByteBuffer) ReadLongStringMap() map[int64]string {
	var size = byteBuffer.ReadInt()
	var m = make(map[int64]string, size)
	if size > 0 {
		for i := 0; i < size; i++ {
			var key = byteBuffer.ReadLong()
			var value = byteBuffer.ReadString()
			m[key] = value
		}
	}
	return m
}

func (byteBuffer *ByteBuffer) WriteStringIntMap(m map[string]int) {
	if m == nil {
		byteBuffer.WriteInt(0)
	} else {
		byteBuffer.WriteInt(len(m))
		for key, value := range m {
			byteBuffer.WriteString(key)
			byteBuffer.WriteInt(value)
		}
	}
}

func (byteBuffer *ByteBuffer) ReadStringIntMap() map[string]int {
	var size = byteBuffer.ReadInt()
	var m = make(map[string]int, size)
	if size > 0 {
		for i := 0; i < size; i++ {
			var key = byteBuffer.ReadString()
			var value = byteBuffer.ReadInt()
			m[key] = value
		}
	}
	return m
}

func (byteBuffer *ByteBuffer) WriteStringLongMap(m map[string]int64) {
	if m == nil {
		byteBuffer.WriteInt(0)
	} else {
		byteBuffer.WriteInt(len(m))
		for key, value := range m {
			byteBuffer.WriteString(key)
			byteBuffer.WriteLong(value)
		}
	}
}

func (byteBuffer *ByteBuffer) ReadStringLongMap() map[string]int64 {
	var size = byteBuffer.ReadInt()
	var m = make(map[string]int64, size)
	if size > 0 {
		for i := 0; i < size; i++ {
			var key = byteBuffer.ReadString()
			var value = byteBuffer.ReadLong()
			m[key] = value
		}
	}
	return m
}

func (byteBuffer *ByteBuffer) WriteStringStringMap(m map[string]string) {
	if m == nil {
		byteBuffer.WriteInt(0)
	} else {
		byteBuffer.WriteInt(len(m))
		for key, value := range m {
			byteBuffer.WriteString(key)
			byteBuffer.WriteString(value)
		}
	}
}

func (byteBuffer *ByteBuffer) ReadStringStringMap() map[string]string {
	var size = byteBuffer.ReadInt()
	var m = make(map[string]string, size)
	if size > 0 {
		for i := 0; i < size; i++ {
			var key = byteBuffer.ReadString()
			var value = byteBuffer.ReadString()
			m[key] = value
		}
	}
	return m
}
