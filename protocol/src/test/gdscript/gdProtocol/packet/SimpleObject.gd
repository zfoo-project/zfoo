# @author jaysunxiao
# @version 3.0

var c # int
var g # boolean

const PROTOCOL_ID = 104

static func write(buffer, packet):
	if (buffer.writePacketFlag(packet)):
		return
	buffer.writeInt(packet.c)
	buffer.writeBool(packet.g)


static func read(buffer):
	if (!buffer.readBool()):
		return null
	var packet = buffer.newInstance(PROTOCOL_ID)
	var result0 = buffer.readInt()
	packet.c = result0
	var result1 = buffer.readBool() 
	packet.g = result1
	return packet
