package protocol

type Message struct {
	Code int
    Message string
    Module int8
}

func (protocol Message) ProtocolId() int16 {
	return 100
}

func (protocol Message) write(buffer *ByteBuffer, packet any) {
	if buffer.WritePacketFlag(packet) {
		return
	}
	var message = packet.(*Message)
	buffer.WriteInt(message.Code)
    buffer.WriteString(message.Message)
    buffer.WriteByte(message.Module)
}

func (protocol Message) read(buffer *ByteBuffer) any {
	var packet = new(Message)
	if !buffer.ReadBool() {
		return packet
	}
	var result0 = buffer.ReadInt()
    packet.Code = result0
    var result1 = buffer.ReadString()
    packet.Message = result1
    var result2 = buffer.ReadByte()
    packet.Module = result2
	return packet
}


type Error struct {
	ErrorCode int
    ErrorMessage string
    Module int
}

func (protocol Error) ProtocolId() int16 {
	return 101
}

func (protocol Error) write(buffer *ByteBuffer, packet any) {
	if buffer.WritePacketFlag(packet) {
		return
	}
	var message = packet.(*Error)
	buffer.WriteInt(message.ErrorCode)
    buffer.WriteString(message.ErrorMessage)
    buffer.WriteInt(message.Module)
}

func (protocol Error) read(buffer *ByteBuffer) any {
	var packet = new(Error)
	if !buffer.ReadBool() {
		return packet
	}
	var result0 = buffer.ReadInt()
    packet.ErrorCode = result0
    var result1 = buffer.ReadString()
    packet.ErrorMessage = result1
    var result2 = buffer.ReadInt()
    packet.Module = result2
	return packet
}


type Heartbeat struct {
	
}

func (protocol Heartbeat) ProtocolId() int16 {
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

func (protocol Ping) ProtocolId() int16 {
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
	Time int64
}

func (protocol Pong) ProtocolId() int16 {
	return 104
}

func (protocol Pong) write(buffer *ByteBuffer, packet any) {
	if buffer.WritePacketFlag(packet) {
		return
	}
	var message = packet.(*Pong)
	buffer.WriteLong(message.Time)
}

func (protocol Pong) read(buffer *ByteBuffer) any {
	var packet = new(Pong)
	if !buffer.ReadBool() {
		return packet
	}
	var result0 = buffer.ReadLong()
    packet.Time = result0
	return packet
}


type PairLong struct {
	Key int64
    Value int64
}

func (protocol PairLong) ProtocolId() int16 {
	return 111
}

func (protocol PairLong) write(buffer *ByteBuffer, packet any) {
	if buffer.WritePacketFlag(packet) {
		return
	}
	var message = packet.(*PairLong)
	buffer.WriteLong(message.Key)
    buffer.WriteLong(message.Value)
}

func (protocol PairLong) read(buffer *ByteBuffer) any {
	var packet = new(PairLong)
	if !buffer.ReadBool() {
		return packet
	}
	var result0 = buffer.ReadLong()
    packet.Key = result0
    var result1 = buffer.ReadLong()
    packet.Value = result1
	return packet
}


type PairString struct {
	Key string
    Value string
}

func (protocol PairString) ProtocolId() int16 {
	return 112
}

func (protocol PairString) write(buffer *ByteBuffer, packet any) {
	if buffer.WritePacketFlag(packet) {
		return
	}
	var message = packet.(*PairString)
	buffer.WriteString(message.Key)
    buffer.WriteString(message.Value)
}

func (protocol PairString) read(buffer *ByteBuffer) any {
	var packet = new(PairString)
	if !buffer.ReadBool() {
		return packet
	}
	var result0 = buffer.ReadString()
    packet.Key = result0
    var result1 = buffer.ReadString()
    packet.Value = result1
	return packet
}


type PairLS struct {
	Key int64
    Value string
}

func (protocol PairLS) ProtocolId() int16 {
	return 113
}

func (protocol PairLS) write(buffer *ByteBuffer, packet any) {
	if buffer.WritePacketFlag(packet) {
		return
	}
	var message = packet.(*PairLS)
	buffer.WriteLong(message.Key)
    buffer.WriteString(message.Value)
}

func (protocol PairLS) read(buffer *ByteBuffer) any {
	var packet = new(PairLS)
	if !buffer.ReadBool() {
		return packet
	}
	var result0 = buffer.ReadLong()
    packet.Key = result0
    var result1 = buffer.ReadString()
    packet.Value = result1
	return packet
}


type TripleLong struct {
	Left int64
    Middle int64
    Right int64
}

func (protocol TripleLong) ProtocolId() int16 {
	return 114
}

func (protocol TripleLong) write(buffer *ByteBuffer, packet any) {
	if buffer.WritePacketFlag(packet) {
		return
	}
	var message = packet.(*TripleLong)
	buffer.WriteLong(message.Left)
    buffer.WriteLong(message.Middle)
    buffer.WriteLong(message.Right)
}

func (protocol TripleLong) read(buffer *ByteBuffer) any {
	var packet = new(TripleLong)
	if !buffer.ReadBool() {
		return packet
	}
	var result0 = buffer.ReadLong()
    packet.Left = result0
    var result1 = buffer.ReadLong()
    packet.Middle = result1
    var result2 = buffer.ReadLong()
    packet.Right = result2
	return packet
}


type TripleString struct {
	Left string
    Middle string
    Right string
}

func (protocol TripleString) ProtocolId() int16 {
	return 115
}

func (protocol TripleString) write(buffer *ByteBuffer, packet any) {
	if buffer.WritePacketFlag(packet) {
		return
	}
	var message = packet.(*TripleString)
	buffer.WriteString(message.Left)
    buffer.WriteString(message.Middle)
    buffer.WriteString(message.Right)
}

func (protocol TripleString) read(buffer *ByteBuffer) any {
	var packet = new(TripleString)
	if !buffer.ReadBool() {
		return packet
	}
	var result0 = buffer.ReadString()
    packet.Left = result0
    var result1 = buffer.ReadString()
    packet.Middle = result1
    var result2 = buffer.ReadString()
    packet.Right = result2
	return packet
}


type TripleLSS struct {
	Left int64
    Middle string
    Right string
}

func (protocol TripleLSS) ProtocolId() int16 {
	return 116
}

func (protocol TripleLSS) write(buffer *ByteBuffer, packet any) {
	if buffer.WritePacketFlag(packet) {
		return
	}
	var message = packet.(*TripleLSS)
	buffer.WriteLong(message.Left)
    buffer.WriteString(message.Middle)
    buffer.WriteString(message.Right)
}

func (protocol TripleLSS) read(buffer *ByteBuffer) any {
	var packet = new(TripleLSS)
	if !buffer.ReadBool() {
		return packet
	}
	var result0 = buffer.ReadLong()
    packet.Left = result0
    var result1 = buffer.ReadString()
    packet.Middle = result1
    var result2 = buffer.ReadString()
    packet.Right = result2
	return packet
}
