${protocol_note}
class ${protocol_name}:
    ${protocol_field_definition}

    def protocolId(self):
        return ${protocol_id}

    @classmethod
    def write(cls, buffer, packet):
        if packet is None:
            buffer.writeInt(0)
            return
        ${protocol_write_serialization}
        pass

    @classmethod
    def read(cls, buffer):
        length = buffer.readInt()
        if length == 0:
            return None
        beforeReadIndex = buffer.getReadOffset()
        packet = ${protocol_name}()
        ${protocol_read_deserialization}
        if length > 0:
            buffer.setReadOffset(beforeReadIndex + length)
        return packet