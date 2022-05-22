--默认为大端模式
--支持的lua版本为>=5.3
--支持标准的Lua是使用64-bit的int以及64-bit的双精度float
--当lua只能支持32位的整数类型时，可以考虑用Long来替代，需要修改原代码
--右移操作>>是无符号右移
--local Long = require("Long")

local ProtocolManager = require("LuaProtocol.ProtocolManager")

local maxInt = 2147483647
local minInt = -2147483648
local initSize = 128
local zeroByte = string.char(0)

local ByteBuffer = {}

local trueBooleanStrValue = string.char(1)
local falseBooleanStrValue = string.char(0)

-------------------------------------构造器-------------------------------------
function ByteBuffer:new()
    --buffer里的每一个元素为一个长度为1的字符串
    local obj = {
        buffer = {},
        writeOffset = 1,
        readOffset = 1
    }
    setmetatable(obj, self)
    self.__index = self

    for i = 1, initSize do
        table.insert(obj.buffer, zeroByte)
    end
    return obj
end


-- C#传进来的byte数组到lua里就会变成string
function readBytes(bytes)
    local buffer = ByteBuffer:new()
    buffer:writeBuffer(bytes)
    local packet = ProtocolManager.read(buffer)
    return packet
end

-------------------------------------UTF8-------------------------------------
-- 判断utf8字符byte长度
-- 0xxxxxxx - 1 byte
-- 110yxxxx - 192, 2 byte
-- 1110yyyy - 225, 3 byte
-- 11110zzz - 240, 4 byte
local function chsize(char)
    if not char then
        print("not char")
        return 0
    elseif char > 240 then
        return 4
    elseif char > 225 then
        return 3
    elseif char > 192 then
        return 2
    else
        return 1
    end
end


-- 截取utf8 字符串
-- str:            要截取的字符串
-- startChar:    开始字符下标,从1开始
-- numChars:    要截取的字符长度
local function utf8sub(str, startChar, numChars)
    local startIndex = 1
    while startChar > 1 do
        local char = string.byte(str, startIndex)
        startIndex = startIndex + chsize(char)
        startChar = startChar - 1
    end

    local currentIndex = startIndex

    while numChars > 0 and currentIndex <= #str do
        local char = string.byte(str, currentIndex)
        currentIndex = currentIndex + chsize(char)
        numChars = numChars - 1
    end
    return str:sub(startIndex, currentIndex - 1)
end


-------------------------------------get和set-------------------------------------
function ByteBuffer:getWriteOffset()
    return self.writeOffset
end

function ByteBuffer:setWriteOffset(writeOffset)
    if writeOffset > #self.buffer then
        error("index out of bounds exception: readerIndex: " + self.readOffset
                + ", writerIndex: " + self.writeOffset
                + "(expected: 0 <= readerIndex <= writerIndex <= capacity:" + #self.buffer)
    end
    self.writeOffset = writeOffset
    return self
end

function ByteBuffer:getReadOffset()
    return self.readOffset
end

function ByteBuffer:setReadOffset(readOffset)
    if readOffset > self.writeOffset then
        error("index out of bounds exception: readerIndex: " + self.readOffset
                + ", writerIndex: " + this.writeOffset
                + "(expected: 0 <= readerIndex <= writerIndex <= capacity:" + #self.buffer)
    end
    self.readOffset = readOffset
    return self
end

function ByteBuffer:getLen()
    return #self.buffer
end

function ByteBuffer:getAvailable()
    return #self.buffer - self.writeOffset + 1
end

function ByteBuffer:isReadable()
    return self.writeOffset > self.readOffset
end

-------------------------------------write和read-------------------------------------

--bool
function ByteBuffer:writeBoolean(boolValue)
    if boolValue then
        self:writeRawByteStr(trueBooleanStrValue)
    else
        self:writeRawByteStr(falseBooleanStrValue)
    end
    return self
end

function ByteBuffer:readBoolean()
    -- When char > 256, the readUByte method will show an error.
    -- So, we have to use readChar
    return self:readRawByteStr() == trueBooleanStrValue
end


--- byte
-- The byte is a number between -128 and 127, otherwise, the lua will get an error.
function ByteBuffer:writeByte(byteValue)
    local str = string.pack("b", byteValue)
    self:writeBuffer(str)
    return self
end

function ByteBuffer:readByte()
    local result = string.unpack("b", self:readRawByteStr())
    return result
end

-- The byte is a number between 0 and 255, otherwise, the lua will get an error.
function ByteBuffer:writeUByte(ubyteValue)
    self:writeRawByteStr(string.char(ubyteValue))
    return self
end

function ByteBuffer:readUByte()
    return string.byte(self:readRawByteStr())
end


-- short
function ByteBuffer:writeShort(shortValue)
    local str = string.pack(">h", shortValue)
    self:writeBuffer(str)
    return self
end

function ByteBuffer:readShort()
    local byteStrArray = self:readBuffer(2)
    local result = string.unpack(">h", byteStrArray)
    return result
end


-- int
function ByteBuffer:writeInt(intValue)
    if (math.type(intValue) ~= "integer") then
        error("intValue must be integer")
    end
    if ((minInt > intValue) or (intValue > maxInt)) then
        error("intValue must range between minInt:-2147483648 and maxInt:2147483647")
    end

    return self:writeLong(intValue)
end

function ByteBuffer:readInt()
    return self:readLong()
end

-- int
function ByteBuffer:writeRawInt(intValue)
    local str = string.pack(">i", intValue)
    self:writeBuffer(str)
    return self
end

function ByteBuffer:readRawInt()
    local byteStrArray = self:readBuffer(4)
    local result = string.unpack(">i", byteStrArray)
    return result
end

--long
function ByteBuffer:writeLong(longValue)
    --Long:writeLong(self, longValue)

    if (math.type(longValue) ~= "integer") then
        error("longValue must be integer")
    end

    --lua中的右移为无符号右移，要特殊处理
    local mask = longValue >> 63
    local value = longValue << 1
    if (mask == 1) then
        value = value ~ 0xFFFFFFFFFFFFFFFF
    end

    if (value >> 7) == 0 then
        self:writeUByte(value)
        return
    end

    if (value >> 14) == 0 then
        self:writeUByte(value & 0x7F | 0x80)
        self:writeUByte((value >> 7) & 0x7F)
        return
    end

    if (value >> 21) == 0 then
        self:writeUByte((value & 0x7F) | 0x80)
        self:writeUByte(((value >> 7) & 0x7F | 0x80))
        self:writeUByte((value >> 14) & 0x7F)
        return
    end

    if (value >> 28) == 0 then
        self:writeUByte(value & 0x7F | 0x80)
        self:writeUByte(((value >> 7) & 0x7F | 0x80))
        self:writeUByte(((value >> 14) & 0x7F | 0x80))
        self:writeUByte((value >> 21) & 0x7F)
        return
    end

    if (value >> 35) == 0 then
        self:writeUByte(value & 0x7F | 0x80)
        self:writeUByte(((value >> 7) & 0x7F | 0x80))
        self:writeUByte(((value >> 14) & 0x7F | 0x80))
        self:writeUByte(((value >> 21) & 0x7F | 0x80))
        self:writeUByte((value >> 28) & 0x7F)
        return
    end

    if (value >> 42) == 0 then
        self:writeUByte(value & 0x7F | 0x80)
        self:writeUByte(((value >> 7) & 0x7F | 0x80))
        self:writeUByte(((value >> 14) & 0x7F | 0x80))
        self:writeUByte(((value >> 21) & 0x7F | 0x80))
        self:writeUByte(((value >> 28) & 0x7F | 0x80))
        self:writeUByte((value >> 35) & 0x7F)
        return
    end

    if (value >> 49) == 0 then
        self:writeUByte(value & 0x7F | 0x80)
        self:writeUByte(((value >> 7) & 0x7F | 0x80))
        self:writeUByte(((value >> 14) & 0x7F | 0x80))
        self:writeUByte(((value >> 21) & 0x7F | 0x80))
        self:writeUByte(((value >> 28) & 0x7F | 0x80))
        self:writeUByte(((value >> 35) & 0x7F | 0x80))
        self:writeUByte((value >> 42) & 0x7F)
        return
    end

    if (value >> 56) == 0 then
        self:writeUByte(value & 0x7F | 0x80)
        self:writeUByte(((value >> 7) & 0x7F | 0x80))
        self:writeUByte(((value >> 14) & 0x7F | 0x80))
        self:writeUByte(((value >> 21) & 0x7F | 0x80))
        self:writeUByte(((value >> 28) & 0x7F | 0x80))
        self:writeUByte(((value >> 35) & 0x7F | 0x80))
        self:writeUByte(((value >> 42) & 0x7F | 0x80))
        self:writeUByte((value >> 49) & 0x7F)
        return
    end

    self:writeUByte(value & 0x7F | 0x80)
    self:writeUByte(((value >> 7) & 0x7F | 0x80))
    self:writeUByte(((value >> 14) & 0x7F | 0x80))
    self:writeUByte(((value >> 21) & 0x7F | 0x80))
    self:writeUByte(((value >> 28) & 0x7F | 0x80))
    self:writeUByte(((value >> 35) & 0x7F | 0x80))
    self:writeUByte(((value >> 42) & 0x7F | 0x80))
    self:writeUByte(((value >> 49) & 0x7F | 0x80))
    self:writeUByte(value >> 56)
    return self
end

function ByteBuffer:readLong()
    --return Long:readLong(self):toString()
    local b = self:readUByte()
    local value = b & 0x7F
    if (b & 0x80) ~= 0 then
        b = self:readUByte()
        value = value | ((b & 0x7F) << 7)
        if (b & 0x80) ~= 0 then
            b = self:readUByte()
            value = value | ((b & 0x7F) << 14)
            if (b & 0x80) ~= 0 then
                b = self:readUByte()
                value = value | ((b & 0x7F) << 21)
                if (b & 0x80) ~= 0 then
                    b = self:readUByte()
                    value = value | ((b & 0x7F) << 28)
                    if (b & 0x80) ~= 0 then
                        b = self:readUByte()
                        value = value | ((b & 0x7F) << 35)
                        if (b & 0x80) ~= 0 then
                            b = self:readUByte()
                            value = value | ((b & 0x7F) << 42)
                            if (b & 0x80) ~= 0 then
                                b = self:readUByte()
                                value = value | ((b & 0x7F) << 49)
                                if (b & 0x80) ~= 0 then
                                    b = self:readUByte()
                                    value = value | (b << 56)
                                end
                            end
                        end
                    end
                end
            end
        end
    end
    return (value >> 1) ~ -(value & 1)
end

--固定8位的lua数字类型
function ByteBuffer:writeLuaNumber(luaNumberValue)
    local str = string.pack(">n", luaNumberValue)
    self:writeBuffer(str)
    return self
end

function ByteBuffer:readLuaNumber()
    local result = string.unpack(">n", self:readBuffer(8))
    return result
end




--float
function ByteBuffer:writeFloat(floatValue)
    local str = string.pack(">f", floatValue)
    self:writeBuffer(str)
    return self
end

function ByteBuffer:readFloat()
    local byteStrArray = self:readBuffer(4)
    local result = string.unpack(">f", byteStrArray)
    return result
end


--double
function ByteBuffer:writeDouble(doubleValue)
    local str = string.pack(">d", doubleValue)
    self:writeBuffer(str)
    return self
end

function ByteBuffer:readDouble()
    local byteStrArray = self:readBuffer(8)
    local result = string.unpack(">d", byteStrArray)
    return result
end


--string
function ByteBuffer:writeString(str)
    if str == nil or #str == 0 then
        self:writeInt(0)
        return
    end
    self:writeInt(#str)
    self:writeBuffer(str)
    return self
end

function ByteBuffer:readString()
    local length = self:readInt()
    return self:readBuffer(length)
end

--char
function ByteBuffer:writeChar(charValue)
    if charValue == nil or #charValue == 0 then
        self:writeString("")
        return
    end
    local str = utf8sub(charValue, 1, 1)
    self:writeString(str)
    return self
end

function ByteBuffer:readChar()
    return self:readString()
end

--- Write a encoded char array into buf
function ByteBuffer:writeBuffer(str)
    for i = 1, #str do
        self:writeRawByteStr(string.sub(str, i, i))
    end
    return self
end

--- Read a byte array as string from current position, then update the position.
function ByteBuffer:readBuffer(length)
    local byteStrArray = self:getBytes(self.readOffset, self.readOffset + length - 1)
    self.readOffset = self.readOffset + length
    return byteStrArray
end

function ByteBuffer:writeRawByteStr(byteStrValue)
    if self.writeOffset > #self.buffer + 1 then
        for i = #self.buffer + 1, self.writeOffset - 1 do
            table.insert(self.buffer, zeroByte)
        end
    end
    self.buffer[self.writeOffset] = string.sub(byteStrValue, 1, 1)
    self.writeOffset = self.writeOffset + 1
    return self
end

function ByteBuffer:readRawByteStr()
    local byteStrValue = self.buffer[self.readOffset]
    self.readOffset = self.readOffset + 1
    return byteStrValue
end

--- Get all byte array as a lua string.
-- Do not update position.
function ByteBuffer:getBytes(startIndex, endIndex)
    startIndex = startIndex or 1
    endIndex = endIndex or #self.buffer
    return table.concat(self.buffer, "", startIndex, endIndex)
end

function ByteBuffer:writePacketFlag(value)
    local flag = (value == null)
    self:writeBoolean(not flag)
    return flag
end

function ByteBuffer:writePacket(value, protocolId)
    local protocolRegistration = ProtocolManager.getProtocol(protocolId)
    protocolRegistration:write(self, value)
    return self
end

function ByteBuffer:readPacket(protocolId)
    local protocolRegistration = ProtocolManager.getProtocol(protocolId)
    return protocolRegistration:read(self)
end

function ByteBuffer:writeBooleanArray(array)
    if array == null then
        self:writeInt(0)
    else
        self:writeInt(#array);
        for index, element in pairs(array) do
            self:writeBoolean(element)
        end
    end
    return self
end

function ByteBuffer:readBooleanArray()
    local array = {}
    local size = self:readInt()
    if size > 0 then
        for index = 1, size do
            table.insert(array, self:readBoolean())
        end
    end
    return array
end

function ByteBuffer:writeByteArray(array)
    if array == null then
        self:writeInt(0)
    else
        self:writeInt(#array);
        for index, element in pairs(array) do
            self:writeByte(element)
        end
    end
    return self
end

function ByteBuffer:readByteArray()
    local array = {}
    local size = self:readInt()
    if size > 0 then
        for index = 1, size do
            table.insert(array, self:readByte())
        end
    end
    return array
end

function ByteBuffer:writeShortArray(array)
    if array == null then
        self:writeInt(0)
    else
        self:writeInt(#array);
        for index, element in pairs(array) do
            self:writeShort(element)
        end
    end
    return self
end

function ByteBuffer:readShortArray()
    local array = {}
    local size = self:readInt()
    if size > 0 then
        for index = 1, size do
            table.insert(array, self:readShort())
        end
    end
    return array
end

function ByteBuffer:writeIntArray(array)
    if array == null then
        self:writeInt(0)
    else
        self:writeInt(#array);
        for index, element in pairs(array) do
            self:writeInt(element)
        end
    end
    return self
end

function ByteBuffer:readIntArray()
    local array = {}
    local size = self:readInt()
    if size > 0 then
        for index = 1, size do
            table.insert(array, self:readInt())
        end
    end
    return array
end

function ByteBuffer:writeLongArray(array)
    if array == null then
        self:writeInt(0)
    else
        self:writeInt(#array);
        for index, element in pairs(array) do
            self:writeLong(element)
        end
    end
    return self
end

function ByteBuffer:readLongArray()
    local array = {}
    local size = self:readInt()
    if size > 0 then
        for index = 1, size do
            table.insert(array, self:readLong())
        end
    end
    return array
end

function ByteBuffer:writeFloatArray(array)
    if array == null then
        self:writeInt(0)
    else
        self:writeInt(#array);
        for index, element in pairs(array) do
            self:writeFloat(element)
        end
    end
    return self
end

function ByteBuffer:readFloatArray()
    local array = {}
    local size = self:readInt()
    if size > 0 then
        for index = 1, size do
            table.insert(array, self:readFloat())
        end
    end
    return array
end

function ByteBuffer:writeDoubleArray(array)
    if array == null then
        self:writeInt(0)
    else
        self:writeInt(#array);
        for index, element in pairs(array) do
            self:writeDouble(element)
        end
    end
    return self
end

function ByteBuffer:readDoubleArray()
    local array = {}
    local size = self:readInt()
    if size > 0 then
        for index = 1, size do
            table.insert(array, self:readDouble())
        end
    end
    return array
end

function ByteBuffer:writeCharArray(array)
    if array == null then
        self:writeInt(0)
    else
        self:writeInt(#array);
        for index, element in pairs(array) do
            self:writeChar(element)
        end
    end
    return self
end

function ByteBuffer:readCharArray()
    local array = {}
    local size = self:readInt()
    if size > 0 then
        for index = 1, size do
            table.insert(array, self:readChar())
        end
    end
    return array
end

function ByteBuffer:writeStringArray(array)
    if array == null then
        self:writeInt(0)
    else
        self:writeInt(#array);
        for index, element in pairs(array) do
            self:writeString(element)
        end
    end
    return self
end

function ByteBuffer:readStringArray()
    local array = {}
    local size = self:readInt()
    if size > 0 then
        for index = 1, size do
            table.insert(array, self:readString())
        end
    end
    return array
end

function ByteBuffer:writePacketArray(array, protocolId)
    if array == null then
        self:writeInt(0)
    else
        local protocolRegistration = ProtocolManager.getProtocol(protocolId)
        self:writeInt(#array);
        for index, element in pairs(array) do
            protocolRegistration:write(self, element)
        end
    end
    return self
end

function ByteBuffer:readPacketArray(protocolId)
    local array = {}
    local size = self:readInt()
    if size > 0 then
        local protocolRegistration = ProtocolManager.getProtocol(protocolId)
        for index = 1, size do
            table.insert(array, protocolRegistration:read(self))
        end
    end
    return array
end

function ByteBuffer:writeIntIntMap(map)
    if map == null then
        self:writeInt(0)
    else
        self:writeInt(table.mapSize(map));
        for key, value in pairs(map) do
            self:writeInt(key)
            self:writeInt(value)
        end
    end
    return self
end

function ByteBuffer:readIntIntMap()
    local map = {}
    local size = self:readInt()
    if size > 0 then
        for index = 1, size do
            local key = self:readInt()
            local value = self:readInt()
            map[key] = value
        end
    end
    return map
end

function ByteBuffer:writeIntLongMap(map)
    if map == null then
        self:writeInt(0)
    else
        self:writeInt(table.mapSize(map));
        for key, value in pairs(map) do
            self:writeInt(key)
            self:writeLong(value)
        end
    end
    return self
end

function ByteBuffer:readIntLongMap()
    local map = {}
    local size = self:readInt()
    if size > 0 then
        for index = 1, size do
            local key = self:readInt()
            local value = self:readLong()
            map[key] = value
        end
    end
    return map
end

function ByteBuffer:writeIntStringMap(map)
    if map == null then
        self:writeInt(0)
    else
        self:writeInt(table.mapSize(map));
        for key, value in pairs(map) do
            self:writeInt(key)
            self:writeString(value)
        end
    end
    return self
end

function ByteBuffer:readIntStringMap()
    local map = {}
    local size = self:readInt()
    if size > 0 then
        for index = 1, size do
            local key = self:readInt()
            local value = self:readString()
            map[key] = value
        end
    end
    return map
end

function ByteBuffer:writeIntPacketMap(map, protocolId)
    if map == null then
        self:writeInt(0)
    else
        local protocolRegistration = ProtocolManager.getProtocol(protocolId)
        self:writeInt(table.mapSize(map));
        for key, value in pairs(map) do
            self:writeInt(key)
            protocolRegistration:write(self, value)
        end
    end
    return self
end

function ByteBuffer:readIntPacketMap(protocolId)
    local map = {}
    local size = self:readInt()
    if size > 0 then
        local protocolRegistration = ProtocolManager.getProtocol(protocolId)
        for index = 1, size do
            local key = self:readInt()
            local value = protocolRegistration:read(self)
            map[key] = value
        end
    end
    return map
end

function ByteBuffer:writeLongIntMap(map)
    if map == null then
        self:writeInt(0)
    else
        self:writeInt(table.mapSize(map));
        for key, value in pairs(map) do
            self:writeLong(key)
            self:writeInt(value)
        end
    end
    return self
end

function ByteBuffer:readLongIntMap()
    local map = {}
    local size = self:readInt()
    if size > 0 then
        for index = 1, size do
            local key = self:readLong()
            local value = self:readInt()
            map[key] = value
        end
    end
    return map
end

function ByteBuffer:writeLongLongMap(map)
    if map == null then
        self:writeInt(0)
    else
        self:writeInt(table.mapSize(map));
        for key, value in pairs(map) do
            self:writeLong(key)
            self:writeLong(value)
        end
    end
    return self
end

function ByteBuffer:readLongLongMap()
    local map = {}
    local size = self:readInt()
    if size > 0 then
        for index = 1, size do
            local key = self:readLong()
            local value = self:readLong()
            map[key] = value
        end
    end
    return map
end

function ByteBuffer:writeLongStringMap(map)
    if map == null then
        self:writeInt(0)
    else
        self:writeInt(table.mapSize(map));
        for key, value in pairs(map) do
            self:writeLong(key)
            self:writeString(value)
        end
    end
    return self
end

function ByteBuffer:readLongStringMap()
    local map = {}
    local size = self:readInt()
    if size > 0 then
        for index = 1, size do
            local key = self:readLong()
            local value = self:readString()
            map[key] = value
        end
    end
    return map
end

function ByteBuffer:writeLongPacketMap(map, protocolId)
    if map == null then
        self:writeInt(0)
    else
        local protocolRegistration = ProtocolManager.getProtocol(protocolId)
        self:writeInt(table.mapSize(map));
        for key, value in pairs(map) do
            self:writeLong(key)
            protocolRegistration:write(self, value)
        end
    end
    return self
end

function ByteBuffer:readLongPacketMap(protocolId)
    local map = {}
    local size = self:readInt()
    if size > 0 then
        local protocolRegistration = ProtocolManager.getProtocol(protocolId)
        for index = 1, size do
            local key = self:readLong()
            local value = protocolRegistration:read(self)
            map[key] = value
        end
    end
    return map
end

function ByteBuffer:writeStringIntMap(map)
    if map == null then
        self:writeInt(0)
    else
        self:writeInt(table.mapSize(map));
        for key, value in pairs(map) do
            self:writeString(key)
            self:writeInt(value)
        end
    end
    return self
end

function ByteBuffer:readStringIntMap()
    local map = {}
    local size = self:readInt()
    if size > 0 then
        for index = 1, size do
            local key = self:readString()
            local value = self:readInt()
            map[key] = value
        end
    end
    return map
end

function ByteBuffer:writeStringLongMap(map)
    if map == null then
        self:writeInt(0)
    else
        self:writeInt(table.mapSize(map));
        for key, value in pairs(map) do
            self:writeString(key)
            self:writeLong(value)
        end
    end
    return self
end

function ByteBuffer:readStringLongMap()
    local map = {}
    local size = self:readInt()
    if size > 0 then
        for index = 1, size do
            local key = self:readString()
            local value = self:readLong()
            map[key] = value
        end
    end
    return map
end

function ByteBuffer:writeStringStringMap(map)
    if map == null then
        self:writeInt(0)
    else
        self:writeInt(table.mapSize(map));
        for key, value in pairs(map) do
            self:writeString(key)
            self:writeString(value)
        end
    end
    return self
end

function ByteBuffer:readStringStringMap()
    local map = {}
    local size = self:readInt()
    if size > 0 then
        for index = 1, size do
            local key = self:readString()
            local value = self:readString()
            map[key] = value
        end
    end
    return map
end

function ByteBuffer:writeStringPacketMap(map, protocolId)
    if map == null then
        self:writeInt(0)
    else
        local protocolRegistration = ProtocolManager.getProtocol(protocolId)
        self:writeInt(table.mapSize(map));
        for key, value in pairs(map) do
            self:writeString(key)
            protocolRegistration:write(self, value)
        end
    end
    return self
end

function ByteBuffer:readStringPacketMap(protocolId)
    local map = {}
    local size = self:readInt()
    if size > 0 then
        local protocolRegistration = ProtocolManager.getProtocol(protocolId)
        for index = 1, size do
            local key = self:readString()
            local value = protocolRegistration:read(self)
            map[key] = value
        end
    end
    return map
end

return ByteBuffer
