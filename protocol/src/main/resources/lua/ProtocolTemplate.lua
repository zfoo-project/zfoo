{}
local {} = {}

function {}:new({})
    local obj = {
        {}
    }
    setmetatable(obj, self)
    self.__index = self
    return obj
end

function {}:protocolId()
    return {}
end

function {}:write(buffer, packet)
    if buffer:writePacketFlag(packet) then
        return
    end
    {}
end

function {}:read(buffer)
    if not(buffer:readBoolean()) then
        return nil
    end
    local packet = {}:new()
    {}
    return packet
end

return {}
