func (protocol ${protocol_name}) ProtocolId() int16 {
	return ${protocol_id}
}

func (protocol ${protocol_name}) write(buffer *ByteBuffer, packet any) {
	if packet == nil {
	    buffer.WriteInt(0)
		return
	}
	var message = packet.(*${protocol_name})
	${protocol_write_serialization}
}

func (protocol ${protocol_name}) read(buffer *ByteBuffer) any {
	var packet = new(${protocol_name})
	var length = buffer.ReadInt()
	if length == 0 {
		return packet
	}
	var beforeReadIndex = buffer.GetReadOffset()
	${protocol_read_deserialization}
	if length > 0 {
        buffer.SetReadOffset(beforeReadIndex + length)
    }
	return packet
}
