package protocol

type TcpHelloRequest struct {
	message string
}

func (protocol TcpHelloRequest) protocolId() int16 {
	return 1300
}

func (protocol TcpHelloRequest) write(buffer *ByteBuffer, packet any) {
	if buffer.WritePacketFlag(packet) {
		return
	}
	var message = packet.(*TcpHelloRequest)
	buffer.WriteString(message.message)
}

func (protocol TcpHelloRequest) read(buffer *ByteBuffer) any {
	var packet = new(TcpHelloRequest)
	if !buffer.ReadBool() {
		return packet
	}
	var result0 = buffer.ReadString()
    packet.message = result0
	return packet
}


type TcpHelloResponse struct {
	message string
}

func (protocol TcpHelloResponse) protocolId() int16 {
	return 1301
}

func (protocol TcpHelloResponse) write(buffer *ByteBuffer, packet any) {
	if buffer.WritePacketFlag(packet) {
		return
	}
	var message = packet.(*TcpHelloResponse)
	buffer.WriteString(message.message)
}

func (protocol TcpHelloResponse) read(buffer *ByteBuffer) any {
	var packet = new(TcpHelloResponse)
	if !buffer.ReadBool() {
		return packet
	}
	var result0 = buffer.ReadString()
    packet.message = result0
	return packet
}
