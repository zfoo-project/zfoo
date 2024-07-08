from .packet import EmptyObject
from .packet import VeryBigObject
from .packet import ComplexObject
from .packet import NormalObject
from .packet import ObjectA
from .packet import ObjectB
from .packet import SimpleObject

protocols = {}
protocolIdMap = {}

protocols[0] = EmptyObject.EmptyObjectRegistration
protocolIdMap[EmptyObject.EmptyObject] = 0
protocols[1] = VeryBigObject.VeryBigObjectRegistration
protocolIdMap[VeryBigObject.VeryBigObject] = 1
protocols[100] = ComplexObject.ComplexObjectRegistration
protocolIdMap[ComplexObject.ComplexObject] = 100
protocols[101] = NormalObject.NormalObjectRegistration
protocolIdMap[NormalObject.NormalObject] = 101
protocols[102] = ObjectA.ObjectARegistration
protocolIdMap[ObjectA.ObjectA] = 102
protocols[103] = ObjectB.ObjectBRegistration
protocolIdMap[ObjectB.ObjectB] = 103
protocols[104] = SimpleObject.SimpleObjectRegistration
protocolIdMap[SimpleObject.SimpleObject] = 104

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