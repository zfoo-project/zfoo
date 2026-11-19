local protocols = {}

local ProtocolManager = {}

-- table extension method for getting map size
function table.mapSize(map)
    local size = 0
    for _,_ in pairs(map) do
        size = size + 1
    end
    return size
end

function ProtocolManager.getProtocol(protocolId)
    local protocol = protocols[protocolId]
    if protocol == nil then
        error("[protocolId:" + protocolId + "] not exist")
    end
    return protocol
end

function ProtocolManager.write(buffer, packet)
    local protocolId = packet:protocolId()
    -- write protocol id to buffer
    buffer:writeShort(protocolId)
    -- write packet
    ProtocolManager.getProtocol(protocolId):write(buffer, packet)
end

function ProtocolManager.read(buffer)
    local protocolId = buffer:readShort()
    return ProtocolManager.getProtocol(protocolId):read(buffer)
end

function ProtocolManager.initProtocol()
    ${protocol_imports}
    ${protocol_manager_registrations}
end

return ProtocolManager
