

class ObjectB {

    flag: boolean = false;
    innerCompatibleValue: number = 0;

    static PROTOCOL_ID: number = 103;

    protocolId(): number {
        return ObjectB.PROTOCOL_ID;
    }

    static write(buffer: any, packet: ObjectB | null) {
        if (packet === null) {
            buffer.writeInt(0);
            return;
        }
        const beforeWriteIndex = buffer.getWriteOffset();
        buffer.writeInt(1);
        buffer.writeBoolean(packet.flag);
        buffer.writeInt(packet.innerCompatibleValue);
        buffer.adjustPadding(1, beforeWriteIndex);
    }

    static read(buffer: any): ObjectB | null {
        const length = buffer.readInt();
        if (length === 0) {
            return null;
        }
        const readIndex = buffer.getReadOffset();
        const packet = new ObjectB();
        const result0 = buffer.readBoolean(); 
        packet.flag = result0;
        if (length !== -1 && buffer.getReadOffset() - readIndex < length) {
            const result1 = buffer.readInt();
            packet.innerCompatibleValue = result1;
        }
        if (length > 0) {
            buffer.setReadOffset(readIndex + length);
        }
        return packet;
    }
}

export default ObjectB;
