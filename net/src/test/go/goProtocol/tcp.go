package protocol

type TcpHelloRequest struct {
	Message string
}

func (protocol TcpHelloRequest) ProtocolId() int16 {
	return 1300
}

func (protocol TcpHelloRequest) write(buffer *ByteBuffer, packet any) {
	if buffer.WritePacketFlag(packet) {
		return
	}
	var message = packet.(*TcpHelloRequest)
	buffer.WriteString(message.Message)
}

func (protocol TcpHelloRequest) read(buffer *ByteBuffer) any {
	var packet = new(TcpHelloRequest)
	if !buffer.ReadBool() {
		return packet
	}
	var result0 = buffer.ReadString()
    packet.Message = result0
	return packet
}


type TcpHelloResponse struct {
	Message string
}

func (protocol TcpHelloResponse) ProtocolId() int16 {
	return 1301
}

func (protocol TcpHelloResponse) write(buffer *ByteBuffer, packet any) {
	if buffer.WritePacketFlag(packet) {
		return
	}
	var message = packet.(*TcpHelloResponse)
	buffer.WriteString(message.Message)
}

func (protocol TcpHelloResponse) read(buffer *ByteBuffer) any {
	var packet = new(TcpHelloResponse)
	if !buffer.ReadBool() {
		return packet
	}
	var result0 = buffer.ReadString()
    packet.Message = result0
	return packet
}
