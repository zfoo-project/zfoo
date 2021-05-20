// TODO: go无法支持嵌套的集合类型，所以现在无法支持这套协议
// Golang字节保存在内存的低地址中默认的使用的是大端模式，和Java一样
// 右移操作>>是带符号右移
package buffer

import (
	"math"
	"strconv"
	"strings"
)

const initSize int = 128
const masSize int = 655537

const maxInt int = 2147483647
const minInt int = -2147483648

var initArray []byte = make([]byte, initSize, initSize)

type ByteBuffer struct {
	buffer      []byte
	writeOffset int
	readOffset  int
}

// -------------------------------------------------get/set-------------------------------------------------
func (byteBuffer *ByteBuffer) WriteOffset() int {
	return byteBuffer.writeOffset
}

func (byteBuffer *ByteBuffer) SetWriteOffset(writeIndex int) {
	if writeIndex > len(byteBuffer.buffer) {
		var builder strings.Builder
		builder.WriteString("writeIndex[")
		builder.WriteString(strconv.Itoa(writeIndex))
		builder.WriteString("] out of bounds exception: readerIndex: ")
		builder.WriteString(strconv.Itoa(byteBuffer.readOffset))
		builder.WriteString(", writerIndex: ")
		builder.WriteString(strconv.Itoa(byteBuffer.writeOffset))
		builder.WriteString("(expected: 0 <= readerIndex <= writerIndex <= capacity:")
		builder.WriteString(strconv.Itoa(len(byteBuffer.buffer)))
		builder.WriteString(")")
		panic(builder.String())
	}
	byteBuffer.writeOffset = writeIndex
}

func (byteBuffer *ByteBuffer) SetReadOffset(readIndex int) {
	if readIndex > byteBuffer.writeOffset {
		var builder strings.Builder
		builder.WriteString("readIndex[")
		builder.WriteString(strconv.Itoa(readIndex))
		builder.WriteString("] out of bounds exception: readerIndex: ")
		builder.WriteString(strconv.Itoa(byteBuffer.readOffset))
		builder.WriteString(", writerIndex: ")
		builder.WriteString(strconv.Itoa(byteBuffer.writeOffset))
		builder.WriteString("(expected: 0 <= readerIndex <= writerIndex <= capacity:")
		builder.WriteString(strconv.Itoa(len(byteBuffer.buffer)))
		builder.WriteString(")")
		panic(builder.String())
	}
	byteBuffer.readOffset = readIndex
}

func (byteBuffer *ByteBuffer) ToBytes() []byte {
	return byteBuffer.buffer[0:byteBuffer.writeOffset]
}

func (byteBuffer *ByteBuffer) ToString() string {
	var builder strings.Builder
	builder.WriteString("writeOffset:")
	builder.WriteString(strconv.Itoa(byteBuffer.writeOffset))
	builder.WriteString(", readOffset:")
	builder.WriteString(strconv.Itoa(byteBuffer.readOffset))
	builder.WriteString(", len:")
	builder.WriteString(strconv.Itoa(len(byteBuffer.buffer)))
	builder.WriteString(", cap:")
	builder.WriteString(strconv.Itoa(cap(byteBuffer.buffer)))
	return builder.String()
}

// -------------------------------------------------write/read-------------------------------------------------
func (byteBuffer *ByteBuffer) GetCapacity() int {
	return len(byteBuffer.buffer) - byteBuffer.writeOffset
}

func (byteBuffer *ByteBuffer) EnsureCapacity(capacity int) {
	for
	{
		if byteBuffer.GetCapacity()-capacity > 0 {
			break
		}

		byteBuffer.buffer = append(byteBuffer.buffer, initArray...)

		if len(byteBuffer.buffer) > masSize {
			panic("Bytebuf max size is [655537], out of memory error")
		}
	}
}

func (byteBuffer *ByteBuffer) WriteBool(value bool) {
	byteBuffer.EnsureCapacity(1)
	if value {
		byteBuffer.buffer[byteBuffer.writeOffset] = 1
	} else {
		byteBuffer.buffer[byteBuffer.writeOffset] = 0
	}
	byteBuffer.writeOffset++
}

func (byteBuffer *ByteBuffer) ReadBool() bool {
	var byteValue = byteBuffer.buffer[byteBuffer.readOffset]
	byteBuffer.readOffset++
	return byteValue == 1
}

func (byteBuffer *ByteBuffer) WriteUByte(value byte) {
	byteBuffer.EnsureCapacity(1)
	byteBuffer.buffer[byteBuffer.writeOffset] = value
	byteBuffer.writeOffset++
}

func (byteBuffer *ByteBuffer) ReadUByte() byte {
	var byteValue = byteBuffer.buffer[byteBuffer.readOffset]
	byteBuffer.readOffset++
	return byteValue
}

func (byteBuffer *ByteBuffer) WriteUBytes(bytes []byte) {
	var length = len(bytes)
	byteBuffer.EnsureCapacity(length)
	copy(byteBuffer.buffer[byteBuffer.writeOffset:], bytes)
	byteBuffer.writeOffset += length
}

func (byteBuffer *ByteBuffer) ReadUBytes(length int) []byte {
	var readOffset = byteBuffer.readOffset
	var endOffset = byteBuffer.readOffset + length
	var bytes = byteBuffer.buffer[readOffset:endOffset]
	return bytes
}

func (byteBuffer *ByteBuffer) WriteShort(value int16) {
	byteBuffer.EnsureCapacity(2)
	byteBuffer.WriteUByte(byte(value >> 8))
	byteBuffer.WriteUByte(byte(value))
}

func (byteBuffer *ByteBuffer) ReadShort() int16 {
	return (int16(byteBuffer.ReadUByte()) << 8) | int16(byteBuffer.ReadUByte())
}

func (byteBuffer *ByteBuffer) WriteRawInt32(intValue int32) {
	byteBuffer.WriteUByte(byte(intValue >> 24))
	byteBuffer.WriteUByte(byte(intValue >> 16))
	byteBuffer.WriteUByte(byte(intValue >> 8))
	byteBuffer.WriteUByte(byte(intValue))
}

func (byteBuffer *ByteBuffer) ReadRawInt32() int32 {
	return int32(uint32(byteBuffer.ReadUByte())<<24 | uint32(byteBuffer.ReadUByte())<<16 | uint32(byteBuffer.ReadUByte())<<8 | uint32(byteBuffer.ReadUByte()))
}

func (byteBuffer *ByteBuffer) WriteInt(intValue int) {
	if intValue < minInt || intValue > maxInt {
		panic("intValue must range between minInt:-2147483648 and maxInt:2147483647")
	}
	byteBuffer.WriteInt32(int32(intValue))
}

func (byteBuffer *ByteBuffer) ReadInt() int {
	return int(byteBuffer.ReadInt32())
}

func (byteBuffer *ByteBuffer) WriteInt32(intValue int32) {
	var value uint32 = uint32(((intValue << 1) ^ (intValue >> 31)))

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
	var uintValue = math.Float32bits(value)
	byteBuffer.WriteRawInt32(int32(uintValue))
}

func (byteBuffer *ByteBuffer) ReadFloat() float32 {
	var int32Value = byteBuffer.ReadRawInt32()
	return math.Float32frombits(uint32(int32Value))
}

func (byteBuffer *ByteBuffer) WriteDouble(value float64) {
	byteBuffer.EnsureCapacity(4)
	var uintValue = math.Float64bits(value)
	byteBuffer.WriteRawInt32(int32(uintValue >> 32))
	byteBuffer.WriteRawInt32(int32(uintValue))
}

func (byteBuffer *ByteBuffer) ReadDouble() float64 {
	var highInt32Value = uint64(byteBuffer.ReadRawInt32())
	var lowInt32Value = uint64(byteBuffer.ReadRawInt32())
	return math.Float64frombits(highInt32Value<<32 | lowInt32Value)
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