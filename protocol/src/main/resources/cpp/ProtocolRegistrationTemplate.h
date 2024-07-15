class ${protocol_name}Registration : public IProtocolRegistration {
public:
    int16_t protocolId() override {
        return ${protocol_id};
    }

    void write(ByteBuffer &buffer, IProtocol *packet) override {
        if (packet == nullptr) {
            buffer.writeInt(0);
            return;
        }
        auto *message = (${protocol_name} *) packet;
        ${protocol_write_serialization}
    }

    IProtocol *read(ByteBuffer &buffer) override {
        auto *packet = new ${protocol_name}();
        auto length = buffer.readInt();
        if (length == 0) {
            return packet;
        }
        auto beforeReadIndex = buffer.getReadOffset();
        ${protocol_read_deserialization}
        if (length > 0) {
            buffer.setReadOffset(beforeReadIndex + length);
        }
        return packet;
    }
};