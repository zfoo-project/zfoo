static var protocols: Dictionary[int, Object] = {
${protocol_manager_registrations}
}

static func write(buffer: ByteBuffer, packet: Object) -> void:
	var protocolId: int = packet.PROTOCOL_ID
	buffer.writeShort(protocolId)
	packet.write(buffer, packet)
	pass

static func read(buffer: ByteBuffer) -> Object:
	var protocolId = buffer.readShort()
	var protocol = protocols[protocolId]
	var packet = protocol.read(buffer)
	return packet
