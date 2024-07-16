const EmptyObject = preload("res://zfoogd/packet/EmptyObject.gd")
const VeryBigObject = preload("res://zfoogd/packet/VeryBigObject.gd")
const ComplexObject = preload("res://zfoogd/packet/ComplexObject.gd")
const NormalObject = preload("res://zfoogd/packet/NormalObject.gd")
const ObjectA = preload("res://zfoogd/packet/ObjectA.gd")
const ObjectB = preload("res://zfoogd/packet/ObjectB.gd")
const SimpleObject = preload("res://zfoogd/packet/SimpleObject.gd")

static var protocols: Dictionary = {}
static var protocolClassMap: Dictionary = {}
static var protocolIdMap: Dictionary = {}

static func initProtocol():
	protocols[0] = EmptyObject.EmptyObjectRegistration.new()
	protocolClassMap[0] = EmptyObject.EmptyObject
	protocolIdMap[EmptyObject.EmptyObject] = 0
	protocols[1] = VeryBigObject.VeryBigObjectRegistration.new()
	protocolClassMap[1] = VeryBigObject.VeryBigObject
	protocolIdMap[VeryBigObject.VeryBigObject] = 1
	protocols[100] = ComplexObject.ComplexObjectRegistration.new()
	protocolClassMap[100] = ComplexObject.ComplexObject
	protocolIdMap[ComplexObject.ComplexObject] = 100
	protocols[101] = NormalObject.NormalObjectRegistration.new()
	protocolClassMap[101] = NormalObject.NormalObject
	protocolIdMap[NormalObject.NormalObject] = 101
	protocols[102] = ObjectA.ObjectARegistration.new()
	protocolClassMap[102] = ObjectA.ObjectA
	protocolIdMap[ObjectA.ObjectA] = 102
	protocols[103] = ObjectB.ObjectBRegistration.new()
	protocolClassMap[103] = ObjectB.ObjectB
	protocolIdMap[ObjectB.ObjectB] = 103
	protocols[104] = SimpleObject.SimpleObjectRegistration.new()
	protocolClassMap[104] = SimpleObject.SimpleObject
	protocolIdMap[SimpleObject.SimpleObject] = 104
	pass

static func getProtocol(protocolId: int):
	return protocols[protocolId]

static func getProtocolClass(protocolId: int):
	return protocolClassMap[protocolId]

static func write(buffer, packet):
	var protocolId: int = protocolIdMap[packet.get_script()]
	buffer.writeShort(protocolId)
	var protocol = protocols[protocolId]
	protocol.write(buffer, packet)

static func read(buffer):
	var protocolId = buffer.readShort()
	var protocol = protocols[protocolId]
	var packet = protocol.read(buffer)
	return packet