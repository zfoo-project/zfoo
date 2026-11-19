local protocols = {}

local ProtocolManager = {}

-- Table extension method: map size
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
    -- Write protocol ID
    buffer:writeShort(protocolId)
    -- Write packet body
    ProtocolManager.getProtocol(protocolId):write(buffer, packet)
end

function ProtocolManager.read(buffer)
    local protocolId = buffer:readShort()
    return ProtocolManager.getProtocol(protocolId):read(buffer)
end

function ProtocolManager.initProtocol()
    local EmptyObject = require("zfoolua.packet.EmptyObject")
    local ComplexObject = require("zfoolua.packet.ComplexObject")
    local NormalObject = require("zfoolua.packet.NormalObject")
    local ObjectA = require("zfoolua.packet.ObjectA")
    local ObjectB = require("zfoolua.packet.ObjectB")
    local SimpleObject = require("zfoolua.packet.SimpleObject")
    protocols[0] = EmptyObject
    protocols[100] = ComplexObject
    protocols[101] = NormalObject
    protocols[102] = ObjectA
    protocols[103] = ObjectB
    protocols[104] = SimpleObject
end

return ProtocolManager