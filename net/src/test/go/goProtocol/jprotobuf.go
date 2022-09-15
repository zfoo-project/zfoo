package protocol

type JProtobufHelloRequest struct {
	Message string
}

func (protocol JProtobufHelloRequest) ProtocolId() int16 {
	return 1500
}

func (protocol JProtobufHelloRequest) write(buffer *ByteBuffer, packet any) {
	if buffer.WritePacketFlag(packet) {
		return
	}
	var message = packet.(*JProtobufHelloRequest)
	buffer.WriteString(message.Message)
}

func (protocol JProtobufHelloRequest) read(buffer *ByteBuffer) any {
	var packet = new(JProtobufHelloRequest)
	if !buffer.ReadBool() {
		return packet
	}
	var result0 = buffer.ReadString()
    packet.Message = result0
	return packet
}


type JProtobufHelloResponse struct {
	Message string
}

func (protocol JProtobufHelloResponse) ProtocolId() int16 {
	return 1501
}

func (protocol JProtobufHelloResponse) write(buffer *ByteBuffer, packet any) {
	if buffer.WritePacketFlag(packet) {
		return
	}
	var message = packet.(*JProtobufHelloResponse)
	buffer.WriteString(message.Message)
}

func (protocol JProtobufHelloResponse) read(buffer *ByteBuffer) any {
	var packet = new(JProtobufHelloResponse)
	if !buffer.ReadBool() {
		return packet
	}
	var result0 = buffer.ReadString()
    packet.Message = result0
	return packet
}
