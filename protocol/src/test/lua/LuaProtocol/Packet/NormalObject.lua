-- @author godotg

local NormalObject = {}

function NormalObject:new(a, aaa, b, c, d, e, f, g, jj, kk, l, ll, lll, llll, m, mm, s, ssss)
    local obj = {
        a = a, -- byte
        aaa = aaa, -- byte[]
        b = b, -- short
        c = c, -- int
        d = d, -- long
        e = e, -- float
        f = f, -- double
        g = g, -- boolean
        jj = jj, -- java.lang.String
        kk = kk, -- com.zfoo.protocol.packet.ObjectA
        l = l, -- java.util.List<java.lang.Integer>
        ll = ll, -- java.util.List<java.lang.Long>
        lll = lll, -- java.util.List<com.zfoo.protocol.packet.ObjectA>
        llll = llll, -- java.util.List<java.lang.String>
        m = m, -- java.util.Map<java.lang.Integer, java.lang.String>
        mm = mm, -- java.util.Map<java.lang.Integer, com.zfoo.protocol.packet.ObjectA>
        s = s, -- java.util.Set<java.lang.Integer>
        ssss = ssss -- java.util.Set<java.lang.String>
    }
    setmetatable(obj, self)
    self.__index = self
    return obj
end

function NormalObject:protocolId()
    return 101
end

function NormalObject:write(buffer, packet)
    if buffer:writePacketFlag(packet) then
        return
    end
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
end

function NormalObject:read(buffer)
    if not(buffer:readBoolean()) then
        return nil
    end
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
    return packet
end

return NormalObject
