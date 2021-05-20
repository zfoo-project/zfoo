local ByteBuffer = require("LuaProtocol.Buffer.ByteBuffer")

protocols = {}

ProtocolManager = {}

-- table扩展方法，后去set和map的大小
function table.setSize(set)
    local size = 0
    for _,_ in pairs(set) do
        size = size + 1
    end
    return size
end


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

function ProtocolManager.write(byteBuffer, packet)
    local protocolId = packet:protocolId()
    -- 写入协议号
    byteBuffer:writeShort(protocolId)
    -- 写入包体
    ProtocolManager.getProtocol(protocolId):write(byteBuffer, packet)
end

function ProtocolManager.read(byteBuffer)
    local protocolId = byteBuffer:readShort()
    return ProtocolManager.getProtocol(protocolId):read(byteBuffer)
end

-- C#传进来的byte数组到lua里就会变成string
function readBytes(bytes)
    local byteBuffer = ByteBuffer:new()
    byteBuffer:writeBuffer(bytes)
    local packet = ProtocolManager.read(byteBuffer)
    return packet
end

