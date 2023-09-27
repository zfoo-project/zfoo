
const ObjectA = function() {
    this.a = 0; // number
    this.m = new Map(); // Map<number, string>
    this.objectB = null; // ObjectB | null
};

ObjectA.prototype.protocolId = function() {
    return 102;
};

ObjectA.write = function(buffer, packet) {
    if (packet === null) {
        buffer.writeInt(0);
        return;
    }
    buffer.writeInt(-1);
    buffer.writeInt(packet.a);
    buffer.writeIntStringMap(packet.m);
    buffer.writePacket(packet.objectB, 103);
};

ObjectA.read = function(buffer) {
    const length = buffer.readInt();
    if (length === 0) {
        return null;
    }
    const beforeReadIndex = buffer.getReadOffset();
    const packet = new ObjectA();
    const result0 = buffer.readInt();
    packet.a = result0;
    const map1 = buffer.readIntStringMap();
    packet.m = map1;
    const result2 = buffer.readPacket(103);
    packet.objectB = result2;
    if (length > 0) {
        buffer.setReadOffset(beforeReadIndex + length);
    }
    return packet;
};

export default ObjectA;
