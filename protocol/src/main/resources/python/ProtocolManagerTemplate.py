${protocol_imports}

protocols = {}
protocolIdMap = {}

${protocol_manager_registrations}

def getProtocol(protocolId):
	return protocols[protocolId]

def write(buffer, packet):
	protocolId = protocolIdMap[type(packet)]
	buffer.writeShort(protocolId)
	protocol = protocols[protocolId]
	protocol.write(buffer, packet)

def read(buffer):
	protocolId = buffer.readShort()
	protocol = protocols[protocolId]
	packet = protocol.read(buffer)
	return packet
