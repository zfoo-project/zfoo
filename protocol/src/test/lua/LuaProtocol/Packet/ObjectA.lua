-- @author godotg

local ObjectA = {}

function ObjectA:new(a, m, objectB)
    local obj = {
        a = a, -- int
        m = m, -- java.util.Map<java.lang.Integer, java.lang.String>
        objectB = objectB -- com.zfoo.protocol.packet.ObjectB
    }
    setmetatable(obj, self)
    self.__index = self
    return obj
end

function ObjectA:protocolId()
    return 102
end

function ObjectA:write(buffer, packet)
    if buffer:writePacketFlag(packet) then
        return
    end
    buffer:writeInt(packet.a)
    buffer:writeIntStringMap(packet.m)
    buffer:writePacket(packet.objectB, 103)
end

function ObjectA:read(buffer)
    if not(buffer:readBoolean()) then
        return nil
    end
    local packet = ObjectA:new()
    local result0 = buffer:readInt()
    packet.a = result0
    local map1 = buffer:readIntStringMap()
    packet.m = map1
    local result2 = buffer:readPacket(103)
    packet.objectB = result2
    return packet
end

return ObjectA
