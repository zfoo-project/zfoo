require_relative "ProtocolManager.rb"

class ByteBuffer
    def initialize()
        @writeOffset = 0
        @readOffset = 0
        @buffer = "\u0000" * 2
    end

    def compatibleRead(beforeReadIndex, length)
        return length != -1 && getWriteOffset() < length + beforeReadIndex
    end

    def getCapacity()
        return @buffer.length - @writeOffset
    end

    def ensureCapacity(capacity)
        while capacity > getCapacity()
            @buffer += "\u0000" * @buffer.length
        end
    end

    def getBuffer()
        return @buffer
    end

    def getWriteOffset()
        return @writeOffset
    end

    def setWriteOffset(writeIndex)
        if writeIndex > @buffer.length
            raise "writeIndex[#{writeIndex}] out of bounds exception: readOffset: #{@readOffset} , writeOffset: #{@writeOffset}(expected: 0 <= readOffset <= writeOffset <= capacity:#{@buffer.length})"
        end
        @writeOffset = writeIndex
    end

    def getReadOffset()
        return @readOffset
    end

    def setReadOffset(readIndex)
        if readIndex > @writeOffset
            raise "readIndex[#{readIndex}] out of bounds exception: readOffset: #{@readOffset} , writeOffset: #{@writeOffset}(expected: 0 <= readOffset <= writeOffset <= capacity:#{@buffer.length})"
        end
        @readOffset = readIndex
    end

    def writeBytes(bytes)
        ensureCapacity(bytes.length)
        for i in 0..bytes.length
            writeByte(bytes[i])
        end
    end

    def writeBytesString(bytes)
        length = bytes.length
        ensureCapacity(length)
        @buffer[@writeOffset..(@writeOffset + length - 1)] = bytes[0..(length - 1)]
        @writeOffset += length
    end

    def writeBool(value)
        ensureCapacity(1)
        if value == true
            @buffer[@writeOffset] = "\u0001"
        else
            @buffer[@writeOffset] = "\u0000"
        end
        @writeOffset += 1
    end

    def readBool()
        value = false
        if @buffer[@readOffset] == "\u0001"
            value = true
        end
        @readOffset += 1
        return value
    end

    def writeByte(value)
        ensureCapacity(1)
        @buffer[@writeOffset] = [value].pack('c')[0]
        @writeOffset += 1
    end

    def readByte()
        value = @buffer[@readOffset].unpack('c').first
        @readOffset += 1
        return value
    end

    def readUByte()
        value = @buffer.getbyte(@readOffset)
        @readOffset += 1
        return value
    end

    def writeShort(value)
        ensureCapacity(2)
        @buffer[@writeOffset..(@writeOffset + 1)] = [value].pack('s>')[0..1]
        @writeOffset += 2
    end

    def readShort()
        value = @buffer[@readOffset..(@readOffset + 1)].unpack('s>').first
        @readOffset += 2
        return value
    end

    def writeRawInt(value)
        ensureCapacity(4)
        @buffer[@writeOffset..(@writeOffset + 3)] = [value].pack('i>')[0..3]
        @writeOffset += 4
    end

    def readRawInt()
        value = @buffer[@readOffset..(@readOffset + 3)].unpack('i>').first
        @readOffset += 4
        return value
    end

    def writeInt(value)
        writeLong(value)
    end

    def readInt()
        return readLong()
    end

    def writeLong(longValue)
        value = (longValue << 1) ^ (longValue >> 63)
        if value < 0
            writeByte(value & 0xFF | 0x80)
            writeByte(value >> 7 & 0xFF | 0x80)
            writeByte(value >> 14 & 0xFF | 0x80)
            writeByte(value >> 21 & 0xFF | 0x80)
            writeByte(value >> 28 & 0xFF | 0x80)
            writeByte(value >> 35 & 0xFF | 0x80)
            writeByte(value >> 42 & 0xFF | 0x80)
            writeByte(value >> 49 & 0xFF | 0x80)
            writeByte(value >> 56 & 0xFF)
            return
        end
        if value >> 7 == 0
            writeByte(value)
            return
        end
        if value >> 14 == 0
            writeByte(value | 0x80)
            writeByte(value >> 7)
            return
        end
        if value >> 21 == 0
            writeByte(value | 0x80)
            writeByte(value >> 7 | 0x80)
            writeByte(value >> 14)
            return
        end
        if value >> 28 == 0
            writeByte(value | 0x80)
            writeByte(value >> 7 | 0x80)
            writeByte(value >> 14 | 0x80)
            writeByte(value >> 21)
            return
        end
        if value >> 35 == 0
            writeByte(value | 0x80)
            writeByte(value >> 7 | 0x80)
            writeByte(value >> 14 | 0x80)
            writeByte(value >> 21 | 0x80)
            writeByte(value >> 28)
            return
        end
        if value >> 42 == 0
            writeByte(value | 0x80)
            writeByte(value >> 7 | 0x80)
            writeByte(value >> 14 | 0x80)
            writeByte(value >> 21 | 0x80)
            writeByte(value >> 28 | 0x80)
            writeByte(value >> 35)
            return
        end
        if value >> 49 == 0
            writeByte(value | 0x80)
            writeByte(value >> 7 | 0x80)
            writeByte(value >> 14 | 0x80)
            writeByte(value >> 21 | 0x80)
            writeByte(value >> 28 | 0x80)
            writeByte(value >> 35 | 0x80)
            writeByte(value >> 42)
            return
        end
        if (value >> 56) == 0
            writeByte(value | 0x80)
            writeByte(value >> 7 | 0x80)
            writeByte(value >> 14 | 0x80)
            writeByte(value >> 21 | 0x80)
            writeByte(value >> 28 | 0x80)
            writeByte(value >> 35 | 0x80)
            writeByte(value >> 42 | 0x80)
            writeByte(value >> 49)
            return
        end
        writeByte(value | 0x80)
        writeByte(value >> 7 | 0x80)
        writeByte(value >> 14 | 0x80)
        writeByte(value >> 21 | 0x80)
        writeByte(value >> 28 | 0x80)
        writeByte(value >> 35 | 0x80)
        writeByte(value >> 42 | 0x80)
        writeByte(value >> 49 | 0x80)
        writeByte(value >> 56)
        return
    end

    def readLong()
        b = readUByte()
        value = b
        if b > 127
            b = readUByte()
            value = value & 0x00000000_0000007F | b << 7
            if b > 127
                b = readUByte()
                value = value & 0x00000000_00003FFF | b << 14
                if b > 127
                    b = readUByte()
                    value = value & 0x00000000_001FFFFF | b << 21
                    if b > 127
                        b = readUByte()
                        value = value & 0x00000000_0FFFFFFF | b << 28
                        if b > 127
                            b = readUByte()
                            value = value & 0x00000007_FFFFFFFF | b << 35
                            if b > 127
                                b = readUByte()
                                value = value & 0x000003FF_FFFFFFFF | b << 42
                                if b > 127
                                    b = readUByte()
                                    value = value & 0x0001FFFF_FFFFFFFF | b << 49
                                    if b > 127
                                        b = readUByte()
                                        value = value & 0x00FFFFFF_FFFFFFFF | b << 56
                                    end
                                end
                            end
                        end
                    end
                end
            end
        end
        return ((value >> 1 & 0x7FFFFFFF_FFFFFFFF) ^ -(value & 1))
    end

    def writeFloat(value)
        ensureCapacity(4)
        @buffer[@writeOffset..(@writeOffset + 3)] = [value].pack('f')[0..3]
        @writeOffset += 4
    end

    def readFloat()
        value = @buffer[@readOffset..(@readOffset + 3)].unpack('f').first
        @readOffset += 4
        return value
    end

    def writeDouble(value)
        ensureCapacity(8)
        @buffer[@writeOffset..(@writeOffset + 7)] = [value].pack('d')[0..7]
        @writeOffset += 8
    end

    def readDouble()
        value = @buffer[@readOffset..(@readOffset + 7)].unpack('d').first
        @readOffset += 8
        return value
    end

    def writeString(value)
        if value.nil? || value.empty?
            writeInt(0)
            return
        end
        value = value.dup
        value.force_encoding("UTF-8")
        bytes = value.bytes()
        length = bytes.length
        writeInt(length)
        ensureCapacity(length)
        @buffer[@writeOffset..(@writeOffset + length - 1)] = bytes.pack('C*')[0..(length - 1)]
        @writeOffset += length
    end

    def readString()
        length = readInt()
        if length == 0
            return ""
        end
        value = "\u0000" * length
        for i in 0..(length - 1)
            value.setbyte(i, readUByte())
        end
        value.force_encoding("UTF-8")
        return value
    end

    def writePacket(packet, protocolId)
        protocolRegistration = ProtocolManager.getProtocol(protocolId)
        protocolRegistration.write(self, packet)
    end

    def readPacket(protocolId)
        protocolRegistration = ProtocolManager.getProtocol(protocolId)
        return protocolRegistration.read(self)
    end

    def writeBoolArray(array)
        if array.nil? || array.empty?
            writeInt(0)
            return
        end
        writeInt(array.length)
        for element in array
            writeBool(element)
        end
    end

    def readBoolArray()
        array = Array.new()
        size = readInt()
        if size > 0
            for i in 0..(size - 1)
                array.push(readBool())
            end
        end
        return array
    end

    def writeByteArray(array)
        if array.nil? || array.empty?
            writeInt(0)
            return
        end
        writeInt(array.length)
        for element in array
            writeByte(element)
        end
    end

    def readByteArray()
        array = Array.new()
        size = readInt()
        if size > 0
            for i in 0..(size - 1)
                array.push(readByte())
            end
        end
        return array
    end

    def writeShortArray(array)
        if array.nil? || array.empty?
            writeInt(0)
            return
        end
        writeInt(array.length)
        for element in array
            writeShort(element)
        end
    end

    def readShortArray()
        array = Array.new()
        size = readInt()
        if size > 0
            for i in 0..(size - 1)
                array.push(readShort())
            end
        end
        return array
    end

    def writeIntArray(array)
        if array.nil? || array.empty?
            writeInt(0)
            return
        end
        writeInt(array.length)
        for element in array
            writeInt(element)
        end
    end

    def readIntArray()
        array = Array.new()
        size = readInt()
        if size > 0
            for i in 0..(size - 1)
                array.push(readInt())
            end
        end
        return array
    end

    def writeLongArray(array)
        if array.nil? || array.empty?
            writeInt(0)
            return
        end
        writeInt(array.length)
        for element in array
            writeLong(element)
        end
    end

    def readLongArray()
        array = Array.new()
        size = readInt()
        if size > 0
            for i in 0..(size - 1)
                array.push(readLong())
            end
        end
        return array
    end

    def writeFloatArray(array)
        if array.nil? || array.empty?
            writeInt(0)
            return
        end
        writeInt(array.length)
        for element in array
            writeFloat(element)
        end
    end

    def readFloatArray()
        array = Array.new()
        size = readInt()
        if size > 0
            for i in 0..(size - 1)
                array.push(readFloat())
            end
        end
        return array
    end

    def writeDoubleArray(array)
        if array.nil? || array.empty?
            writeInt(0)
            return
        end
        writeInt(array.length)
        for element in array
            writeDouble(element)
        end
    end

    def readDoubleArray()
        array = Array.new()
        size = readInt()
        if size > 0
            for i in 0..(size - 1)
                array.push(readDouble())
            end
        end
        return array
    end

    def writeStringArray(array)
        if array.nil? || array.empty?
            writeInt(0)
            return
        end
        writeInt(array.length)
        for element in array
            writeString(element)
        end
    end

    def readStringArray()
        array = Array.new()
        size = readInt()
        if size > 0
            for i in 0..(size - 1)
                array.push(readString())
            end
        end
        return array
    end

    def writePacketArray(array, protocolId)
        if array.nil? || array.empty?
            writeInt(0)
            return
        end
        writeInt(array.length)
        for element in array
            writePacket(element, protocolId)
        end
    end

    def readPacketArray(protocolId)
        array = Array.new()
        size = readInt()
        if size > 0
            for i in 0..(size - 1)
                array.push(readPacket(protocolId))
            end
        end
        return array
    end

    def writeBoolSet(set)
        if set.nil? || set.empty?
            writeInt(0)
            return
        end
        writeInt(set.length)
        for element in set
            writeBool(element)
        end
    end

    def readBoolSet()
        set = Set.new()
        size = readInt()
        if size > 0
            for i in 0..(size - 1)
                set.add(readBool())
            end
        end
        return set
    end

    def writeByteSet(set)
        if set.nil? || set.empty?
            writeInt(0)
            return
        end
        writeInt(set.length)
        for element in set
            writeByte(element)
        end
    end

    def readByteSet()
        set = Set.new()
        size = readInt()
        if size > 0
            for i in 0..(size - 1)
                set.add(readByte())
            end
        end
        return set
    end

    def writeShortSet(set)
        if set.nil? || set.empty?
            writeInt(0)
            return
        end
        writeInt(set.length)
        for element in set
            writeShort(element)
        end
    end

    def readShortSet()
        set = Set.new()
        size = readInt()
        if size > 0
            for i in 0..(size - 1)
                set.add(readShort())
            end
        end
        return set
    end

    def writeIntSet(set)
        if set.nil? || set.empty?
            writeInt(0)
            return
        end
        writeInt(set.length)
        for element in set
            writeInt(element)
        end
    end

    def readIntSet()
        set = Set.new()
        size = readInt()
        if size > 0
            for i in 0..(size - 1)
                set.add(readInt())
            end
        end
        return set
    end

    def writeLongSet(set)
        if set.nil? || set.empty?
            writeInt(0)
            return
        end
        writeInt(set.length)
        for element in set
            writeLong(element)
        end
    end

    def readLongSet()
        set = Set.new()
        size = readInt()
        if size > 0
            for i in 0..(size - 1)
                set.add(readLong())
            end
        end
        return set
    end

    def writeFloatSet(set)
        if set.nil? || set.empty?
            writeInt(0)
            return
        end
        writeInt(set.length)
        for element in set
            writeFloat(element)
        end
    end

    def readFloatSet()
        set = Set.new()
        size = readInt()
        if size > 0
            for i in 0..(size - 1)
                set.add(readFloat())
            end
        end
        return set
    end

    def writeDoubleSet(set)
        if set.nil? || set.empty?
            writeInt(0)
            return
        end
        writeInt(set.length)
        for element in set
            writeDouble(element)
        end
    end

    def readDoubleSet()
        set = Set.new()
        size = readInt()
        if size > 0
            for i in 0..(size - 1)
                set.add(readDouble())
            end
        end
        return set
    end

    def writeStringSet(set)
        if set.nil? || set.empty?
            writeInt(0)
            return
        end
        writeInt(set.length)
        for element in set
            writeString(element)
        end
    end

    def readStringSet()
        set = Set.new()
        size = readInt()
        if size > 0
            for i in 0..(size - 1)
                set.add(readString())
            end
        end
        return set
    end

    def writePacketSet(set, protocolId)
        if set.nil? || set.empty?
            writeInt(0)
            return
        end
        writeInt(set.length)
        for element in set
            writePacket(element, protocolId)
        end
    end

    def readPacketSet(protocolId)
        set = Set.new()
        size = readInt()
        if size > 0
            for i in 0..(size - 1)
                set.add(readPacket(protocolId))
            end
        end
        return set
    end

    def writeIntIntMap(map)
        if map.nil? || map.empty?
            writeInt(0)
            return
        end
        writeInt(map.length)
        map.each do |key, value|
            writeInt(key)
            writeInt(value)
        end
    end

    def readIntIntMap()
        map = Hash.new()
        size = readInt()
        if size > 0
            for i in 0..(size - 1)
                key = readInt()
                value = readInt()
                map[key] = value
            end
        end
        return map
    end

    def writeIntLongMap(map)
        if map.nil? || map.empty?
            writeInt(0)
            return
        end
        writeInt(map.length)
        map.each do |key, value|
            writeInt(key)
            writeLong(value)
        end
    end

    def readIntLongMap()
        map = Hash.new()
        size = readInt()
        if size > 0
            for i in 0..(size - 1)
                key = readInt()
                value = readLong()
                map[key] = value
            end
        end
        return map
    end

    def writeIntStringMap(map)
        if map.nil? || map.empty?
            writeInt(0)
            return
        end
        writeInt(map.length)
        map.each do |key, value|
            writeInt(key)
            writeString(value)
        end
    end

    def readIntStringMap()
        map = Hash.new()
        size = readInt()
        if size > 0
            for i in 0..(size - 1)
                key = readInt()
                value = readString()
                map[key] = value
            end
        end
        return map
    end

    def writeIntPacketMap(map, protocolId)
        if map.nil? || map.empty?
            writeInt(0)
            return
        end
        writeInt(map.length)
        map.each do |key, value|
            writeInt(key)
            writePacket(value, protocolId)
        end
    end

    def readIntPacketMap(protocolId)
        map = Hash.new()
        size = readInt()
        if size > 0
            for i in 0..(size - 1)
                key = readInt()
                value = readPacket(protocolId)
                map[key] = value
            end
        end
        return map
    end

    def writeLongIntMap(map)
        if map.nil? || map.empty?
            writeInt(0)
            return
        end
        writeInt(map.length)
        map.each do |key, value|
            writeLong(key)
            writeInt(value)
        end
    end

    def readLongIntMap()
        map = Hash.new()
        size = readInt()
        if size > 0
            for i in 0..(size - 1)
                key = readLong()
                value = readInt()
                map[key] = value
            end
        end
        return map
    end

    def writeLongLongMap(map)
        if map.nil? || map.empty?
            writeInt(0)
            return
        end
        writeInt(map.length)
        map.each do |key, value|
            writeLong(key)
            writeLong(value)
        end
    end

    def readLongLongMap()
        map = Hash.new()
        size = readInt()
        if size > 0
            for i in 0..(size - 1)
                key = readLong()
                value = readLong()
                map[key] = value
            end
        end
        return map
    end

    def writeLongStringMap(map)
        if map.nil? || map.empty?
            writeInt(0)
            return
        end
        writeInt(map.length)
        map.each do |key, value|
            writeLong(key)
            writeString(value)
        end
    end

    def readLongStringMap()
        map = Hash.new()
        size = readInt()
        if size > 0
            for i in 0..(size - 1)
                key = readLong()
                value = readString()
                map[key] = value
            end
        end
        return map
    end

    def writeLongPacketMap(map, protocolId)
        if map.nil? || map.empty?
            writeInt(0)
            return
        end
        writeInt(map.length)
        map.each do |key, value|
            writeLong(key)
            writePacket(value, protocolId)
        end
    end

    def readLongPacketMap(protocolId)
        map = Hash.new()
        size = readInt()
        if size > 0
            for i in 0..(size - 1)
                key = readLong()
                value = readPacket(protocolId)
                map[key] = value
            end
        end
        return map
    end

    def writeStringIntMap(map)
        if map.nil? || map.empty?
            writeInt(0)
            return
        end
        writeInt(map.length)
        map.each do |key, value|
            writeString(key)
            writeInt(value)
        end
    end

    def readStringIntMap()
        map = Hash.new()
        size = readInt()
        if size > 0
            for i in 0..(size - 1)
                key = readString()
                value = readInt()
                map[key] = value
            end
        end
        return map
    end

    def writeStringLongMap(map)
        if map.nil? || map.empty?
            writeInt(0)
            return
        end
        writeInt(map.length)
        map.each do |key, value|
            writeString(key)
            writeLong(value)
        end
    end

    def readLongLongMap()
        map = Hash.new()
        size = readInt()
        if size > 0
            for i in 0..(size - 1)
                key = readString()
                value = readLong()
                map[key] = value
            end
        end
        return map
    end

    def writeStringStringMap(map)
        if map.nil? || map.empty?
            writeInt(0)
            return
        end
        writeInt(map.length)
        map.each do |key, value|
            writeString(key)
            writeString(value)
        end
    end

    def readStringStringMap()
        map = Hash.new()
        size = readInt()
        if size > 0
            for i in 0..(size - 1)
                key = readString()
                value = readString()
                map[key] = value
            end
        end
        return map
    end

    def writeStringPacketMap(map, protocolId)
        if map.nil? || map.empty?
            writeInt(0)
            return
        end
        writeInt(map.length)
        map.each do |key, value|
            writeString(key)
            writePacket(value, protocolId)
        end
    end

    def readStringPacketMap(protocolId)
        map = Hash.new()
        size = readInt()
        if size > 0
            for i in 0..(size - 1)
                key = readString()
                value = readPacket(protocolId)
                map[key] = value
            end
        end
        return map
    end
end