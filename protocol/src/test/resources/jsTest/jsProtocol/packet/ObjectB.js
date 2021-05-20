// @author jaysunxiao
// @version 1.0
// @since 2017 10.12 15:39
const ObjectB = function (flag) {
    this.flag = flag; // boolean
};

ObjectB.prototype.protocolId = function () {
    return 1117;
};

ObjectB.writeObject = function (byteBuffer, packet) {
    if (packet === null) {
        byteBuffer.writeBoolean(false);
        return;
    }
    byteBuffer.writeBoolean(true);
    byteBuffer.writeBoolean(packet.flag);
};

ObjectB.readObject = function (byteBuffer) {
    if (!byteBuffer.readBoolean()) {
        return null;
    }
    const packet = new ObjectB();
    const result0 = byteBuffer.readBoolean();
    packet.flag = result0;
    return packet;
};

export default ObjectB;
