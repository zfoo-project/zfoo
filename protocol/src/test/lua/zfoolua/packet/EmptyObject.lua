
local EmptyObject = {}

function EmptyObject:new()
    local obj = {
        
    }
    setmetatable(obj, self)
    self.__index = self
    return obj
end

function EmptyObject:protocolId()
    return 0
end

function EmptyObject:write(buffer, packet)
    if packet == nil then
        buffer:writeInt(0)
        return
    end
    buffer:writeInt(-1)
end

function EmptyObject:read(buffer)
    local length = buffer:readInt()
    if length == 0 then
        return nil
    end
    local beforeReadIndex = buffer:getReadOffset()
    local packet = EmptyObject:new()
    
    if length > 0 then
        buffer:setReadOffset(beforeReadIndex + length)
    end
    return packet
end

return EmptyObject
