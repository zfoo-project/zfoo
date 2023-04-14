{}
{}
class {} {

    {}

    static PROTOCOL_ID: number = {};

    protocolId(): number {
        return {}.PROTOCOL_ID;
    }

    static write(buffer: any, packet: {} | null) {
        if (buffer.writePacketFlag(packet) || packet == null) {
            return;
        }
        {}
    }

    static read(buffer: any): {} | null {
        if (!buffer.readBoolean()) {
            return null;
        }
        const packet = new {}();
        {}
        return packet;
    }
}

export default {};
