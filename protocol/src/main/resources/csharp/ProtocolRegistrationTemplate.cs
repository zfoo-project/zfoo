public class ${protocol_name}Registration : IProtocolRegistration
{
    public short ProtocolId()
    {
        return ${protocol_id};
    }

    public void Write(ByteBuffer buffer, object packet)
    {
        if (packet == null)
        {
            buffer.WriteInt(0);
            return;
        }
        ${protocol_name} message = (${protocol_name}) packet;
        ${protocol_write_serialization}
    }

    public object Read(ByteBuffer buffer)
    {
        int length = buffer.ReadInt();
        if (length == 0)
        {
            return null;
        }
        int beforeReadIndex = buffer.ReadOffset();
        ${protocol_name} packet = new ${protocol_name}();
        ${protocol_read_deserialization}
        if (length > 0)
        {
            buffer.SetReadOffset(beforeReadIndex + length);
        }
        return packet;
    }
}