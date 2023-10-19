local ByteBuffer = require("zfoolua.ByteBuffer")
local ProtocolManager = require("zfoolua.ProtocolManager")


-------------------------------------ProtocolManager的测试-------------------------------------
function complexObjectTest(bytes)
    print("complex size ", #bytes)
    ProtocolManager.initProtocol()

    local byteBuffer = ByteBuffer:new()
    byteBuffer:writeBuffer(bytes)
    local packet = ProtocolManager.read(byteBuffer)

    local newByteBuffer = ByteBuffer:new()
    ProtocolManager.write(newByteBuffer, packet)
    assert(#byteBuffer.buffer <= #newByteBuffer.buffer)

    -- set和map是无序的，所以有的时候输入和输出的字节流有可能不一致，但是长度一定是一致的
    --for i = 1, #byteBuffer.buffer do
    --    print(i)
    --    assert(byteBuffer.buffer[i] == newByteBuffer.buffer[i], i)
    --end

    local newPacket = ProtocolManager.read(newByteBuffer)
    return packet
end

function normalObjectTest(bytes)
    ProtocolManager.initProtocol()

    local byteBuffer = ByteBuffer:new()
    byteBuffer:writeBuffer(bytes)
    local packet = ProtocolManager.read(byteBuffer)

    local newByteBuffer = ByteBuffer:new()
    ProtocolManager.write(newByteBuffer, packet)

    -- set和map是无序的，所以有的时候输入和输出的字节流有可能不一致，但是长度一定是一致的
    --for i = 1, #byteBuffer.buffer do
    --    print(i)
    --    assert(byteBuffer.buffer[i] == newByteBuffer.buffer[i], i)
    --end

    local newPacket = ProtocolManager.read(newByteBuffer)
    print("normal source size ", #bytes)
    print("normal target size ", newByteBuffer:getLen())
    return packet
end


-------------------------------------ByteBuffer的测试-------------------------------------
function byteBufferTest()
    local byteBuffer = ByteBuffer:new()

    byteBuffer:writeBoolean(true)
    byteBuffer:writeBoolean(false)
    assert(byteBuffer:readBoolean() == true)
    assert(byteBuffer:readBoolean() == false)
    byteBuffer:setWriteOffset(1)
    byteBuffer:setReadOffset(1)

    byteBuffer:writeUByte(99)
    byteBuffer:writeUByte(128)
    assert(byteBuffer:readUByte() == 99)
    assert(byteBuffer:readUByte() == 128)
    byteBuffer:setWriteOffset(1)
    byteBuffer:setReadOffset(1)

    byteBuffer:writeByte(127)
    byteBuffer:writeByte(-128)
    assert(byteBuffer:readByte() == 127)
    assert(byteBuffer:readByte() == -128)
    byteBuffer:setWriteOffset(1)
    byteBuffer:setReadOffset(1)

    byteBuffer:writeShort(32767)
    byteBuffer:writeShort(0)
    byteBuffer:writeShort(-32768)
    assert(byteBuffer:readShort() == 32767)
    assert(byteBuffer:readShort() == 0)
    assert(byteBuffer:readShort() == -32768)
    byteBuffer:setWriteOffset(1)
    byteBuffer:setReadOffset(1)

    byteBuffer:writeInt(2147483647)
    byteBuffer:writeInt(-999999)
    byteBuffer:writeInt(0)
    byteBuffer:writeInt(999999)
    byteBuffer:writeInt(-2147483648)
    assert(byteBuffer:readInt() == 2147483647)
    assert(byteBuffer:readInt() == -999999)
    assert(byteBuffer:readInt() == 0)
    assert(byteBuffer:readInt() == 999999)
    assert(byteBuffer:readInt() == -2147483648)
    byteBuffer:setWriteOffset(1)
    byteBuffer:setReadOffset(1)

    byteBuffer:writeLuaNumber(1234.5678)
    byteBuffer:writeLuaNumber(0)
    byteBuffer:writeLuaNumber(-2147483648)
    assert(math.abs(byteBuffer:readLuaNumber() - 1234.5678) < 0.001)
    assert(byteBuffer:readLuaNumber() == 0)
    assert(byteBuffer:readLuaNumber() == -2147483648)
    byteBuffer:setWriteOffset(1)
    byteBuffer:setReadOffset(1)

    byteBuffer:writeLong(math.mininteger)
    byteBuffer:writeLong(-9223372036854775807)
    byteBuffer:writeLong(-9999999999999999)
    byteBuffer:writeLong(-99999999)
    byteBuffer:writeLong(0)
    byteBuffer:writeLong(99999999)
    byteBuffer:writeLong(9999999999999999)
    byteBuffer:writeLong(9223372036854775807)
    assert(byteBuffer:readLong() == math.mininteger)
    assert(byteBuffer:readLong() == -9223372036854775807)
    assert(byteBuffer:readLong() == -9999999999999999)
    assert(byteBuffer:readLong() == -99999999)
    assert(byteBuffer:readLong() == 0)
    assert(byteBuffer:readLong() == 99999999)
    assert(byteBuffer:readLong() == 9999999999999999)
    assert(byteBuffer:readLong() == 9223372036854775807)
    byteBuffer:setWriteOffset(1)
    byteBuffer:setReadOffset(1)

    byteBuffer:writeFloat(0x0.000002P-126)
    byteBuffer:writeFloat(0)
    byteBuffer:writeFloat(1234.5678)
    byteBuffer:writeFloat(0x1.fffffeP+127)
    assert(byteBuffer:readFloat() == 0x0.000002P-126)
    assert(byteBuffer:readFloat() == 0)
    assert(math.abs(byteBuffer:readFloat() - 1234.5678) < 0.001)
    assert(byteBuffer:readFloat() == 0x1.fffffeP+127)
    byteBuffer:setWriteOffset(1)
    byteBuffer:setReadOffset(1)

    byteBuffer:writeDouble(0x0.0000000000001P-1022)
    byteBuffer:writeDouble(0)
    byteBuffer:writeDouble(1234.5678)
    byteBuffer:writeDouble(0x1.fffffffffffffP+1023)
    assert(byteBuffer:readDouble() == 0x0.0000000000001P-1022)
    assert(byteBuffer:readDouble() == 0)
    assert(math.abs(byteBuffer:readDouble() - 1234.5678) < 0.001)
    assert(byteBuffer:readDouble() == 0x1.fffffffffffffP+1023)
    byteBuffer:setWriteOffset(1)
    byteBuffer:setReadOffset(1)

    local s = "你好 hello world"
    byteBuffer:writeString(s)
    assert(byteBuffer:readString() == s)

    byteBuffer:setWriteOffset(0)
    byteBuffer:setReadOffset(0)


    print("----------------------------------------------------")
    print("Hello")
end
