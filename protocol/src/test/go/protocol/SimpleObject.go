package protocol

type SimpleObject struct {
	c int
	g bool
}

func (protocol SimpleObject) protocolId() int16 {
	return 104
}

func (protocol SimpleObject) write(buffer *ByteBuffer, packet any) {
	if buffer.WritePacketFlag(packet) {
		return
	}
	var message = packet.(*SimpleObject)
	buffer.WriteInt(message.c)
	buffer.WriteBool(message.g)
}

func (protocol SimpleObject) read(buffer *ByteBuffer) any {
	var packet = new(SimpleObject)
	if !buffer.ReadBool() {
		return packet
	}
	var result0 = buffer.ReadInt()
	packet.c = result0
	var result1 = buffer.ReadBool()
	packet.g = result1
	return packet
}
