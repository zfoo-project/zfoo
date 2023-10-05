const PROTOCOL_ID = {}
const PROTOCOL_CLASS_NAME = "{}"
{}
{}
{}

func _to_string() -> String:
	const jsonTemplate = "{}"
	var params = {}
	return jsonTemplate.format(params, "{}")

static func write(buffer, packet):
	if (packet == null):
		buffer.writeInt(0)
		return
	{}
	pass

static func read(buffer):
	var length = buffer.readInt()
	if (length == 0):
		return null
	var beforeReadIndex = buffer.getReadOffset()
	var packet = buffer.newInstance(PROTOCOL_ID)
	{}
	if (length > 0):
		buffer.setReadOffset(beforeReadIndex + length)
	return packet
