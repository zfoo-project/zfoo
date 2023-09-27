{}
{}
class {} {

    {}

    static PROTOCOL_ID: number = {};

    protocolId(): number {
        return {}.PROTOCOL_ID;
    }

    static write(buffer: any, packet: {} | null) {
        if (packet === null) {
            buffer.writeInt(0);
            return;
        }
        {}
    }

    static read(buffer: any): {} | null {
        const length = buffer.readInt();
        if (length === 0) {
            return null;
        }
        const beforeReadIndex = buffer.getReadOffset();
        const packet = new {}();
        {}
        if (length > 0) {
            buffer.setReadOffset(beforeReadIndex + length);
        }
        return packet;
    }
}

export default {};
