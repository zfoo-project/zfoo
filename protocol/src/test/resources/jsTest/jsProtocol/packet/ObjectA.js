// @author jaysunxiao
// @version 3.0
const ObjectA = function(a, m, objectB) {
    this.a = a; // int
    this.m = m; // java.util.Map<java.lang.Integer, java.lang.String>
    this.objectB = objectB; // com.zfoo.protocol.packet.ObjectB
};

ObjectA.prototype.protocolId = function() {
    return 102;
};

ObjectA.write = function(byteBuffer, packet) {
    if (byteBuffer.writePacketFlag(packet)) {
        return;
    }
    byteBuffer.writeInt(packet.a);
    byteBuffer.writeIntStringMap(packet.m);
    byteBuffer.writePacket(packet.objectB, 103);
};

ObjectA.read = function(byteBuffer) {
    if (!byteBuffer.readBoolean()) {
        return null;
    }
    const packet = new ObjectA();
    const result0 = byteBuffer.readInt();
    packet.a = result0;
    const map1 = byteBuffer.readIntStringMap();
    packet.m = map1;
    const result2 = byteBuffer.readPacket(103);
    packet.objectB = result2;
    return packet;
};

export default ObjectA;
