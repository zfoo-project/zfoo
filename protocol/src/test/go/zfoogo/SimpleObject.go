package protocol

type SimpleObject struct {
	C int
    G bool
}

func (protocol SimpleObject) ProtocolId() int16 {
	return 104
}

func (protocol SimpleObject) write(buffer *ByteBuffer, packet any) {
	if packet == nil {
	    buffer.WriteInt(0)
		return
	}
	var message = packet.(*SimpleObject)
	buffer.WriteInt(-1)
    buffer.WriteInt(message.C)
    buffer.WriteBool(message.G)
}

func (protocol SimpleObject) read(buffer *ByteBuffer) any {
	var packet = new(SimpleObject)
	var length = buffer.ReadInt()
	if length == 0 {
		return packet
	}
	var beforeReadIndex = buffer.ReadOffset()
	var result0 = buffer.ReadInt()
    packet.C = result0
    var result1 = buffer.ReadBool()
    packet.G = result1
	if length > 0 {
        buffer.SetReadOffset(beforeReadIndex + length)
    }
	return packet
}
