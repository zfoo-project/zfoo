# @author jaysunxiao
# @version 3.0

var flag # boolean

const PROTOCOL_ID = 103

static func write(buffer, packet):
	if (buffer.writePacketFlag(packet)):
		return
	buffer.writeBool(packet.flag)


static func read(buffer):
	if (!buffer.readBool()):
		return null
	var packet = buffer.newInstance(PROTOCOL_ID)
	var result0 = buffer.readBool() 
	packet.flag = result0
	return packet
