
local ObjectB = {}

function ObjectB:new()
    local obj = {
        flag = false, -- bool
        innerCompatibleValue = 0 -- int
    }
    setmetatable(obj, self)
    self.__index = self
    return obj
end

function ObjectB:protocolId()
    return 103
end

function ObjectB:write(buffer, packet)
    if packet == nil then
        buffer:writeInt(0)
        return
    end
    local beforeWriteIndex = buffer:getWriteOffset()
    buffer:writeInt(4)
    buffer:writeBoolean(packet.flag)
    buffer:writeInt(packet.innerCompatibleValue)
    buffer:adjustPadding(4, beforeWriteIndex)
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
    if buffer:compatibleRead(beforeReadIndex, length) then
        local result1 = buffer:readInt()
        packet.innerCompatibleValue = result1
    end
    if length > 0 then
        buffer:setReadOffset(beforeReadIndex + length)
    end
    return packet
end

return ObjectB
