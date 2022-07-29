// @author godotg
// @version 3.0
const ObjectB = function(flag) {
    this.flag = flag; // boolean
};

ObjectB.prototype.protocolId = function() {
    return 103;
};

ObjectB.write = function(buffer, packet) {
    if (buffer.writePacketFlag(packet)) {
        return;
    }
    buffer.writeBoolean(packet.flag);
};

ObjectB.read = function(buffer) {
    if (!buffer.readBoolean()) {
        return null;
    }
    const packet = new ObjectB();
    const result0 = buffer.readBoolean(); 
    packet.flag = result0;
    return packet;
};

export default ObjectB;
