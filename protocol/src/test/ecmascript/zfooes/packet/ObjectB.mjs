
class ObjectB {
    flag = false; // boolean
    innerCompatibleValue = 0; // number
}

export class ObjectBRegistration {
    protocolId() {
        return 103;
    }

    write(buffer, packet) {
        if (packet === null) {
            buffer.writeInt(0);
            return;
        }
        const beforeWriteIndex = buffer.getWriteOffset();
        buffer.writeInt(4);
        buffer.writeBool(packet.flag);
        buffer.writeInt(packet.innerCompatibleValue);
        buffer.adjustPadding(4, beforeWriteIndex);
    }

    read(buffer) {
        const length = buffer.readInt();
        if (length === 0) {
            return null;
        }
        const beforeReadIndex = buffer.getReadOffset();
        const packet = new ObjectB();
        const result0 = buffer.readBool(); 
        packet.flag = result0;
        if (buffer.compatibleRead(beforeReadIndex, length)) {
            const result1 = buffer.readInt();
            packet.innerCompatibleValue = result1;
        }
        if (length > 0) {
            buffer.setReadOffset(beforeReadIndex + length);
        }
        return packet;
    }
}

export default ObjectB;