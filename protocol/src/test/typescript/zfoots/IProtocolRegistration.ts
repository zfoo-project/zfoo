import IByteBuffer from "./IByteBuffer";

interface IProtocolRegistration<T> {
    protocolId(): number;

    write(buffer: IByteBuffer, packet: T | null): void;

    read(buffer: IByteBuffer): T | null;
}

export default IProtocolRegistration;