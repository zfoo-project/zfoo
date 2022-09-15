package protocol

type JProtobufHelloRequest struct {
	message string
}

func (protocol JProtobufHelloRequest) protocolId() int16 {
	return 1500
}

func (protocol JProtobufHelloRequest) write(buffer *ByteBuffer, packet any) {
	if buffer.WritePacketFlag(packet) {
		return
	}
	var message = packet.(*JProtobufHelloRequest)
	buffer.WriteString(message.message)
}

func (protocol JProtobufHelloRequest) read(buffer *ByteBuffer) any {
	var packet = new(JProtobufHelloRequest)
	if !buffer.ReadBool() {
		return packet
	}
	var result0 = buffer.ReadString()
    packet.message = result0
	return packet
}


type JProtobufHelloResponse struct {
	message string
}

func (protocol JProtobufHelloResponse) protocolId() int16 {
	return 1501
}

func (protocol JProtobufHelloResponse) write(buffer *ByteBuffer, packet any) {
	if buffer.WritePacketFlag(packet) {
		return
	}
	var message = packet.(*JProtobufHelloResponse)
	buffer.WriteString(message.message)
}

func (protocol JProtobufHelloResponse) read(buffer *ByteBuffer) any {
	var packet = new(JProtobufHelloResponse)
	if !buffer.ReadBool() {
		return packet
	}
	var result0 = buffer.ReadString()
    packet.message = result0
	return packet
}
