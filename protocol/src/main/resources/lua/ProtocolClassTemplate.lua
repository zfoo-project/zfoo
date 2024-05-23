${protocol_note}
local ${protocol_name} = {}

function ${protocol_name}:new()
    local obj = {
        ${protocol_field_definition}
    }
    setmetatable(obj, self)
    self.__index = self
    return obj
end