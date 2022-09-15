package protocol

type Message struct {
	code int
    message string
    module int8
}

func (protocol Message) protocolId() int16 {
	return 100
}

func (protocol Message) write(buffer *ByteBuffer, packet any) {
	if buffer.WritePacketFlag(packet) {
		return
	}
	var message = packet.(*Message)
	buffer.WriteInt(message.code)
    buffer.WriteString(message.message)
    buffer.WriteByte(message.module)
}

func (protocol Message) read(buffer *ByteBuffer) any {
	var packet = new(Message)
	if !buffer.ReadBool() {
		return packet
	}
	var result0 = buffer.ReadInt()
    packet.code = result0
    var result1 = buffer.ReadString()
    packet.message = result1
    var result2 = buffer.ReadByte()
    packet.module = result2
	return packet
}


type Error struct {
	errorCode int
    errorMessage string
    module int
}

func (protocol Error) protocolId() int16 {
	return 101
}

func (protocol Error) write(buffer *ByteBuffer, packet any) {
	if buffer.WritePacketFlag(packet) {
		return
	}
	var message = packet.(*Error)
	buffer.WriteInt(message.errorCode)
    buffer.WriteString(message.errorMessage)
    buffer.WriteInt(message.module)
}

func (protocol Error) read(buffer *ByteBuffer) any {
	var packet = new(Error)
	if !buffer.ReadBool() {
		return packet
	}
	var result0 = buffer.ReadInt()
    packet.errorCode = result0
    var result1 = buffer.ReadString()
    packet.errorMessage = result1
    var result2 = buffer.ReadInt()
    packet.module = result2
	return packet
}


type Heartbeat struct {
	
}

func (protocol Heartbeat) protocolId() int16 {
	return 102
}

func (protocol Heartbeat) write(buffer *ByteBuffer, packet any) {
	if buffer.WritePacketFlag(packet) {
		return
	}
}

func (protocol Heartbeat) read(buffer *ByteBuffer) any {
	var packet = new(Heartbeat)
	if !buffer.ReadBool() {
		return packet
	}
	return packet
}


type Ping struct {
	
}

func (protocol Ping) protocolId() int16 {
	return 103
}

func (protocol Ping) write(buffer *ByteBuffer, packet any) {
	if buffer.WritePacketFlag(packet) {
		return
	}
}

func (protocol Ping) read(buffer *ByteBuffer) any {
	var packet = new(Ping)
	if !buffer.ReadBool() {
		return packet
	}
	return packet
}


type Pong struct {
	time int64
}

func (protocol Pong) protocolId() int16 {
	return 104
}

func (protocol Pong) write(buffer *ByteBuffer, packet any) {
	if buffer.WritePacketFlag(packet) {
		return
	}
	var message = packet.(*Pong)
	buffer.WriteLong(message.time)
}

func (protocol Pong) read(buffer *ByteBuffer) any {
	var packet = new(Pong)
	if !buffer.ReadBool() {
		return packet
	}
	var result0 = buffer.ReadLong()
    packet.time = result0
	return packet
}


type PairLong struct {
	key int64
    value int64
}

func (protocol PairLong) protocolId() int16 {
	return 111
}

func (protocol PairLong) write(buffer *ByteBuffer, packet any) {
	if buffer.WritePacketFlag(packet) {
		return
	}
	var message = packet.(*PairLong)
	buffer.WriteLong(message.key)
    buffer.WriteLong(message.value)
}

func (protocol PairLong) read(buffer *ByteBuffer) any {
	var packet = new(PairLong)
	if !buffer.ReadBool() {
		return packet
	}
	var result0 = buffer.ReadLong()
    packet.key = result0
    var result1 = buffer.ReadLong()
    packet.value = result1
	return packet
}


type PairString struct {
	key string
    value string
}

func (protocol PairString) protocolId() int16 {
	return 112
}

func (protocol PairString) write(buffer *ByteBuffer, packet any) {
	if buffer.WritePacketFlag(packet) {
		return
	}
	var message = packet.(*PairString)
	buffer.WriteString(message.key)
    buffer.WriteString(message.value)
}

func (protocol PairString) read(buffer *ByteBuffer) any {
	var packet = new(PairString)
	if !buffer.ReadBool() {
		return packet
	}
	var result0 = buffer.ReadString()
    packet.key = result0
    var result1 = buffer.ReadString()
    packet.value = result1
	return packet
}


type PairLS struct {
	key int64
    value string
}

func (protocol PairLS) protocolId() int16 {
	return 113
}

func (protocol PairLS) write(buffer *ByteBuffer, packet any) {
	if buffer.WritePacketFlag(packet) {
		return
	}
	var message = packet.(*PairLS)
	buffer.WriteLong(message.key)
    buffer.WriteString(message.value)
}

func (protocol PairLS) read(buffer *ByteBuffer) any {
	var packet = new(PairLS)
	if !buffer.ReadBool() {
		return packet
	}
	var result0 = buffer.ReadLong()
    packet.key = result0
    var result1 = buffer.ReadString()
    packet.value = result1
	return packet
}


type TripleLong struct {
	left int64
    middle int64
    right int64
}

func (protocol TripleLong) protocolId() int16 {
	return 114
}

func (protocol TripleLong) write(buffer *ByteBuffer, packet any) {
	if buffer.WritePacketFlag(packet) {
		return
	}
	var message = packet.(*TripleLong)
	buffer.WriteLong(message.left)
    buffer.WriteLong(message.middle)
    buffer.WriteLong(message.right)
}

func (protocol TripleLong) read(buffer *ByteBuffer) any {
	var packet = new(TripleLong)
	if !buffer.ReadBool() {
		return packet
	}
	var result0 = buffer.ReadLong()
    packet.left = result0
    var result1 = buffer.ReadLong()
    packet.middle = result1
    var result2 = buffer.ReadLong()
    packet.right = result2
	return packet
}


type TripleString struct {
	left string
    middle string
    right string
}

func (protocol TripleString) protocolId() int16 {
	return 115
}

func (protocol TripleString) write(buffer *ByteBuffer, packet any) {
	if buffer.WritePacketFlag(packet) {
		return
	}
	var message = packet.(*TripleString)
	buffer.WriteString(message.left)
    buffer.WriteString(message.middle)
    buffer.WriteString(message.right)
}

func (protocol TripleString) read(buffer *ByteBuffer) any {
	var packet = new(TripleString)
	if !buffer.ReadBool() {
		return packet
	}
	var result0 = buffer.ReadString()
    packet.left = result0
    var result1 = buffer.ReadString()
    packet.middle = result1
    var result2 = buffer.ReadString()
    packet.right = result2
	return packet
}


type TripleLSS struct {
	left int64
    middle string
    right string
}

func (protocol TripleLSS) protocolId() int16 {
	return 116
}

func (protocol TripleLSS) write(buffer *ByteBuffer, packet any) {
	if buffer.WritePacketFlag(packet) {
		return
	}
	var message = packet.(*TripleLSS)
	buffer.WriteLong(message.left)
    buffer.WriteString(message.middle)
    buffer.WriteString(message.right)
}

func (protocol TripleLSS) read(buffer *ByteBuffer) any {
	var packet = new(TripleLSS)
	if !buffer.ReadBool() {
		return packet
	}
	var result0 = buffer.ReadLong()
    packet.left = result0
    var result1 = buffer.ReadString()
    packet.middle = result1
    var result2 = buffer.ReadString()
    packet.right = result2
	return packet
}
