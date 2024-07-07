object ${protocol_name}Registration extends IProtocolRegistration {
  override def protocolId: Short = ${protocol_id}

  override def write(buffer: ByteBuffer, packet: Any): Unit = {
    if (packet == null) {
      buffer.writeInt(0)
      return
    }
    val message = packet.asInstanceOf[${protocol_name}]
    ${protocol_write_serialization}
  }

  override def read(buffer: ByteBuffer): AnyRef = {
    val length: Int = buffer.readInt
    if (length == 0) return null
    val beforeReadIndex: Int = buffer.getReadOffset
    val packet: ${protocol_name} = new ${protocol_name}
    ${protocol_read_deserialization}
    if (length > 0) buffer.setReadOffset(beforeReadIndex + length)
    packet
  }
}