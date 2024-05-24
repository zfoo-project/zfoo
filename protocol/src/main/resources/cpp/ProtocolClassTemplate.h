${protocol_note}
class ${protocol_name} : public IProtocol {
public:
    ${protocol_field_definition}

    ~${protocol_name}() override = default;

    int16_t protocolId() override {
        return ${protocol_id};
    }
};