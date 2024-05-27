${protocol_name}.PROTOCOL_ID = ${protocol_id};

${protocol_name}.prototype.protocolId = function() {
    return ${protocol_name}.PROTOCOL_ID;
};

${protocol_name}.write = function(buffer, packet) {
    if (packet === null) {
        buffer.writeInt(0);
        return;
    }
    ${protocol_write_serialization}
};

${protocol_name}.read = function(buffer) {
    const length = buffer.readInt();
    if (length === 0) {
        return null;
    }
    const beforeReadIndex = buffer.getReadOffset();
    const packet = new ${protocol_name}();
    ${protocol_read_deserialization}
    if (length > 0) {
        buffer.setReadOffset(beforeReadIndex + length);
    }
    return packet;
};