
local SimpleObject = {}

function SimpleObject:new()
    local obj = {
        c = 0, -- int
        g = false -- bool
    }
    setmetatable(obj, self)
    self.__index = self
    return obj
end

function SimpleObject:protocolId()
    return 104
end

function SimpleObject:protocolName()
    return "SimpleObject"
end

function SimpleObject:__tostring()
    return table.serializeTableToJson(self)
end

function SimpleObject:write(buffer, packet)
    if packet == nil then
        buffer:writeInt(0)
        return
    end
    buffer:writeInt(-1)
    buffer:writeInt(packet.c)
    buffer:writeBoolean(packet.g)
end

function SimpleObject:read(buffer)
    local length = buffer:readInt()
    if length == 0 then
        return nil
    end
    local beforeReadIndex = buffer:getReadOffset()
    local packet = SimpleObject:new()
    local result0 = buffer:readInt()
    packet.c = result0
    local result1 = buffer:readBoolean()
    packet.g = result1
    if length > 0 then
        buffer:setReadOffset(beforeReadIndex + length)
    end
    return packet
end

return SimpleObject