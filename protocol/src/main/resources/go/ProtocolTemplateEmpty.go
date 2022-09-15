package protocol
{}
type {} struct {
	{}
}

func (protocol {}) ProtocolId() int16 {
	return {}
}

func (protocol {}) write(buffer *ByteBuffer, packet any) {
	if buffer.WritePacketFlag(packet) {
		return
	}
}

func (protocol {}) read(buffer *ByteBuffer) any {
	var packet = new({})
	if !buffer.ReadBool() {
		return packet
	}
	return packet
}
