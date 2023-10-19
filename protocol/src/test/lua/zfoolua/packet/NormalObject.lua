
local NormalObject = {}

function NormalObject:new()
    local obj = {
        a = 0, -- byte
        aaa = {}, -- byte[]
        b = 0, -- short
        c = 0, -- int
        d = 0, -- long
        e = 0, -- float
        f = 0, -- double
        g = false, -- bool
        jj = "", -- string
        kk = nil, -- ObjectA
        l = {}, -- List<int>
        ll = {}, -- List<long>
        lll = {}, -- List<ObjectA>
        llll = {}, -- List<string>
        m = {}, -- Dictionary<int, string>
        mm = {}, -- Dictionary<int, ObjectA>
        s = {}, -- HashSet<int>
        ssss = {}, -- HashSet<string>
        outCompatibleValue = 0 -- int
    }
    setmetatable(obj, self)
    self.__index = self
    return obj
end

function NormalObject:protocolId()
    return 101
end

function NormalObject:write(buffer, packet)
    if packet == nil then
        buffer:writeInt(0)
        return
    end
    local beforeWriteIndex = buffer:getWriteOffset()
    buffer:writeInt(854)
    buffer:writeByte(packet.a)
    buffer:writeByteArray(packet.aaa)
    buffer:writeShort(packet.b)
    buffer:writeInt(packet.c)
    buffer:writeLong(packet.d)
    buffer:writeFloat(packet.e)
    buffer:writeDouble(packet.f)
    buffer:writeBoolean(packet.g)
    buffer:writeString(packet.jj)
    buffer:writePacket(packet.kk, 102)
    buffer:writeIntArray(packet.l)
    buffer:writeLongArray(packet.ll)
    buffer:writePacketArray(packet.lll, 102)
    buffer:writeStringArray(packet.llll)
    buffer:writeIntStringMap(packet.m)
    buffer:writeIntPacketMap(packet.mm, 102)
    buffer:writeIntArray(packet.s)
    buffer:writeStringArray(packet.ssss)
    buffer:writeInt(packet.outCompatibleValue)
    buffer:adjustPadding(854, beforeWriteIndex)
end

function NormalObject:read(buffer)
    local length = buffer:readInt()
    if length == 0 then
        return nil
    end
    local beforeReadIndex = buffer:getReadOffset()
    local packet = NormalObject:new()
    local result0 = buffer:readByte()
    packet.a = result0
    local array1 = buffer:readByteArray()
    packet.aaa = array1
    local result2 = buffer:readShort()
    packet.b = result2
    local result3 = buffer:readInt()
    packet.c = result3
    local result4 = buffer:readLong()
    packet.d = result4
    local result5 = buffer:readFloat()
    packet.e = result5
    local result6 = buffer:readDouble()
    packet.f = result6
    local result7 = buffer:readBoolean()
    packet.g = result7
    local result8 = buffer:readString()
    packet.jj = result8
    local result9 = buffer:readPacket(102)
    packet.kk = result9
    local list10 = buffer:readIntArray()
    packet.l = list10
    local list11 = buffer:readLongArray()
    packet.ll = list11
    local list12 = buffer:readPacketArray(102)
    packet.lll = list12
    local list13 = buffer:readStringArray()
    packet.llll = list13
    local map14 = buffer:readIntStringMap()
    packet.m = map14
    local map15 = buffer:readIntPacketMap(102)
    packet.mm = map15
    local set16 = buffer:readIntArray()
    packet.s = set16
    local set17 = buffer:readStringArray()
    packet.ssss = set17
    if buffer:compatibleRead(beforeReadIndex, length) then
        local result18 = buffer:readInt()
        packet.outCompatibleValue = result18
    end
    if length > 0 then
        buffer:setReadOffset(beforeReadIndex + length)
    end
    return packet
end

return NormalObject
