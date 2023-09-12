-- @author godotg

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
    return 104
end

function SimpleObject:write(buffer, packet)
    if buffer:writePacketFlag(packet) then
        return
    end
    buffer:writeInt(packet.c)
    buffer:writeBoolean(packet.g)
end

function SimpleObject:read(buffer)
    if not(buffer:readBoolean()) then
        return nil
    end
    local packet = SimpleObject:new()
    local result0 = buffer:readInt()
    packet.c = result0
    local result1 = buffer:readBoolean()
    packet.g = result1
    return packet
end

return SimpleObject
