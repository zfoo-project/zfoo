function ${protocol_name}:protocolId()
    return ${protocol_id}
end

function ${protocol_name}:protocolName()
    return "${protocol_name}"
end

function ${protocol_name}:__tostring()
    return table.serializeTableToJson(self)
end

function ${protocol_name}:write(buffer, packet)
    if packet == nil then
        buffer:writeInt(0)
        return
    end
    ${protocol_write_serialization}
end

function ${protocol_name}:read(buffer)
    local length = buffer:readInt()
    if length == 0 then
        return nil
    end
    local beforeReadIndex = buffer:getReadOffset()
    local packet = ${protocol_name}:new()
    ${protocol_read_deserialization}
    if length > 0 then
        buffer:setReadOffset(beforeReadIndex + length)
    end
    return packet
end