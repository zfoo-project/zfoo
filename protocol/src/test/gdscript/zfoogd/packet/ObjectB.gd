const PROTOCOL_ID = 103
const PROTOCOL_CLASS_NAME = "ObjectB"


var flag: bool
var innerCompatibleValue: int

func _to_string() -> String:
	const jsonTemplate = "{flag:{}, innerCompatibleValue:{}}"
	var params = [self.flag, self.innerCompatibleValue]
	return jsonTemplate.format(params, "{}")

static func write(buffer, packet):
	if (packet == null):
		buffer.writeInt(0)
		return
	var beforeWriteIndex = buffer.getWriteOffset()
	buffer.writeInt(4)
	buffer.writeBool(packet.flag)
	buffer.writeInt(packet.innerCompatibleValue)
	buffer.adjustPadding(4, beforeWriteIndex)
	pass

static func read(buffer):
	var length = buffer.readInt()
	if (length == 0):
		return null
	var beforeReadIndex = buffer.getReadOffset()
	var packet = buffer.newInstance(PROTOCOL_ID)
	var result0 = buffer.readBool() 
	packet.flag = result0
	if buffer.compatibleRead(beforeReadIndex, length):
		var result1 = buffer.readInt()
		packet.innerCompatibleValue = result1;
	if (length > 0):
		buffer.setReadOffset(beforeReadIndex + length)
	return packet
