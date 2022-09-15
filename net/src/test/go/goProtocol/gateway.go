package protocol

type GatewayToProviderRequest struct {
	message string
}

func (protocol GatewayToProviderRequest) protocolId() int16 {
	return 5000
}

func (protocol GatewayToProviderRequest) write(buffer *ByteBuffer, packet any) {
	if buffer.WritePacketFlag(packet) {
		return
	}
	var message = packet.(*GatewayToProviderRequest)
	buffer.WriteString(message.message)
}

func (protocol GatewayToProviderRequest) read(buffer *ByteBuffer) any {
	var packet = new(GatewayToProviderRequest)
	if !buffer.ReadBool() {
		return packet
	}
	var result0 = buffer.ReadString()
    packet.message = result0
	return packet
}


type GatewayToProviderResponse struct {
	message string
}

func (protocol GatewayToProviderResponse) protocolId() int16 {
	return 5001
}

func (protocol GatewayToProviderResponse) write(buffer *ByteBuffer, packet any) {
	if buffer.WritePacketFlag(packet) {
		return
	}
	var message = packet.(*GatewayToProviderResponse)
	buffer.WriteString(message.message)
}

func (protocol GatewayToProviderResponse) read(buffer *ByteBuffer) any {
	var packet = new(GatewayToProviderResponse)
	if !buffer.ReadBool() {
		return packet
	}
	var result0 = buffer.ReadString()
    packet.message = result0
	return packet
}
