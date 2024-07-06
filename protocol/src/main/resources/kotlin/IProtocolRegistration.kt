${protocol_root_path}

interface IProtocolRegistration {
    fun protocolId(): Short
    fun write(buffer: ByteBuffer, packet: Any?)
    fun read(buffer: ByteBuffer): Any
}