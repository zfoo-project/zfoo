${protocol_imports}

static var protocols: Dictionary = {}
static var protocolClassMap: Dictionary = {}

static func initProtocol():
	${protocol_manager_registrations}
	pass

static func getProtocol(protocolId: int):
	return protocols[protocolId]

static func getProtocolClass(protocolId: int):
	return protocolClassMap[protocolId]

static func newInstance(protocolId: int):
	var protocol = protocolClassMap[protocolId]
	return protocol.new()

static func write(buffer, packet):
	var protocolId: int = packet.protocolId()
	buffer.writeShort(protocolId)
	var protocol = protocols[protocolId]
	protocol.write(buffer, packet)

static func read(buffer):
	var protocolId = buffer.readShort()
	var protocol = protocols[protocolId]
	var packet = protocol.read(buffer)
	return packet
