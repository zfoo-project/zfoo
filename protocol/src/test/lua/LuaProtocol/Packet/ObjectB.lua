-- @author jaysunxiao
-- @version 1.0
-- @since 2017 10.12 15:39

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
    return 1117
end

function ObjectB:write(byteBuffer, packet)
    if packet == null then
        byteBuffer:writeBoolean(false)
        return
    end
    byteBuffer:writeBoolean(true)
    byteBuffer:writeBoolean(packet.flag)
end

function ObjectB:read(byteBuffer)
    if not(byteBuffer:readBoolean()) then
        return nil
    end
    local packet = ObjectB:new()
    local result0 = byteBuffer:readBoolean()
    packet.flag = result0
    return packet
end

return ObjectB
