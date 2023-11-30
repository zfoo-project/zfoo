function Protocols.{}:read(buffer)
    local length = buffer:readInt()
    if length == 0 then
        return nil
    end
    local beforeReadIndex = buffer:getReadOffset()
    local packet = Protocols.{}:new()
    {}
    if length > 0 then
        buffer:setReadOffset(beforeReadIndex + length)
    end
    return packet
end