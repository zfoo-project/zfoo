package protocol

type ObjectB struct {
	Flag bool
    InnerCompatibleValue int
}

func (protocol ObjectB) ProtocolId() int16 {
	return 103
}

func (protocol ObjectB) write(buffer *ByteBuffer, packet any) {
	if packet == nil {
	    buffer.WriteInt(0)
		return
	}
	var message = packet.(*ObjectB)
	var beforeWriteIndex = buffer.WriteOffset()
    buffer.WriteInt(4)
    buffer.WriteBool(message.Flag)
    buffer.WriteInt(message.InnerCompatibleValue)
    buffer.AdjustPadding(4, beforeWriteIndex)
}

func (protocol ObjectB) read(buffer *ByteBuffer) any {
	var packet = new(ObjectB)
	var length = buffer.ReadInt()
	if length == 0 {
		return packet
	}
	var beforeReadIndex = buffer.ReadOffset()
	var result0 = buffer.ReadBool()
    packet.Flag = result0
    if buffer.CompatibleRead(beforeReadIndex, length) {
        var result1 = buffer.ReadInt()
        packet.InnerCompatibleValue = result1
    }
	if length > 0 {
        buffer.SetReadOffset(beforeReadIndex + length)
    }
	return packet
}
