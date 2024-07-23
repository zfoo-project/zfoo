class ${protocol_name}Registration
    def protocolId()
        return ${protocol_id}
    end

    def write(buffer, packet)
        if packet.nil?
            buffer.writeInt(0)
            return
        end
        ${protocol_write_serialization}
    end

    def read(buffer)
        length = buffer.readInt()
        if length == 0
            return nil
        end
        beforeReadIndex = buffer.getReadOffset()
        packet = ${protocol_name}.new()
        ${protocol_read_deserialization}
        if length > 0
            buffer.setReadOffset(beforeReadIndex + length)
        end
        return packet
    end
end