const PROTOCOL_ID = 103
const PROTOCOL_CLASS_NAME = "ObjectB"


var flag: bool

func _to_string() -> String:
	const jsonTemplate = "{flag:{}}"
	var params = [self.flag]
	return jsonTemplate.format(params, "{}")

static func write(buffer, packet):
	if (packet == null):
		buffer.writeInt(0)
		return
	buffer.writeInt(-1)
	buffer.writeBool(packet.flag)
	pass

static func read(buffer):
	var length = buffer.readInt()
	if (length == 0):
		return null
	var beforeReadIndex = buffer.getReadOffset()
	var packet = buffer.newInstance(PROTOCOL_ID)
	var result0 = buffer.readBool() 
	packet.flag = result0
	if (length > 0):
		buffer.setReadOffset(beforeReadIndex + length)
	return packet
