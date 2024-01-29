function Protocols.{}:protocolId()
    return {}
end

function Protocols.{}:protocolName()
    return "{}"
end

function Protocols.{}:__tostring()
    return table.serializeTableToJson(self)
end