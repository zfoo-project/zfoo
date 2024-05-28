
const EmptyObject = function() {
    
};

EmptyObject.PROTOCOL_ID = 0;

EmptyObject.prototype.protocolId = function() {
    return EmptyObject.PROTOCOL_ID;
};

EmptyObject.write = function(buffer, packet) {
    if (packet === null) {
        buffer.writeInt(0);
        return;
    }
    buffer.writeInt(-1);
};

EmptyObject.read = function(buffer) {
    const length = buffer.readInt();
    if (length === 0) {
        return null;
    }
    const beforeReadIndex = buffer.getReadOffset();
    const packet = new EmptyObject();
    
    if (length > 0) {
        buffer.setReadOffset(beforeReadIndex + length);
    }
    return packet;
};

export default EmptyObject;