
local ObjectB = {}

function ObjectB:new()
    local obj = {
        flag = false -- bool
    }
    setmetatable(obj, self)
    self.__index = self
    return obj
end

function ObjectB:protocolId()
    return 103
end

function ObjectB:protocolName()
    return "ObjectB"
end

function ObjectB:__tostring()
    return table.serializeTableToJson(self)
end

function ObjectB:write(buffer, packet)
    if packet == nil then
        buffer:writeInt(0)
        return
    end
    buffer:writeInt(-1)
    buffer:writeBoolean(packet.flag)
end

function ObjectB:read(buffer)
    local length = buffer:readInt()
    if length == 0 then
        return nil
    end
    local beforeReadIndex = buffer:getReadOffset()
    local packet = ObjectB:new()
    local result0 = buffer:readBoolean()
    packet.flag = result0
    if length > 0 then
        buffer:setReadOffset(beforeReadIndex + length)
    end
    return packet
end

return ObjectB