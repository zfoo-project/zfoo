

class EmptyObject {

    

    static PROTOCOL_ID: number = 0;

    protocolId(): number {
        return EmptyObject.PROTOCOL_ID;
    }

    static write(buffer: any, packet: EmptyObject | null) {
        if (packet === null) {
            buffer.writeInt(0);
            return;
        }
        buffer.writeInt(-1);
    }

    static read(buffer: any): EmptyObject | null {
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
    }
}

export default EmptyObject;
