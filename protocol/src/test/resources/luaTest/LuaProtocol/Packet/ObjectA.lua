-- @author jaysunxiao
-- @version 1.0
-- @since 2017 10.12 15:39

local ProtocolManager = require("LuaProtocol.ProtocolManager")

local ObjectA = {}

function ObjectA:new(a, m, objectB)
    local obj = {
        a = a, -- int
        m = m, -- java.util.Map<java.lang.Integer, java.lang.String>
        objectB = objectB -- com.zfoo.protocol.packet.ObjectB
    }
    setmetatable(obj, self)
    self.__index = self
    return obj
end

function ObjectA:protocolId()
    return 1116
end

function ObjectA:write(byteBuffer, packet)
    if packet == null then
        byteBuffer:writeBoolean(false)
        return
    end
    byteBuffer:writeBoolean(true)
    byteBuffer:writeInt(packet.a)
    if packet.m == null then
        byteBuffer:writeInt(0)
    else
        byteBuffer:writeInt(table.mapSize(packet.m))
        for key0, value1 in pairs(packet.m) do
            byteBuffer:writeInt(key0)
            byteBuffer:writeString(value1)
        end
    end
    ProtocolManager.getProtocol(1117):write(byteBuffer, packet.objectB)
end

function ObjectA:read(byteBuffer)
    if not(byteBuffer:readBoolean()) then
        return nil
    end
    local packet = ObjectA:new()
    local result2 = byteBuffer:readInt()
    packet.a = result2
    local result3 = {}
    local size4 = byteBuffer:readInt()
    if size4 > 0 then
        for index5 = 1, size4 do
            local result6 = byteBuffer:readInt()
            local result7 = byteBuffer:readString()
            result3[result6] = result7
        end
    end
    packet.m = result3
    local result8 = ProtocolManager.getProtocol(1117):read(byteBuffer)
    packet.objectB = result8
    return packet
end

return ObjectA
