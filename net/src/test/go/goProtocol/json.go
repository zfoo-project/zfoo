package protocol

type JsonHelloRequest struct {
	message string
}

func (protocol JsonHelloRequest) protocolId() int16 {
	return 1600
}

func (protocol JsonHelloRequest) write(buffer *ByteBuffer, packet any) {
	if buffer.WritePacketFlag(packet) {
		return
	}
	var message = packet.(*JsonHelloRequest)
	buffer.WriteString(message.message)
}

func (protocol JsonHelloRequest) read(buffer *ByteBuffer) any {
	var packet = new(JsonHelloRequest)
	if !buffer.ReadBool() {
		return packet
	}
	var result0 = buffer.ReadString()
    packet.message = result0
	return packet
}


type JsonHelloResponse struct {
	message string
}

func (protocol JsonHelloResponse) protocolId() int16 {
	return 1601
}

func (protocol JsonHelloResponse) write(buffer *ByteBuffer, packet any) {
	if buffer.WritePacketFlag(packet) {
		return
	}
	var message = packet.(*JsonHelloResponse)
	buffer.WriteString(message.message)
}

func (protocol JsonHelloResponse) read(buffer *ByteBuffer) any {
	var packet = new(JsonHelloResponse)
	if !buffer.ReadBool() {
		return packet
	}
	var result0 = buffer.ReadString()
    packet.message = result0
	return packet
}
