// @author jaysunxiao
// @version 3.0
const ObjectB = function(flag) {
    this.flag = flag; // boolean
};

ObjectB.prototype.protocolId = function() {
    return 103;
};

ObjectB.write = function(byteBuffer, packet) {
    if (byteBuffer.writePacketFlag(packet)) {
        return;
    }
    byteBuffer.writeBoolean(packet.flag);
};

ObjectB.read = function(byteBuffer) {
    if (!byteBuffer.readBoolean()) {
        return null;
    }
    const packet = new ObjectB();
    const result0 = byteBuffer.readBoolean(); 
    packet.flag = result0;
    return packet;
};

export default ObjectB;
