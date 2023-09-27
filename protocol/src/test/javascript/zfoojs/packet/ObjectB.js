
const ObjectB = function() {
    this.flag = false; // boolean
};

ObjectB.prototype.protocolId = function() {
    return 103;
};

ObjectB.write = function(buffer, packet) {
    if (packet === null) {
        buffer.writeInt(0);
        return;
    }
    buffer.writeInt(-1);
    buffer.writeBoolean(packet.flag);
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
    if (length > 0) {
        buffer.setReadOffset(beforeReadIndex + length);
    }
    return packet;
};

export default ObjectB;
