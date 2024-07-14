class ${protocol_name}Registration : IProtocolRegistration {
    override fun protocolId(): Short {
        return ${protocol_id}
    }

    override fun write(buffer: ByteBuffer, packet: Any?) {
        if (packet == null) {
            buffer.writeInt(0)
            return
        }
        val message = packet as ${protocol_name}
        ${protocol_write_serialization}
    }

    override fun read(buffer: ByteBuffer): Any {
        val length = buffer.readInt()
        val packet = ${protocol_name}()
        if (length == 0) {
            return packet
        }
        val beforeReadIndex = buffer.getReadOffset()
        ${protocol_read_deserialization}
        if (length > 0) {
            buffer.setReadOffset(beforeReadIndex + length)
        }
        return packet
    }
}