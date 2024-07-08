import IByteBuffer from '../IByteBuffer';
import IProtocolRegistration from '../IProtocolRegistration';


class EmptyObject {
    
}

export class EmptyObjectRegistration implements IProtocolRegistration<EmptyObject> {
    protocolId(): number {
        return 0;
    }

    write(buffer: IByteBuffer, packet: EmptyObject | null) {
        if (packet === null) {
            buffer.writeInt(0);
            return;
        }
        buffer.writeInt(-1);
    }

    read(buffer: IByteBuffer): EmptyObject | null {
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