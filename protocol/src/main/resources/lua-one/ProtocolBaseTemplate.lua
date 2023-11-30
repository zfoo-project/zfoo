function Protocols.{}:protocolId()
    return {}
end

function Protocols.{}:protocolName()
    return Protocols.{}
end

function Protocols.{}:__tostring()
    local jsonTemplate = "{}"
    local result = string.format(jsonTemplate, {})
    return result
end