package protocol
{}
type {} struct {
	{}
}

func (protocol {}) ProtocolId() int16 {
	return {}
}

func (protocol {}) write(buffer *ByteBuffer, packet any) {
	if packet == nil {
	    buffer.WriteInt(0)
		return
	}
	buffer.WriteInt(-1)
}

func (protocol {}) read(buffer *ByteBuffer) any {
	var packet = new({})
	var length = buffer.ReadInt()
	if length == 0 {
		return packet
	}
	var beforeReadIndex = buffer.ReadOffset()
	if length > 0 {
        buffer.SetReadOffset(beforeReadIndex + length)
    }
	return packet
}
