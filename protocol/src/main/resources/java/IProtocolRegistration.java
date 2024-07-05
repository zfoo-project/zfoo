${protocol_root_path}

public interface IProtocolRegistration {
    short protocolId();

    void write(ByteBuffer buffer, Object packet);

    Object read(ByteBuffer buffer);

}