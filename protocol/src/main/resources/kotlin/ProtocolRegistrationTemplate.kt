companion object {
    @JvmField
    val registration${protocol_name}: IProtocolRegistration = object : IProtocolRegistration {
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

        override fun read(buffer: ByteBuffer): Any? {
            val length = buffer.readInt()
            if (length == 0) {
                return null
            }
            val beforeReadIndex = buffer.readOffset()
            val packet = ${protocol_name}()
            ${protocol_read_deserialization}
            if (length > 0) {
                buffer.setReadOffset(beforeReadIndex + length)
            }
            return packet
        }
    }
}