function Protocols.{}:write(buffer, packet)
    if packet == nil then
        buffer:writeInt(0)
        return
    end
    {}
end