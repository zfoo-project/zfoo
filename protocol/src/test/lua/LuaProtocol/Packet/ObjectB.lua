-- @author godotg

local ObjectB = {}

function ObjectB:new(flag)
    local obj = {
        flag = flag -- boolean
    }
    setmetatable(obj, self)
    self.__index = self
    return obj
end

function ObjectB:protocolId()
    return 103
end

function ObjectB:write(buffer, packet)
    if buffer:writePacketFlag(packet) then
        return
    end
    buffer:writeBoolean(packet.flag)
end

function ObjectB:read(buffer)
    if not(buffer:readBoolean()) then
        return nil
    end
    local packet = ObjectB:new()
    local result0 = buffer:readBoolean()
    packet.flag = result0
    return packet
end

return ObjectB
