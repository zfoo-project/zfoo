

class ObjectB {

    flag: boolean = false;

    static PROTOCOL_ID: number = 103;

    protocolId(): number {
        return ObjectB.PROTOCOL_ID;
    }

    static write(buffer: any, packet: ObjectB | null) {
        if (packet === null) {
            buffer.writeInt(0);
            return;
        }
        buffer.writeInt(-1);
        buffer.writeBoolean(packet.flag);
    }

    static read(buffer: any): ObjectB | null {
        const length = buffer.readInt();
        if (length === 0) {
            return null;
        }
        const beforeReadIndex = buffer.getReadOffset();
        const packet = new ObjectB();
        const result0 = buffer.readBoolean(); 
        packet.flag = result0;
        if (length > 0) {
            buffer.setReadOffset(beforeReadIndex + length);
        }
        return packet;
    }
}

export default ObjectB;
