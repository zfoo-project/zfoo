// @author jaysunxiao
// @version 3.0
const SimpleObject = function(c, g) {
    this.c = c; // int
    this.g = g; // boolean
};

SimpleObject.prototype.protocolId = function() {
    return 104;
};

SimpleObject.write = function(byteBuffer, packet) {
    if (byteBuffer.writePacketFlag(packet)) {
        return;
    }
    byteBuffer.writeInt(packet.c);
    byteBuffer.writeBoolean(packet.g);
};

SimpleObject.read = function(byteBuffer) {
    if (!byteBuffer.readBoolean()) {
        return null;
    }
    const packet = new SimpleObject();
    const result0 = byteBuffer.readInt();
    packet.c = result0;
    const result1 = byteBuffer.readBoolean(); 
    packet.g = result1;
    return packet;
};

export default SimpleObject;
