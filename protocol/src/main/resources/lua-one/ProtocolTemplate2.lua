function Protocols.{}:protocolId()
    return {}
end

function Protocols.{}:protocolName()
    return {}
end

function Protocols.{}:__tostring()
    local jsonTemplate = "{}"
    local result = string.format(jsonTemplate, {})
    return result
end

function Protocols.{}:write(buffer, packet)
    if packet == nil then
        buffer:writeInt(0)
        return
    end
    {}
end

function Protocols.{}:read(buffer)
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