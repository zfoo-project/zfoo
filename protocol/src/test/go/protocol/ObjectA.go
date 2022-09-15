package protocol

type ObjectA struct {
	a       int
	m       map[int]string
	objectB ObjectB
}

func (protocol ObjectA) protocolId() int16 {
	return 102
}

func (protocol ObjectA) write(buffer *ByteBuffer, packet any) {
	if buffer.WritePacketFlag(packet) {
		return
	}
	var message = packet.(*ObjectA)
	buffer.WriteInt(message.a)
	buffer.WriteIntStringMap(message.m)
	buffer.WritePacket(&message.objectB, 103)
}

func (protocol ObjectA) read(buffer *ByteBuffer) any {
	var packet = new(ObjectA)
	if !buffer.ReadBool() {
		return packet
	}
	var result0 = buffer.ReadInt()
	packet.a = result0
	var map1 = buffer.ReadIntStringMap()
	packet.m = map1
	var result2 = *buffer.ReadPacket(103).(*ObjectB)
	packet.objectB = result2
	return packet
}
