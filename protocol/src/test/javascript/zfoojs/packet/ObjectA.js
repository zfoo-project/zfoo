// @author godotg
// @version 3.0
const ObjectA = function(a, m, objectB) {
    this.a = a; // int
    this.m = m; // java.util.Map<java.lang.Integer, java.lang.String>
    this.objectB = objectB; // com.zfoo.protocol.packet.ObjectB
};

ObjectA.prototype.protocolId = function() {
    return 102;
};

ObjectA.write = function(buffer, packet) {
    if (buffer.writePacketFlag(packet)) {
        return;
    }
    buffer.writeInt(packet.a);
    buffer.writeIntStringMap(packet.m);
    buffer.writePacket(packet.objectB, 103);
};

ObjectA.read = function(buffer) {
    if (!buffer.readBoolean()) {
        return null;
    }
    const packet = new ObjectA();
    const result0 = buffer.readInt();
    packet.a = result0;
    const map1 = buffer.readIntStringMap();
    packet.m = map1;
    const result2 = buffer.readPacket(103);
    packet.objectB = result2;
    return packet;
};

export default ObjectA;
