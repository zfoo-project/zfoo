class_name ObjectB
const PROTOCOL_ID: int = 103

var flag: bool

static func write(buffer: ByteBuffer, packet: ObjectB):
	if (packet == null):
		buffer.writeInt(0)
		return
	buffer.writeInt(-1)
	buffer.writeBool(packet.flag)
	pass

static func read(buffer: ByteBuffer) -> ObjectB:
	var length = buffer.readInt()
	if (length == 0):
		return null
	var beforeReadIndex = buffer.getReadOffset()
	var packet: ObjectB = ObjectB.new()
	var result0 = buffer.readBool() 
	packet.flag = result0
	if (length > 0):
		buffer.setReadOffset(beforeReadIndex + length)
	return packet