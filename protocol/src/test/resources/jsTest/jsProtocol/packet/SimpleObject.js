// @author jaysunxiao
// @version 1.0
// @since 2021-03-27 15:18
const SimpleObject = function (c, g) {
    this.c = c; // int
    this.g = g; // boolean
};

SimpleObject.prototype.protocolId = function () {
    return 1163;
};

SimpleObject.writeObject = function (byteBuffer, packet) {
    if (packet === null) {
        byteBuffer.writeBoolean(false);
        return;
    }
    byteBuffer.writeBoolean(true);
    byteBuffer.writeInt(packet.c);
    byteBuffer.writeBoolean(packet.g);
};

SimpleObject.readObject = function (byteBuffer) {
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
