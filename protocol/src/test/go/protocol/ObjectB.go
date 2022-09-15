package protocol

type ObjectB struct {
	flag bool
}

func (protocol ObjectB) protocolId() int16 {
	return 103
}

func (protocol ObjectB) write(buffer *ByteBuffer, packet any) {
	if buffer.WritePacketFlag(packet) {
		return
	}
	var message = packet.(*ObjectB)
	buffer.WriteBool(message.flag)
}

func (protocol ObjectB) read(buffer *ByteBuffer) any {
	var packet = new(ObjectB)
	if !buffer.ReadBool() {
		return packet
	}
	var result0 = buffer.ReadBool()
	packet.flag = result0
	return packet
}
