static var protocols: Dictionary[int, Object] = {
	0 : EmptyObject,
	1 : VeryBigObject,
	100 : ComplexObject,
	101 : NormalObject,
	102 : ObjectA,
	103 : ObjectB,
	104 : SimpleObject
}

static func write(buffer: ByteBuffer, packet: Object) -> void:
	var protocolId: int = packet.protocolId()
	buffer.writeShort(protocolId)
	packet.write(buffer, packet)
	pass

static func read(buffer: ByteBuffer) -> Object:
	var protocolId = buffer.readShort()
	var protocol = protocols[protocolId]
	var packet = protocol.read(buffer)
	return packet