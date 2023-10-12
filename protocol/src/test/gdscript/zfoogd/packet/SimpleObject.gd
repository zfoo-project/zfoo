const PROTOCOL_ID = 104
const PROTOCOL_CLASS_NAME = "SimpleObject"


var c: int
var g: bool

func _to_string() -> String:
	const jsonTemplate = "{c:{}, g:{}}"
	var params = [self.c, self.g]
	return jsonTemplate.format(params, "{}")

static func write(buffer, packet):
	if (packet == null):
		buffer.writeInt(0)
		return
	buffer.writeInt(-1)
	buffer.writeInt(packet.c)
	buffer.writeBool(packet.g)
	pass

static func read(buffer):
	var length = buffer.readInt()
	if (length == 0):
		return null
	var beforeReadIndex = buffer.getReadOffset()
	var packet = buffer.newInstance(PROTOCOL_ID)
	var result0 = buffer.readInt()
	packet.c = result0
	var result1 = buffer.readBool() 
	packet.g = result1
	if (length > 0):
		buffer.setReadOffset(beforeReadIndex + length)
	return packet
