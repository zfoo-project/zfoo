protocols = {}

ProtocolManager = {}

-- table扩展方法，map的大小
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
        error("[protocolId:" + protocolId + "]协议不存在")
    end
    return protocol
end

function ProtocolManager.write(buffer, packet)
    local protocolId = packet:protocolId()
    -- 写入协议号
    buffer:writeShort(protocolId)
    -- 写入包体
    ProtocolManager.getProtocol(protocolId):write(buffer, packet)
end

function ProtocolManager.read(buffer)
    local protocolId = buffer:readShort()
    return ProtocolManager.getProtocol(protocolId):read(buffer)
end

function initProtocol()
    local ComplexObject = require("LuaProtocol.Packet.ComplexObject")
    local NormalObject = require("LuaProtocol.Packet.NormalObject")
    local ObjectA = require("LuaProtocol.Packet.ObjectA")
    local ObjectB = require("LuaProtocol.Packet.ObjectB")
    local SimpleObject = require("LuaProtocol.Packet.SimpleObject")
    protocols[100] = ComplexObject
    protocols[101] = NormalObject
    protocols[102] = ObjectA
    protocols[103] = ObjectB
    protocols[104] = SimpleObject
end

ProtocolManager.initProtocol = initProtocol
return ProtocolManager
