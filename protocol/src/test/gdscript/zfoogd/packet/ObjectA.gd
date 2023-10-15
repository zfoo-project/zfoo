const PROTOCOL_ID = 102
const PROTOCOL_CLASS_NAME = "ObjectA"
const ObjectB = preload("res://zfoogd/packet/ObjectB.gd")


var a: int
var m: Dictionary	# Map<number, string>
var objectB: ObjectB
var innerCompatibleValue: int

func _to_string() -> String:
	const jsonTemplate = "{a:{}, m:{}, objectB:{}, innerCompatibleValue:{}}"
	var params = [self.a, JSON.stringify(self.m), self.objectB, self.innerCompatibleValue]
	return jsonTemplate.format(params, "{}")

static func write(buffer, packet):
	if (packet == null):
		buffer.writeInt(0)
		return
	var beforeWriteIndex = buffer.getWriteOffset()
	buffer.writeInt(201)
	buffer.writeInt(packet.a)
	buffer.writeIntStringMap(packet.m)
	buffer.writePacket(packet.objectB, 103)
	buffer.writeInt(packet.innerCompatibleValue)
	buffer.adjustPadding(201, beforeWriteIndex)
	pass

static func read(buffer):
	var length = buffer.readInt()
	if (length == 0):
		return null
	var beforeReadIndex = buffer.getReadOffset()
	var packet = buffer.newInstance(PROTOCOL_ID)
	var result0 = buffer.readInt()
	packet.a = result0
	var map1 = buffer.readIntStringMap()
	packet.m = map1
	var result2 = buffer.readPacket(103)
	packet.objectB = result2
	if buffer.compatibleRead(beforeReadIndex, length):
		var result3 = buffer.readInt()
		packet.innerCompatibleValue = result3;
	if (length > 0):
		buffer.setReadOffset(beforeReadIndex + length)
	return packet
