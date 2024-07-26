class ${protocol_name}Registration implements IProtocolRegistration<${protocol_name}> {
  @override
  int protocolId() {
    return ${protocol_id};
  }

  @override
  void write(IByteBuffer buffer, ${protocol_name}? packet) {
    if (packet == null) {
      buffer.writeInt(0);
      return;
    }
    ${protocol_write_serialization}
  }


  @override
  ${protocol_name} read(IByteBuffer buffer) {
    var length = buffer.readInt();
    var packet = ${protocol_name}();
    if (length == 0) {
      return packet;
    }
    var beforeReadIndex = buffer.getReadOffset();
    ${protocol_read_deserialization}
    if (length > 0) {
      buffer.setReadOffset(beforeReadIndex + length);
    }
    return packet;
  }
}