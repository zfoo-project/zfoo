package zfoogo


type EmptyObject struct {
	
}

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
	OutCompatibleValue2 int
}

type ObjectA struct {
	A int
	M map[int]string
	ObjectB ObjectB
	InnerCompatibleValue int
}

type ObjectB struct {
	Flag bool
	InnerCompatibleValue int
}

type SimpleObject struct {
	C int
	G bool
}

func (protocol EmptyObject) ProtocolId() int16 {
	return 0
}

func (protocol EmptyObject) write(buffer *ByteBuffer, packet any) {
	if packet == nil {
	    buffer.WriteInt(0)
		return
	}
	buffer.WriteInt(-1)
}

func (protocol EmptyObject) read(buffer *ByteBuffer) any {
	var packet = new(EmptyObject)
	var length = buffer.ReadInt()
	if length == 0 {
		return packet
	}
	var beforeReadIndex = buffer.ReadOffset()
	if length > 0 {
        buffer.SetReadOffset(beforeReadIndex + length)
    }
	return packet
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
	buffer.WriteInt(857)
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
	buffer.WriteInt(message.OutCompatibleValue2)
	buffer.AdjustPadding(857, beforeWriteIndex)
}

func (protocol NormalObject) read(buffer *ByteBuffer) any {
	var packet = new(NormalObject)
	var length = buffer.ReadInt()
	if length == 0 {
		return packet
	}
	var beforeReadIndex = buffer.ReadOffset()
	var result0 = buffer.ReadByte()
	packet.A = result0
	var array1 = buffer.ReadByteArray()
	packet.Aaa = array1
	var result2 = buffer.ReadShort()
	packet.B = result2
	var result3 = buffer.ReadInt()
	packet.C = result3
	var result4 = buffer.ReadLong()
	packet.D = result4
	var result5 = buffer.ReadFloat()
	packet.E = result5
	var result6 = buffer.ReadDouble()
	packet.F = result6
	var result7 = buffer.ReadBool()
	packet.G = result7
	var result8 = buffer.ReadString()
	packet.Jj = result8
	var result9 = *buffer.ReadPacket(102).(*ObjectA)
	packet.Kk = result9
	var list10 = buffer.ReadIntArray()
	packet.L = list10
	var list11 = buffer.ReadLongArray()
	packet.Ll = list11
	var size14 = buffer.ReadInt()
	var result12 = make([]ObjectA, size14)
	if size14 > 0 {
	    for index13 := 0; index13 < size14; index13++ {
	        var result15 = *buffer.ReadPacket(102).(*ObjectA)
	        result12[index13] = result15
	    }
	}
	packet.Lll = result12
	var list16 = buffer.ReadStringArray()
	packet.Llll = list16
	var map17 = buffer.ReadIntStringMap()
	packet.M = map17
	var size19 = buffer.ReadInt()
	var result18 = make(map[int]ObjectA)
	if size19 > 0 {
	    for index20 := 0; index20 < size19; index20++ {
	        var result21 = buffer.ReadInt()
	        var result22 = *buffer.ReadPacket(102).(*ObjectA)
	        result18[result21] = result22
	    }
	}
	packet.Mm = result18
	var set23 = buffer.ReadIntArray()
	packet.S = set23
	var set24 = buffer.ReadStringArray()
	packet.Ssss = set24
	if buffer.CompatibleRead(beforeReadIndex, length) {
	    var result25 = buffer.ReadInt()
	    packet.OutCompatibleValue = result25
	}
	if buffer.CompatibleRead(beforeReadIndex, length) {
	    var result26 = buffer.ReadInt()
	    packet.OutCompatibleValue2 = result26
	}
	if length > 0 {
        buffer.SetReadOffset(beforeReadIndex + length)
    }
	return packet
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
func (protocol SimpleObject) ProtocolId() int16 {
	return 104
}

func (protocol SimpleObject) write(buffer *ByteBuffer, packet any) {
	if packet == nil {
	    buffer.WriteInt(0)
		return
	}
	var message = packet.(*SimpleObject)
	buffer.WriteInt(-1)
	buffer.WriteInt(message.C)
	buffer.WriteBool(message.G)
}

func (protocol SimpleObject) read(buffer *ByteBuffer) any {
	var packet = new(SimpleObject)
	var length = buffer.ReadInt()
	if length == 0 {
		return packet
	}
	var beforeReadIndex = buffer.ReadOffset()
	var result0 = buffer.ReadInt()
	packet.C = result0
	var result1 = buffer.ReadBool()
	packet.G = result1
	if length > 0 {
        buffer.SetReadOffset(beforeReadIndex + length)
    }
	return packet
}