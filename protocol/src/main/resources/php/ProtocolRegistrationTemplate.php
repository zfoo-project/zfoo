class ${protocol_name}Registration implements IProtocolRegistration {
    public function protocolId(): int
    {
        return ${protocol_id};
    }

    public function write(ByteBuffer $buffer, mixed $packet): void
    {
        if ($packet == null)
        {
            $buffer->writeInt(0);
            return;
        }
        ${protocol_write_serialization}
    }

    public function read(ByteBuffer $buffer): mixed
    {
        $length = $buffer->readInt();
        $packet = new ${protocol_name}();
        if ($length == 0) {
            return $packet;
        }
        $beforeReadIndex = $buffer->readOffset();
        ${protocol_read_deserialization}
        if ($length > 0) {
            $buffer->setReadOffset($beforeReadIndex + $length);
        }
        return $packet;
    }
}