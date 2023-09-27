
const SimpleObject = function() {
    this.c = 0; // number
    this.g = false; // boolean
};

SimpleObject.prototype.protocolId = function() {
    return 104;
};

SimpleObject.write = function(buffer, packet) {
    if (packet === null) {
        buffer.writeInt(0);
        return;
    }
    buffer.writeInt(-1);
    buffer.writeInt(packet.c);
    buffer.writeBoolean(packet.g);
};

SimpleObject.read = function(buffer) {
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
};

export default SimpleObject;
