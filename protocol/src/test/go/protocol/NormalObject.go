package protocol

type NormalObject struct {
	a    int8
	aaa  []int8
	b    int16
	c    int
	d    int64
	e    float32
	f    float64
	g    bool
	jj   string
	kk   ObjectA
	l    []int
	ll   []int64
	lll  []ObjectA
	llll []string
	m    map[int]string
	mm   map[int]ObjectA
	s    []int
	ssss []string
}

func (protocol NormalObject) protocolId() int16 {
	return 101
}

func (protocol NormalObject) write(buffer *ByteBuffer, packet any) {
	if buffer.WritePacketFlag(packet) {
		return
	}
	var message = packet.(*NormalObject)
	buffer.WriteByte(message.a)
	buffer.WriteByteArray(message.aaa)
	buffer.WriteShort(message.b)
	buffer.WriteInt(message.c)
	buffer.WriteLong(message.d)
	buffer.WriteFloat(message.e)
	buffer.WriteDouble(message.f)
	buffer.WriteBool(message.g)
	buffer.WriteString(message.jj)
	buffer.WritePacket(&message.kk, 102)
	buffer.WriteIntArray(message.l)
	buffer.WriteLongArray(message.ll)
	if message.lll == nil {
		buffer.WriteInt(0)
	} else {
		buffer.WriteInt(len(message.lll))
		var length0 = len(message.lll)
		for i1 := 0; i1 < length0; i1++ {
			var element2 = message.lll[i1]
			buffer.WritePacket(&element2, 102)
		}
	}
	buffer.WriteStringArray(message.llll)
	buffer.WriteIntStringMap(message.m)
	if (message.mm == nil) || (len(message.mm) == 0) {
		buffer.WriteInt(0)
	} else {
		buffer.WriteInt(len(message.mm))
		for keyElement4, valueElement5 := range message.mm {
			buffer.WriteInt(keyElement4)
			buffer.WritePacket(&valueElement5, 102)
		}
	}
	buffer.WriteIntArray(message.s)
	buffer.WriteStringArray(message.ssss)
}

func (protocol NormalObject) read(buffer *ByteBuffer) any {
	var packet = new(NormalObject)
	if !buffer.ReadBool() {
		return packet
	}
	var result6 = buffer.ReadByte()
	packet.a = result6
	var array7 = buffer.ReadByteArray()
	packet.aaa = array7
	var result8 = buffer.ReadShort()
	packet.b = result8
	var result9 = buffer.ReadInt()
	packet.c = result9
	var result10 = buffer.ReadLong()
	packet.d = result10
	var result11 = buffer.ReadFloat()
	packet.e = result11
	var result12 = buffer.ReadDouble()
	packet.f = result12
	var result13 = buffer.ReadBool()
	packet.g = result13
	var result14 = buffer.ReadString()
	packet.jj = result14
	var result15 = *buffer.ReadPacket(102).(*ObjectA)
	packet.kk = result15
	var list16 = buffer.ReadIntArray()
	packet.l = list16
	var list17 = buffer.ReadLongArray()
	packet.ll = list17
	var size20 = buffer.ReadInt()
	var result18 = make([]ObjectA, size20)
	if size20 > 0 {
		for index19 := 0; index19 < size20; index19++ {
			var result21 = *buffer.ReadPacket(102).(*ObjectA)
			result18[index19] = result21
		}
	}
	packet.lll = result18
	var list22 = buffer.ReadStringArray()
	packet.llll = list22
	var map23 = buffer.ReadIntStringMap()
	packet.m = map23
	var size25 = buffer.ReadInt()
	var result24 = make(map[int]ObjectA)
	if size25 > 0 {
		for index26 := 0; index26 < size25; index26++ {
			var result27 = buffer.ReadInt()
			var result28 = *buffer.ReadPacket(102).(*ObjectA)
			result24[result27] = result28
		}
	}
	packet.mm = result24
	var set29 = buffer.ReadIntArray()
	packet.s = set29
	var set30 = buffer.ReadStringArray()
	packet.ssss = set30
	return packet
}
