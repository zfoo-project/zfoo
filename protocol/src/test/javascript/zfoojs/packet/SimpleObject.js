// @author godotg
// @version 3.0
const SimpleObject = function(c, g) {
    this.c = c; // int
    this.g = g; // boolean
};

SimpleObject.prototype.protocolId = function() {
    return 104;
};

SimpleObject.write = function(buffer, packet) {
    if (buffer.writePacketFlag(packet)) {
        return;
    }
    buffer.writeInt(packet.c);
    buffer.writeBoolean(packet.g);
};

SimpleObject.read = function(buffer) {
    if (!buffer.readBoolean()) {
        return null;
    }
    const packet = new SimpleObject();
    const result0 = buffer.readInt();
    packet.c = result0;
    const result1 = buffer.readBoolean(); 
    packet.g = result1;
    return packet;
};

export default SimpleObject;
