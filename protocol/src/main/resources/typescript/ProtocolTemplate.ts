{}
{}
class {} {

    {}

    protocolId(): number {
        return {};
    }

    static write(buffer: any, packet: {} | null) {
        if (buffer.writePacketFlag(packet)) {
            return;
        }
        if (packet === null) {
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
