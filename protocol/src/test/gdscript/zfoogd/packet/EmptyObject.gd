class_name EmptyObject
const PROTOCOL_ID: int = 0

static func write(buffer: ByteBuffer, packet: EmptyObject):
	if (packet == null):
		buffer.writeInt(0)
		return
	buffer.writeInt(-1)
	pass

static func read(buffer: ByteBuffer) -> EmptyObject:
	var length = buffer.readInt()
	if (length == 0):
		return null
	var beforeReadIndex = buffer.getReadOffset()
	var packet: EmptyObject = EmptyObject.new()
	
	if (length > 0):
		buffer.setReadOffset(beforeReadIndex + length)
	return packet