
class ObjectB {
    flag = false; // boolean

    static PROTOCOL_ID = 103;

    protocolId() {
        return ObjectB.PROTOCOL_ID;
    }

    static write(buffer, packet) {
        if (packet === null) {
            buffer.writeInt(0);
            return;
        }
        buffer.writeInt(-1);
        buffer.writeBoolean(packet.flag);
    }

    static read(buffer) {
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