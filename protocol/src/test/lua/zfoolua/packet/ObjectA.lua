
local ObjectA = {}

function ObjectA:new()
    local obj = {
        a = 0, -- int
        m = {}, -- Dictionary<int, string>
        objectB = nil -- ObjectB
    }
    setmetatable(obj, self)
    self.__index = self
    return obj
end

function ObjectA:protocolId()
    return 102
end

function ObjectA:protocolName()
    return "ObjectA"
end

function ObjectA:__tostring()
    return table.serializeTableToJson(self)
end

function ObjectA:write(buffer, packet)
    if packet == nil then
        buffer:writeInt(0)
        return
    end
    buffer:writeInt(-1)
    buffer:writeInt(packet.a)
    buffer:writeIntStringMap(packet.m)
    buffer:writePacket(packet.objectB, 103)
end

function ObjectA:read(buffer)
    local length = buffer:readInt()
    if length == 0 then
        return nil
    end
    local beforeReadIndex = buffer:getReadOffset()
    local packet = ObjectA:new()
    local result0 = buffer:readInt()
    packet.a = result0
    local map1 = buffer:readIntStringMap()
    packet.m = map1
    local result2 = buffer:readPacket(103)
    packet.objectB = result2
    if length > 0 then
        buffer:setReadOffset(beforeReadIndex + length)
    end
    return packet
end

return ObjectA