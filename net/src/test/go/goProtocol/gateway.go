package protocol

type GatewayToProviderRequest struct {
	Message string
}

func (protocol GatewayToProviderRequest) ProtocolId() int16 {
	return 5000
}

func (protocol GatewayToProviderRequest) write(buffer *ByteBuffer, packet any) {
	if buffer.WritePacketFlag(packet) {
		return
	}
	var message = packet.(*GatewayToProviderRequest)
	buffer.WriteString(message.Message)
}

func (protocol GatewayToProviderRequest) read(buffer *ByteBuffer) any {
	var packet = new(GatewayToProviderRequest)
	if !buffer.ReadBool() {
		return packet
	}
	var result0 = buffer.ReadString()
    packet.Message = result0
	return packet
}


type GatewayToProviderResponse struct {
	Message string
}

func (protocol GatewayToProviderResponse) ProtocolId() int16 {
	return 5001
}

func (protocol GatewayToProviderResponse) write(buffer *ByteBuffer, packet any) {
	if buffer.WritePacketFlag(packet) {
		return
	}
	var message = packet.(*GatewayToProviderResponse)
	buffer.WriteString(message.Message)
}

func (protocol GatewayToProviderResponse) read(buffer *ByteBuffer) any {
	var packet = new(GatewayToProviderResponse)
	if !buffer.ReadBool() {
		return packet
	}
	var result0 = buffer.ReadString()
    packet.Message = result0
	return packet
}
