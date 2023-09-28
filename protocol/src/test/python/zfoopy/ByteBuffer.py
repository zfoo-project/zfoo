import struct
from . import ProtocolManager

maxSize = 655537
maxInt = 2147483647
minInt = -2147483648
maxLong = 9223372036854775807
minLong = -9223372036854775808
empty_str = ""


class ByteBuffer():
    writeOffset = 0
    readOffset = 0
    buffer = bytearray(256)

    def adjustPadding(self, predictionLength, beforeWriteIndex):
        currentWriteIndex = self.writeOffset
        predictionCount = self.writeIntCount(predictionLength)
        length = currentWriteIndex - beforeWriteIndex - predictionCount
        lengthCount = self.writeIntCount(length)
        padding = lengthCount - predictionCount
        if (padding == 0):
            self.setWriteOffset(beforeWriteIndex)
            self.writeInt(length)
            self.setWriteOffset(currentWriteIndex)
        else:
            retainedByteBuf = self.buffer[(currentWriteIndex - length):currentWriteIndex]
            self.setWriteOffset(beforeWriteIndex)
            self.writeInt(length)
            self.writeBytes(retainedByteBuf)
        pass

    def compatibleRead(self, beforeReadIndex, length):
        return length != -1 and self.getReadOffset() < length + beforeReadIndex

    def getWriteOffset(self):
        return self.writeOffset

    def setWriteOffset(self, writeOffset):
        if writeOffset > len(self.buffer):
            raise ValueError("index out of bounds exception:readerIndex:" + str(self.readOffset) +
                             ", writerIndex:" + str(self.writeOffset) +
                             "(expected:0 <= readerIndex <= writerIndex <= capacity:" + str(len(self.buffer)))
        self.writeOffset = writeOffset
        pass

    def getReadOffset(self):
        return self.readOffset

    def setReadOffset(self, readOffset):
        if readOffset > self.writeOffset:
            raise ValueError("index out of bounds exception:readerIndex:" + str(self.readOffset) +
                             ", writerIndex:" + str(self.writeOffset) +
                             "(expected:0 <= readerIndex <= writerIndex <= capacity:" + str(len(self.buffer)))
        self.readOffset = readOffset
        pass

    def isReadable(self):
        return self.writeOffset > self.readOffset
        pass

    def getCapacity(self):
        return len(self.buffer) - self.writeOffset

    def ensureCapacity(self, minCapacity):
        while (minCapacity - self.getCapacity() > 0):
            newSize = len(self.buffer) * 2
            if newSize > maxSize:
                raise ValueError("out of memory error")
            # auto grow capacity
            self.buffer.extend(bytearray(len(self.buffer)))

    def clear(self):
        self.writeOffset = 0
        self.readOffset = 0
        self.buffer = bytearray(256)
        pass

    def toBytes(self):
        return self.buffer[0:self.writeOffset]

    def writeBool(self, value):
        if value:
            self.writeByte(1)
        else:
            self.writeByte(0)
        pass

    def readBool(self):
        if self.readByte():
            return True
        else:
            return False

    def writeByte(self, value):
        self.ensureCapacity(1)
        struct.pack_into('>b', self.buffer, self.writeOffset, value)
        self.writeOffset += 1
        pass

    def readByte(self):
        value = struct.unpack_from('>b', self.buffer, self.readOffset)[0]
        self.readOffset += 1
        return value

    def writeBytes(self, value):
        length = len(value)
        self.ensureCapacity(length)
        self.buffer[self.writeOffset:length] = value[:]
        self.writeOffset += length
        pass

    def writeUByte(self, value):
        self.ensureCapacity(1)
        struct.pack_into('>B', self.buffer, self.writeOffset, value)
        self.writeOffset += 1
        pass

    def readUByte(self):
        value = struct.unpack_from('>B', self.buffer, self.readOffset)[0]
        self.readOffset += 1
        return value

    def writeShort(self, value):
        self.ensureCapacity(2)
        struct.pack_into('>h', self.buffer, self.writeOffset, value)
        self.writeOffset += 2
        pass

    def readShort(self):
        value = struct.unpack_from('>h', self.buffer, self.readOffset)[0]
        self.readOffset += 2
        return value

    def writeRawInt(self, value):
        self.ensureCapacity(4)
        struct.pack_into('>i', self.buffer, self.writeOffset, value)
        self.writeOffset += 4
        pass

    def readRawInt(self):
        value = struct.unpack_from('>i', self.buffer, self.readOffset)[0]
        self.readOffset += 4
        return value

    def writeInt(self, value):
        if not (minInt <= value <= maxInt):
            raise ValueError('value must range between minInt:-2147483648 and maxInt:2147483647')
        value = (value << 1) ^ (value >> 31)
        self.ensureCapacity(5)
        if value >> 7 == 0:
            self.writeUByte(value)
            return

        if value >> 14 == 0:
            self.writeUByte(value & 0x7F | 0x80)
            self.writeUByte((value >> 7) & 0x7F)
            return

        if value >> 21 == 0:
            self.writeUByte((value & 0x7F) | 0x80)
            self.writeUByte(((value >> 7) & 0x7F | 0x80))
            self.writeUByte((value >> 14) & 0x7F)
            return

        if value >> 28 == 0:
            self.writeUByte(value & 0x7F | 0x80)
            self.writeUByte(((value >> 7) & 0x7F | 0x80))
            self.writeUByte(((value >> 14) & 0x7F | 0x80))
            self.writeUByte((value >> 21) & 0x7F)
            return

        self.writeUByte(value & 0x7F | 0x80)
        self.writeUByte(((value >> 7) & 0x7F | 0x80))
        self.writeUByte(((value >> 14) & 0x7F | 0x80))
        self.writeUByte(((value >> 21) & 0x7F | 0x80))
        self.writeUByte((value >> 28) & 0x7F)
        pass

    def writeIntCount(self, value):
        if not (minInt <= value <= maxInt):
            raise ValueError('value must range between minInt:-2147483648 and maxInt:2147483647')
        value = (value << 1) ^ (value >> 31)
        if value >> 7 == 0:
            return 1
        if value >> 14 == 0:
            return 2
        if value >> 21 == 0:
            return 3
        if value >> 28 == 0:
            return 4
        return 5

    def readInt(self):
        b = self.readUByte()
        value = b & 0x7F
        if (b & 0x80) != 0:
            b = self.readUByte()
            value |= (b & 0x7F) << 7
            if (b & 0x80) != 0:
                b = self.readUByte()
                value |= (b & 0x7F) << 14
                if (b & 0x80) != 0:
                    b = self.readUByte()
                    value |= (b & 0x7F) << 21
                    if (b & 0x80) != 0:
                        b = self.readUByte()
                        value |= (b & 0x7F) << 28
        return (value >> 1) ^ -(value & 1)

    def writeLong(self, value):
        if not (minLong <= value <= maxLong):
            raise ValueError('value must range between minLong:-9223372036854775808 and maxLong:9223372036854775807')
        value = (value << 1) ^ (value >> 63)
        self.ensureCapacity(9)
        if value >> 7 == 0:
            self.writeUByte(value)
            return

        if value >> 14 == 0:
            self.writeUByte(value & 0x7F | 0x80)
            self.writeUByte((value >> 7) & 0x7F)
            return

        if value >> 21 == 0:
            self.writeUByte((value & 0x7F) | 0x80)
            self.writeUByte(((value >> 7) & 0x7F | 0x80))
            self.writeUByte((value >> 14) & 0x7F)
            return

        if value >> 28 == 0:
            self.writeUByte(value & 0x7F | 0x80)
            self.writeUByte(((value >> 7) & 0x7F | 0x80))
            self.writeUByte(((value >> 14) & 0x7F | 0x80))
            self.writeUByte((value >> 21) & 0x7F)
            return
        if value >> 35 == 0:
            self.writeUByte(value & 0x7F | 0x80)
            self.writeUByte(((value >> 7) & 0x7F | 0x80))
            self.writeUByte(((value >> 14) & 0x7F | 0x80))
            self.writeUByte(((value >> 21) & 0x7F | 0x80))
            self.writeUByte((value >> 28) & 0x7F)
            return
        if value >> 42 == 0:
            self.writeUByte(value & 0x7F | 0x80)
            self.writeUByte(((value >> 7) & 0x7F | 0x80))
            self.writeUByte(((value >> 14) & 0x7F | 0x80))
            self.writeUByte(((value >> 21) & 0x7F | 0x80))
            self.writeUByte(((value >> 28) & 0x7F | 0x80))
            self.writeUByte((value >> 35) & 0x7F)
            return
        if value >> 49 == 0:
            self.writeUByte(value & 0x7F | 0x80)
            self.writeUByte(((value >> 7) & 0x7F | 0x80))
            self.writeUByte(((value >> 14) & 0x7F | 0x80))
            self.writeUByte(((value >> 21) & 0x7F | 0x80))
            self.writeUByte(((value >> 28) & 0x7F | 0x80))
            self.writeUByte(((value >> 35) & 0x7F | 0x80))
            self.writeUByte((value >> 42) & 0x7F)
            return
        if value >> 56 == 0:
            self.writeUByte(value & 0x7F | 0x80)
            self.writeUByte(((value >> 7) & 0x7F | 0x80))
            self.writeUByte(((value >> 14) & 0x7F | 0x80))
            self.writeUByte(((value >> 21) & 0x7F | 0x80))
            self.writeUByte(((value >> 28) & 0x7F | 0x80))
            self.writeUByte(((value >> 35) & 0x7F | 0x80))
            self.writeUByte(((value >> 42) & 0x7F | 0x80))
            self.writeUByte((value >> 49) & 0x7F)
            return
        self.writeUByte(value & 0x7F | 0x80)
        self.writeUByte(((value >> 7) & 0x7F | 0x80))
        self.writeUByte(((value >> 14) & 0x7F | 0x80))
        self.writeUByte(((value >> 21) & 0x7F | 0x80))
        self.writeUByte(((value >> 28) & 0x7F | 0x80))
        self.writeUByte(((value >> 35) & 0x7F | 0x80))
        self.writeUByte(((value >> 42) & 0x7F | 0x80))
        self.writeUByte(((value >> 49) & 0x7F | 0x80))
        self.writeUByte(value >> 56)
        pass

    def readLong(self):
        b = self.readUByte()
        value = b & 0x7F
        if (b & 0x80) != 0:
            b = self.readUByte()
            value |= (b & 0x7F) << 7
            if (b & 0x80) != 0:
                b = self.readUByte()
                value |= (b & 0x7F) << 14
                if (b & 0x80) != 0:
                    b = self.readUByte()
                    value |= (b & 0x7F) << 21
                    if (b & 0x80) != 0:
                        b = self.readUByte()
                        value |= (b & 0x7F) << 28
                        if (b & 0x80) != 0:
                            b = self.readUByte()
                            value |= (b & 0x7F) << 35
                            if (b & 0x80) != 0:
                                b = self.readUByte()
                                value |= (b & 0x7F) << 42
                                if (b & 0x80) != 0:
                                    b = self.readUByte()
                                    value |= (b & 0x7F) << 49
                                    if (b & 0x80) != 0:
                                        b = self.readUByte()
                                        value |= b << 56
        return (value >> 1) ^ -(value & 1)

    def writeFloat(self, value):
        self.ensureCapacity(4)
        struct.pack_into('>f', self.buffer, self.writeOffset, value)
        self.writeOffset += 4
        pass

    def readFloat(self):
        value = struct.unpack_from('>f', self.buffer, self.readOffset)[0]
        self.readOffset += 4
        return value

    def writeDouble(self, value):
        self.ensureCapacity(8)
        struct.pack_into('>d', self.buffer, self.writeOffset, value)
        self.writeOffset += 8
        pass

    def readDouble(self):
        value = struct.unpack_from('>d', self.buffer, self.readOffset)[0]
        self.readOffset += 8
        return value

    def writeString(self, value):
        if len(value) == 0:
            self.writeInt(0)
            return
        byte_array = bytearray(value, 'utf-8')
        length = len(byte_array)
        self.ensureCapacity(5 + length)
        self.writeInt(length)
        self.buffer[self.writeOffset:self.writeOffset + length] = byte_array[:]
        self.writeOffset += length
        pass

    def readString(self):
        length = self.readInt()
        if (length <= 0):
            return empty_str
        byte_array = self.buffer[self.readOffset:self.readOffset + length]
        # 使用utf-8编码进行解码
        value = byte_array.decode('utf-8')
        self.readOffset += length
        return value

    def writePacketFlag(self, value):
        if value is None:
            self.writeBool(False)
        else:
            self.writeBool(True)
        pass

    def writePacket(self, packet, protocolId):
        protocolRegistration = ProtocolManager.getProtocol(protocolId)
        protocolRegistration.write(self, packet)
        pass

    def readPacket(self, protocolId):
        protocolRegistration = ProtocolManager.getProtocol(protocolId)
        return protocolRegistration.read(self)

    def writeBooleanArray(self, array):
        if array is None:
            self.writeInt(0)
        else:
            self.writeInt(len(array))
            for element in array:
                self.writeBool(element)
        pass

    def readBooleanArray(self):
        array = []
        size = self.readInt()
        if size > 0:
            for index in range(size):
                array.append(self.readBool())
        return array

    def writeByteArray(self, array):
        if array is None:
            self.writeInt(0)
        else:
            self.writeInt(len(array))
            for element in array:
                self.writeByte(element)
        pass

    def readByteArray(self):
        array = []
        size = self.readInt()
        if size > 0:
            for index in range(size):
                array.append(self.readByte())
        return array

    def writeShortArray(self, array):
        if array is None:
            self.writeInt(0)
        else:
            self.writeInt(len(array))
            for element in array:
                self.writeShort(element)
        pass

    def readShortArray(self):
        array = []
        size = self.readInt()
        if size > 0:
            for index in range(size):
                array.append(self.readShort())
        return array

    def writeIntArray(self, array):
        if array is None:
            self.writeInt(0)
        else:
            self.writeInt(len(array))
            for element in array:
                self.writeInt(element)
        pass

    def readIntArray(self):
        array = []
        size = self.readInt()
        if size > 0:
            for index in range(size):
                array.append(self.readInt())
        return array

    def writeLongArray(self, array):
        if array is None:
            self.writeInt(0)
        else:
            self.writeInt(len(array))
            for element in array:
                self.writeLong(element)
        pass

    def readLongArray(self):
        array = []
        size = self.readInt()
        if size > 0:
            for index in range(size):
                array.append(self.readLong())
        return array

    def writeFloatArray(self, array):
        if array is None:
            self.writeInt(0)
        else:
            self.writeInt(len(array))
            for element in array:
                self.writeFloat(element)
        pass

    def readFloatArray(self):
        array = []
        size = self.readInt()
        if size > 0:
            for index in range(size):
                array.append(self.readFloat())
        return array

    def writeDoubleArray(self, array):
        if array is None:
            self.writeInt(0)
        else:
            self.writeInt(len(array))
            for element in array:
                self.writeDouble(element)
        pass

    def readDoubleArray(self):
        array = []
        size = self.readInt()
        if size > 0:
            for index in range(size):
                array.append(self.readDouble())
        return array

    def writeStringArray(self, array):
        if array is None:
            self.writeInt(0)
        else:
            self.writeInt(len(array))
            for element in array:
                self.writeString(element)
        pass

    def readStringArray(self):
        array = []
        size = self.readInt()
        if size > 0:
            for index in range(size):
                array.append(self.readString())
        return array

    def writePacketArray(self, array, protocolId):
        if array is None:
            self.writeInt(0)
        else:
            self.writeInt(len(array))
            for element in array:
                self.writePacket(element, protocolId)
        pass

    def readPacketArray(self, protocolId):
        array = []
        size = self.readInt()
        if size > 0:
            for index in range(size):
                array.append(self.readPacket(protocolId))
        return array

    def writeIntIntMap(self, map):
        if map is None:
            self.writeInt(0)
        else:
            self.writeInt(len(map))
            for key, value in map.items():
                self.writeInt(key)
                self.writeInt(value)
        pass

    def readIntIntMap(self):
        map = {}
        size = self.readInt()
        if size > 0:
            for index in range(size):
                key = self.readInt()
                value = self.readInt()
                map[key] = value
        return map

    def writeIntLongMap(self, map):
        if map is None:
            self.writeInt(0)
        else:
            self.writeInt(len(map))
            for key, value in map.items():
                self.writeInt(key)
                self.writeLong(value)
        pass

    def readIntLongMap(self):
        map = {}
        size = self.readInt()
        if size > 0:
            for index in range(size):
                key = self.readInt()
                value = self.readLong()
                map[key] = value
        return map

    def writeIntStringMap(self, map):
        if map is None:
            self.writeInt(0)
        else:
            self.writeInt(len(map))
            for key, value in map.items():
                self.writeInt(key)
                self.writeString(value)
        pass

    def readIntStringMap(self):
        map = {}
        size = self.readInt()
        if size > 0:
            for index in range(size):
                key = self.readInt()
                value = self.readString()
                map[key] = value
        return map

    def writeIntPacketMap(self, map, protocolId):
        if map is None:
            self.writeInt(0)
        else:
            self.writeInt(len(map))
            for key, value in map.items():
                self.writeInt(key)
                self.writePacket(value, protocolId)
        pass

    def readIntPacketMap(self, protocolId):
        map = {}
        size = self.readInt()
        if size > 0:
            for index in range(size):
                key = self.readInt()
                value = self.readPacket(protocolId)
                map[key] = value
        return map

    def writeLongIntMap(self, map):
        if map is None:
            self.writeInt(0)
        else:
            self.writeInt(len(map))
            for key, value in map.items():
                self.writeLong(key)
                self.writeInt(value)
        pass

    def readLongIntMap(self):
        map = {}
        size = self.readInt()
        if size > 0:
            for index in range(size):
                key = self.readLong()
                value = self.readInt()
                map[key] = value
        return map

    def writeLongLongMap(self, map):
        if map is None:
            self.writeInt(0)
        else:
            self.writeInt(len(map))
            for key, value in map.items():
                self.writeLong(key)
                self.writeLong(value)
        pass

    def readLongLongMap(self):
        map = {}
        size = self.readInt()
        if size > 0:
            for index in range(size):
                key = self.readLong()
                value = self.readLong()
                map[key] = value
        return map

    def writeLongStringMap(self, map):
        if map is None:
            self.writeInt(0)
        else:
            self.writeInt(len(map))
            for key, value in map.items():
                self.writeLong(key)
                self.writeString(value)
        pass

    def readLongStringMap(self):
        map = {}
        size = self.readInt()
        if size > 0:
            for index in range(size):
                key = self.readLong()
                value = self.readString()
                map[key] = value
        return map

    def writeLongPacketMap(self, map, protocolId):
        if map is None:
            self.writeInt(0)
        else:
            self.writeInt(len(map))
            for key, value in map.items():
                self.writeLong(key)
                self.writePacket(value, protocolId)
        pass

    def readLongPacketMap(self, protocolId):
        map = {}
        size = self.readInt()
        if size > 0:
            for index in range(size):
                key = self.readLong()
                value = self.readPacket(protocolId)
                map[key] = value
        return map

    def writeStringIntMap(self, map):
        if map is None:
            self.writeInt(0)
        else:
            self.writeInt(len(map))
            for key, value in map.items():
                self.writeString(key)
                self.writeInt(value)
        pass

    def readStringIntMap(self):
        map = {}
        size = self.readInt()
        if size > 0:
            for index in range(size):
                key = self.readString()
                value = self.readInt()
                map[key] = value
        return map

    def writeStringLongMap(self, map):
        if map is None:
            self.writeInt(0)
        else:
            self.writeInt(len(map))
            for key, value in map.items():
                self.writeString(key)
                self.writeLong(value)
        pass

    def readStringLongMap(self):
        map = {}
        size = self.readInt()
        if size > 0:
            for index in range(size):
                key = self.readString()
                value = self.readLong()
                map[key] = value
        return map

    def writeStringStringMap(self, map):
        if map is None:
            self.writeInt(0)
        else:
            self.writeInt(len(map))
            for key, value in map.items():
                self.writeString(key)
                self.writeString(value)
        pass

    def readStringStringMap(self):
        map = {}
        size = self.readInt()
        if size > 0:
            for index in range(size):
                key = self.readString()
                value = self.readString()
                map[key] = value
        return map

    def writeStringPacketMap(self, map, protocolId):
        if map is None:
            self.writeInt(0)
        else:
            self.writeInt(len(map))
            for key, value in map.items():
                self.writeString(key)
                self.writePacket(value, protocolId)
        pass

    def readStringPacketMap(self, protocolId):
        map = {}
        size = self.readInt()
        if size > 0:
            for index in range(size):
                key = self.readString()
                value = self.readPacket(protocolId)
                map[key] = value
        return map

    def writeBooleanSet(self, value):
        if value is None:
            self.writeInt(0)
        else:
            self.writeInt(len(value))
            for element in value:
                self.writeBool(element)
        pass

    def readBooleanSet(self):
        value = set()
        size = self.readInt()
        if size > 0:
            for index in range(size):
                value.add(self.readBool())
        return value

    def writeByteSet(self, value):
        if value is None:
            self.writeInt(0)
        else:
            self.writeInt(len(value))
            for element in value:
                self.writeByte(element)
        pass

    def readByteSet(self):
        value = set()
        size = self.readInt()
        if size > 0:
            for index in range(size):
                value.add(self.readByte())
        return value

    def writeShortSet(self, value):
        if value is None:
            self.writeInt(0)
        else:
            self.writeInt(len(value))
            for element in value:
                self.writeShort(element)
        pass

    def readShortSet(self):
        value = set()
        size = self.readInt()
        if size > 0:
            for index in range(size):
                value.add(self.readShort())
        return value

    def writeIntSet(self, value):
        if value is None:
            self.writeInt(0)
        else:
            self.writeInt(len(value))
            for element in value:
                self.writeInt(element)
        pass

    def readIntSet(self):
        value = set()
        size = self.readInt()
        if size > 0:
            for index in range(size):
                value.add(self.readInt())
        return value

    def writeLongSet(self, value):
        if value is None:
            self.writeInt(0)
        else:
            self.writeInt(len(value))
            for element in value:
                self.writeLong(element)
        pass

    def readLongSet(self):
        value = set()
        size = self.readInt()
        if size > 0:
            for index in range(size):
                value.add(self.readLong())
        return value

    def writeFloatSet(self, value):
        if value is None:
            self.writeInt(0)
        else:
            self.writeInt(len(value))
            for element in value:
                self.writeFloat(element)
        pass

    def readFloatSet(self):
        value = set()
        size = self.readInt()
        if size > 0:
            for index in range(size):
                value.add(self.readFloat())
        return value

    def writeDoubleSet(self, value):
        if value is None:
            self.writeInt(0)
        else:
            self.writeInt(len(value))
            for element in value:
                self.writeDouble(element)
        pass

    def readDoubleSet(self):
        value = set()
        size = self.readInt()
        if size > 0:
            for index in range(size):
                value.add(self.readDouble())
        return value

    def writeStringSet(self, value):
        if value is None:
            self.writeInt(0)
        else:
            self.writeInt(len(value))
            for element in value:
                self.writeString(element)
        pass

    def readStringSet(self):
        value = set()
        size = self.readInt()
        if size > 0:
            for index in range(size):
                value.add(self.readString())
        return value

    def writePacketSet(self, value, protocolId):
        if value is None:
            self.writeInt(0)
        else:
            self.writeInt(len(value))
            for element in value:
                self.writePacket(element, protocolId)
        pass

    def readPacketSet(self, protocolId):
        value = set()
        size = self.readInt()
        if size > 0:
            for index in range(size):
                value.add(self.readPacket(protocolId))
        return value