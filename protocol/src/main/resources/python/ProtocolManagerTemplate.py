{}

protocols = {}

{}

def getProtocol(protocolId):
	return protocols[protocolId]

def write(buffer, packet):
	protocolId = packet.protocolId()
	buffer.writeShort(protocolId)
	protocol = protocols[protocolId]
	protocol.write(buffer, packet)

def read(buffer):
	protocolId = buffer.readShort()
	protocol = protocols[protocolId]
	packet = protocol.read(buffer)
	return packet
