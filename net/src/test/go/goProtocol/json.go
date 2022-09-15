package protocol

type JsonHelloRequest struct {
	Message string
}

func (protocol JsonHelloRequest) ProtocolId() int16 {
	return 1600
}

func (protocol JsonHelloRequest) write(buffer *ByteBuffer, packet any) {
	if buffer.WritePacketFlag(packet) {
		return
	}
	var message = packet.(*JsonHelloRequest)
	buffer.WriteString(message.Message)
}

func (protocol JsonHelloRequest) read(buffer *ByteBuffer) any {
	var packet = new(JsonHelloRequest)
	if !buffer.ReadBool() {
		return packet
	}
	var result0 = buffer.ReadString()
    packet.Message = result0
	return packet
}


type JsonHelloResponse struct {
	Message string
}

func (protocol JsonHelloResponse) ProtocolId() int16 {
	return 1601
}

func (protocol JsonHelloResponse) write(buffer *ByteBuffer, packet any) {
	if buffer.WritePacketFlag(packet) {
		return
	}
	var message = packet.(*JsonHelloResponse)
	buffer.WriteString(message.Message)
}

func (protocol JsonHelloResponse) read(buffer *ByteBuffer) any {
	var packet = new(JsonHelloResponse)
	if !buffer.ReadBool() {
		return packet
	}
	var result0 = buffer.ReadString()
    packet.Message = result0
	return packet
}
