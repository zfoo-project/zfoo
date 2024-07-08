export class ${protocol_name}Registration {
    protocolId() {
        return ${protocol_id};
    }

    write(buffer, packet) {
        if (packet === null) {
            buffer.writeInt(0);
            return;
        }
        ${protocol_write_serialization}
    }

    read(buffer) {
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