class_name SimpleObject
const PROTOCOL_ID: int = 104

var c: int
var g: bool

static func write(buffer: ByteBuffer, packet: SimpleObject):
	if (packet == null):
		buffer.writeInt(0)
		return
	buffer.writeInt(-1)
	buffer.writeInt(packet.c)
	buffer.writeBool(packet.g)
	pass

static func read(buffer: ByteBuffer) -> SimpleObject:
	var length = buffer.readInt()
	if (length == 0):
		return null
	var beforeReadIndex = buffer.getReadOffset()
	var packet: SimpleObject = SimpleObject.new()
	var result0 = buffer.readInt()
	packet.c = result0
	var result1 = buffer.readBool() 
	packet.g = result1
	if (length > 0):
		buffer.setReadOffset(beforeReadIndex + length)
	return packet