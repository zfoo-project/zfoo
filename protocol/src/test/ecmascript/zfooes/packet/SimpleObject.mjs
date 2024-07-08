
class SimpleObject {
    c = 0; // number
    g = false; // boolean
}

export class SimpleObjectRegistration {
    protocolId() {
        return 104;
    }

    write(buffer, packet) {
        if (packet === null) {
            buffer.writeInt(0);
            return;
        }
        buffer.writeInt(-1);
        buffer.writeInt(packet.c);
        buffer.writeBoolean(packet.g);
    }

    read(buffer) {
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