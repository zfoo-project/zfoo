${protocol_note}
class ${protocol_name} {
    ${protocol_field_definition}

    static PROTOCOL_ID = ${protocol_id};

    protocolId() {
        return ${protocol_name}.PROTOCOL_ID;
    }

    static write(buffer, packet) {
        if (packet === null) {
            buffer.writeInt(0);
            return;
        }
        ${protocol_write_serialization}
    }

    static read(buffer) {
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
    }
}