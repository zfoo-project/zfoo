package protocol

type NormalObject struct {
	A int8
    Aaa []int8
    B int16
    C int
    D int64
    E float32
    F float64
    G bool
    Jj string
    Kk ObjectA
    L []int
    Ll []int64
    Lll []ObjectA
    Llll []string
    M map[int]string
    Mm map[int]ObjectA
    S []int
    Ssss []string
    OutCompatibleValue int
}

func (protocol NormalObject) ProtocolId() int16 {
	return 101
}

func (protocol NormalObject) write(buffer *ByteBuffer, packet any) {
	if packet == nil {
	    buffer.WriteInt(0)
		return
	}
	var message = packet.(*NormalObject)
	var beforeWriteIndex = buffer.WriteOffset()
    buffer.WriteInt(854)
    buffer.WriteByte(message.A)
    buffer.WriteByteArray(message.Aaa)
    buffer.WriteShort(message.B)
    buffer.WriteInt(message.C)
    buffer.WriteLong(message.D)
    buffer.WriteFloat(message.E)
    buffer.WriteDouble(message.F)
    buffer.WriteBool(message.G)
    buffer.WriteString(message.Jj)
    buffer.WritePacket(&message.Kk, 102)
    buffer.WriteIntArray(message.L)
    buffer.WriteLongArray(message.Ll)
    if message.Lll == nil {
        buffer.WriteInt(0)
    } else {
        buffer.WriteInt(len(message.Lll))
        var length0 = len(message.Lll)
        for i1 := 0; i1 < length0; i1++ {
            var element2 = message.Lll[i1]
            buffer.WritePacket(&element2, 102)
        }
    }
    buffer.WriteStringArray(message.Llll)
    buffer.WriteIntStringMap(message.M)
    if (message.Mm == nil) || (len(message.Mm) == 0) {
        buffer.WriteInt(0)
    } else {
        buffer.WriteInt(len(message.Mm))
        for keyElement4, valueElement5 := range message.Mm {
            buffer.WriteInt(keyElement4)
            buffer.WritePacket(&valueElement5, 102)
        }
    }
    buffer.WriteIntArray(message.S)
    buffer.WriteStringArray(message.Ssss)
    buffer.WriteInt(message.OutCompatibleValue)
    buffer.AdjustPadding(854, beforeWriteIndex)
}

func (protocol NormalObject) read(buffer *ByteBuffer) any {
	var packet = new(NormalObject)
	var length = buffer.ReadInt()
	if length == 0 {
		return packet
	}
	var beforeReadIndex = buffer.ReadOffset()
	var result6 = buffer.ReadByte()
    packet.A = result6
    var array7 = buffer.ReadByteArray()
    packet.Aaa = array7
    var result8 = buffer.ReadShort()
    packet.B = result8
    var result9 = buffer.ReadInt()
    packet.C = result9
    var result10 = buffer.ReadLong()
    packet.D = result10
    var result11 = buffer.ReadFloat()
    packet.E = result11
    var result12 = buffer.ReadDouble()
    packet.F = result12
    var result13 = buffer.ReadBool()
    packet.G = result13
    var result14 = buffer.ReadString()
    packet.Jj = result14
    var result15 = *buffer.ReadPacket(102).(*ObjectA)
    packet.Kk = result15
    var list16 = buffer.ReadIntArray()
    packet.L = list16
    var list17 = buffer.ReadLongArray()
    packet.Ll = list17
    var size20 = buffer.ReadInt()
    var result18 = make([]ObjectA, size20)
    if size20 > 0 {
        for index19 := 0; index19 < size20; index19++ {
            var result21 = *buffer.ReadPacket(102).(*ObjectA)
            result18[index19] = result21
        }
    }
    packet.Lll = result18
    var list22 = buffer.ReadStringArray()
    packet.Llll = list22
    var map23 = buffer.ReadIntStringMap()
    packet.M = map23
    var size25 = buffer.ReadInt()
    var result24 = make(map[int]ObjectA)
    if size25 > 0 {
        for index26 := 0; index26 < size25; index26++ {
            var result27 = buffer.ReadInt()
            var result28 = *buffer.ReadPacket(102).(*ObjectA)
            result24[result27] = result28
        }
    }
    packet.Mm = result24
    var set29 = buffer.ReadIntArray()
    packet.S = set29
    var set30 = buffer.ReadStringArray()
    packet.Ssss = set30
    if buffer.CompatibleRead(beforeReadIndex, length) {
        var result31 = buffer.ReadInt()
        packet.OutCompatibleValue = result31
    }
	if length > 0 {
        buffer.SetReadOffset(beforeReadIndex + length)
    }
	return packet
}
