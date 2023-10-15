package protocol

type EmptyObject struct {
	
}

func (protocol EmptyObject) ProtocolId() int16 {
	return 0
}

func (protocol EmptyObject) write(buffer *ByteBuffer, packet any) {
	if packet == nil {
	    buffer.WriteInt(0)
		return
	}
	buffer.WriteInt(-1)
}

func (protocol EmptyObject) read(buffer *ByteBuffer) any {
	var packet = new(EmptyObject)
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
