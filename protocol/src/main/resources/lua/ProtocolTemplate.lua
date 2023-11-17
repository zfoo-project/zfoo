{}
local {} = {}

function {}:new()
    local obj = {
        {}
    }
    setmetatable(obj, self)
    self.__index = self
    return obj
end

function {}:protocolId()
    return {}
end

function {}:protocolName()
    return {}
end

function {}:__tostring()
    local jsonTemplate = "{}"
    local result = string.format(jsonTemplate, {})
    return result
end

function {}:write(buffer, packet)
    if packet == nil then
        buffer:writeInt(0)
        return
    end
    {}
end

function {}:read(buffer)
    local length = buffer:readInt()
    if length == 0 then
        return nil
    end
    local beforeReadIndex = buffer:getReadOffset()
    local packet = {}:new()
    {}
    if length > 0 then
        buffer:setReadOffset(beforeReadIndex + length)
    end
    return packet
end

return {}
