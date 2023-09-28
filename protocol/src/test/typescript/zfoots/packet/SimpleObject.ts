

class SimpleObject {

    c: number = 0;
    g: boolean = false;

    static PROTOCOL_ID: number = 104;

    protocolId(): number {
        return SimpleObject.PROTOCOL_ID;
    }

    static write(buffer: any, packet: SimpleObject | null) {
        if (packet === null) {
            buffer.writeInt(0);
            return;
        }
        buffer.writeInt(-1);
        buffer.writeInt(packet.c);
        buffer.writeBoolean(packet.g);
    }

    static read(buffer: any): SimpleObject | null {
        const length = buffer.readInt();
        if (length === 0) {
            return null;
        }
        const beforeReadIndex = buffer.getReadOffset();
        const packet = new SimpleObject();
        const result0 = buffer.readInt();
        packet.c = result0;
        const result1 = buffer.readBoolean(); 
        packet.g = result1;
        if (length > 0) {
            buffer.setReadOffset(beforeReadIndex + length);
        }
        return packet;
    }
}

export default SimpleObject;
