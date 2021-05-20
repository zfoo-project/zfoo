-- @author jaysunxiao
-- @version 1.0
-- @since 2021-03-27 15:18

local SimpleObject = {}

function SimpleObject:new(c, g)
    local obj = {
        c = c, -- int
        g = g -- boolean
    }
    setmetatable(obj, self)
    self.__index = self
    return obj
end

function SimpleObject:protocolId()
    return 1163
end

function SimpleObject:write(byteBuffer, packet)
    if packet == null then
        byteBuffer:writeBoolean(false)
        return
    end
    byteBuffer:writeBoolean(true)
    byteBuffer:writeInt(packet.c)
    byteBuffer:writeBoolean(packet.g)
end

function SimpleObject:read(byteBuffer)
    if not(byteBuffer:readBoolean()) then
        return nil
    end
    local packet = SimpleObject:new()
    local result0 = byteBuffer:readInt()
    packet.c = result0
    local result1 = byteBuffer:readBoolean()
    packet.g = result1
    return packet
end

return SimpleObject
