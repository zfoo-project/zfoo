export class ${protocol_name}Registration implements IProtocolRegistration<${protocol_name}> {
    protocolId(): number {
        return ${protocol_id};
    }

    write(buffer: IByteBuffer, packet: ${protocol_name} | null) {
        if (packet === null) {
            buffer.writeInt(0);
            return;
        }
        ${protocol_write_serialization}
    }

    read(buffer: IByteBuffer): ${protocol_name} | null {
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