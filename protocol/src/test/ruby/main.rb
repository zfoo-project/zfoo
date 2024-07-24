require './zfooruby/ProtocolManager.rb'
require './zfooruby/ByteBuffer.rb'

def assertEqual(a, b)
  if a != b
    raise "assert failed"
  end
end

def byteBufferTest
  buffer = ByteBuffer.new()
  buffer.writeBool(true)
  assertEqual(buffer.readBool(), true)
  buffer.writeByte(100)
  assertEqual(buffer.readByte(), 100)
  buffer.writeShort(127)
  assertEqual(buffer.readShort(), 127)
  buffer.writeRawInt(2147483647)
  assertEqual(buffer.readRawInt(), 2147483647)
  buffer.writeRawInt(-2147483648)
  assertEqual(buffer.readRawInt(), -2147483648)
  buffer.writeInt(2147483647)
  assertEqual(buffer.readInt(), 2147483647)
  buffer.writeInt(-2147483648)
  assertEqual(buffer.readInt(), -2147483648)
  buffer.writeLong(9999999999999999)
  assertEqual(buffer.readLong(), 9999999999999999)
  buffer.writeLong(444444444444444)
  assertEqual(buffer.readLong(), 444444444444444)
  buffer.writeLong(-9223372036854775808)
  assertEqual(buffer.readLong(), -9223372036854775808)
  buffer.writeString("Hello, 你好")
  assertEqual(buffer.readString(), "Hello, 你好")
  buffer.writeFloat(99.0)
  assertEqual(buffer.readFloat(), 99.0)
  buffer.writeFloat(-99.0)
  assertEqual(buffer.readFloat(), -99.0)
  buffer.writeDouble(99.0)
  assertEqual(buffer.readDouble(), 99.0)
  buffer.writeDouble(-99.0)
  assertEqual(buffer.readDouble(), -99.0)
end


ProtocolManager.initProtocol()
# data = File.read("D:\\Project\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-no-compatible.bytes")
# data = File.read("D:\\Project\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-out-compatible.bytes")
# data = File.read("D:\\Project\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-inner-compatible.bytes")
# data = File.read("D:\\Project\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-out-inner-compatible.bytes")
data = File.read("D:\\Project\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-out-inner-inner-compatible.bytes")

byteBuffer = ByteBuffer.new()
byteBuffer.writeBytesString(data)
packet = ProtocolManager.read(byteBuffer)

newByteBuffer = ByteBuffer.new()
ProtocolManager.write(newByteBuffer, packet)
newPacket = ProtocolManager.read(newByteBuffer)

puts packet.inspect
puts newPacket.inspect


byteBufferTest()
