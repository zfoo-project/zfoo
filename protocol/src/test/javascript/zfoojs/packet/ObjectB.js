
const ObjectB = function() {
    this.flag = false; // boolean
    this.innerCompatibleValue = 0; // number
};

ObjectB.PROTOCOL_ID = 103;

ObjectB.prototype.protocolId = function() {
    return ObjectB.PROTOCOL_ID;
};

ObjectB.write = function(buffer, packet) {
    if (packet === null) {
        buffer.writeInt(0);
        return;
    }
    const beforeWriteIndex = buffer.getWriteOffset();
    buffer.writeInt(4);
    buffer.writeBoolean(packet.flag);
    buffer.writeInt(packet.innerCompatibleValue);
    buffer.adjustPadding(4, beforeWriteIndex);
};

ObjectB.read = function(buffer) {
    const length = buffer.readInt();
    if (length === 0) {
        return null;
    }
    const beforeReadIndex = buffer.getReadOffset();
    const packet = new ObjectB();
    const result0 = buffer.readBoolean(); 
    packet.flag = result0;
    if (buffer.compatibleRead(beforeReadIndex, length)) {
        const result1 = buffer.readInt();
        packet.innerCompatibleValue = result1;
    }
    if (length > 0) {
        buffer.setReadOffset(beforeReadIndex + length);
    }
    return packet;
};

export default ObjectB;