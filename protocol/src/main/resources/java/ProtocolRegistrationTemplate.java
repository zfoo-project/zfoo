class ${protocol_name}Registration implements IProtocolRegistration {
    @Override
    public short protocolId() {
        return ${protocol_id};
    }

    @Override
    public void write(ByteBuffer buffer, Object packet) {
        if (packet == null) {
            buffer.writeInt(0);
            return;
        }
        ${protocol_name} message = (${protocol_name}) packet;
        ${protocol_write_serialization}
    }

    @Override
    public Object read(ByteBuffer buffer) {
        var length = buffer.readInt();
        if (length == 0) {
            return null;
        }
        var beforeReadIndex = buffer.getReadOffset();
        var packet = new ${protocol_name}();
        ${protocol_read_deserialization}
        if (length > 0) {
            buffer.setReadOffset(beforeReadIndex + length);
        }
        return packet;
    }
};