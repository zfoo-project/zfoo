class ${protocol_name}Registration : IProtocolRegistration {
    func write(_ buffer: ByteBuffer, _ packet: Any?) {
        if (packet == nil) {
            buffer.writeInt(0)
            return
        }
        let message = packet as! ${protocol_name}
        ${protocol_write_serialization}
    }
    
    func read(_ buffer: ByteBuffer) -> Any {
        let length = buffer.readInt()
        let packet = ${protocol_name}()
        if (length == 0) {
            return packet
        }
        let beforeReadIndex = buffer.getReadOffset()
        ${protocol_read_deserialization}
        if (length > 0) {
            buffer.setReadOffset(beforeReadIndex + length)
        }
        return packet
    }
}
