package protocol

type ObjectA struct {
	A int
    M map[int]string
    ObjectB ObjectB
    InnerCompatibleValue int
}

func (protocol ObjectA) ProtocolId() int16 {
	return 102
}

func (protocol ObjectA) write(buffer *ByteBuffer, packet any) {
	if packet == nil {
	    buffer.WriteInt(0)
		return
	}
	var message = packet.(*ObjectA)
	var beforeWriteIndex = buffer.WriteOffset()
    buffer.WriteInt(201)
    buffer.WriteInt(message.A)
    buffer.WriteIntStringMap(message.M)
    buffer.WritePacket(&message.ObjectB, 103)
    buffer.WriteInt(message.InnerCompatibleValue)
    buffer.AdjustPadding(201, beforeWriteIndex)
}

func (protocol ObjectA) read(buffer *ByteBuffer) any {
	var packet = new(ObjectA)
	var length = buffer.ReadInt()
	if length == 0 {
		return packet
	}
	var beforeReadIndex = buffer.ReadOffset()
	var result0 = buffer.ReadInt()
    packet.A = result0
    var map1 = buffer.ReadIntStringMap()
    packet.M = map1
    var result2 = *buffer.ReadPacket(103).(*ObjectB)
    packet.ObjectB = result2
    if buffer.CompatibleRead(beforeReadIndex, length) {
        var result3 = buffer.ReadInt()
        packet.InnerCompatibleValue = result3
    }
	if length > 0 {
        buffer.SetReadOffset(beforeReadIndex + length)
    }
	return packet
}
