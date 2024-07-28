import Foundation

ProtocolManager.initProtocol()

//let filePath = "/Users/wjd/Desktop/zfoo-swift/zfoo-swift-support/zfoo-swift-support/compatible/normal-no-compatible.bytes"
//let filePath = "/Users/wjd/Desktop/zfoo-swift/zfoo-swift-support/zfoo-swift-support/compatible/normal-inner-compatible.bytes"
//let filePath = "/Users/wjd/Desktop/zfoo-swift/zfoo-swift-support/zfoo-swift-support/compatible/normal-out-compatible.bytes"
//let filePath = "/Users/wjd/Desktop/zfoo-swift/zfoo-swift-support/zfoo-swift-support/compatible/normal-out-inner-compatible.bytes"
//let filePath = "/Users/wjd/Desktop/zfoo-swift/zfoo-swift-support/zfoo-swift-support/compatible/normal-out-inner-inner-compatible-big.bytes"
let filePath = "/Users/wjd/Desktop/zfoo-swift/zfoo-swift-support/zfoo-swift-support/compatible/normal-out-inner-inner-compatible.bytes"
let fileUrl = URL(fileURLWithPath: filePath)
let fileData = try Data(contentsOf: fileUrl)
let bytes = [UInt8](fileData)
let buffer = ByteBuffer()
buffer.writeUBytes(bytes)
let packet = ProtocolManager.read(buffer)
let newBuffer = ByteBuffer()
ProtocolManager.write(newBuffer, packet)
let newPacket = ProtocolManager.read(newBuffer)

byteBufferTest()

func byteBufferTest() {
    let buffer = ByteBuffer()
    assert(buffer.getCapacity() == 128)
    buffer.writeBool(true)
    assert(buffer.readBool() == true)
    buffer.writeByte(99)
    assert(buffer.readByte() == 99)
    buffer.writeByte(-99)
    assert(buffer.readByte() == -99)
    buffer.writeShort(9999)
    assert(buffer.readShort() == 9999)
    buffer.writeInt(2147483647)
    assert(buffer.readInt() == 2147483647)
    buffer.writeInt(-2147483648)
    assert(buffer.readInt() == -2147483648)
    buffer.writeRawInt(2147483647)
    assert(buffer.readRawInt() == 2147483647)
    buffer.writeRawInt(-2147483648)
    assert(buffer.readRawInt() == -2147483648)
    buffer.writeLong(9223372036854775807)
    assert(buffer.readLong() == 9223372036854775807)
    buffer.writeLong(-9999999999999)
    assert(buffer.readLong() == -9999999999999)
    buffer.writeRawLong(9223372036854775807)
    assert(buffer.readRawLong() == 9223372036854775807)
    buffer.writeRawLong(-9999999999999)
    assert(buffer.readRawLong() == -9999999999999)
    buffer.writeFloat(99.0)
    assert(buffer.readFloat() == 99.0)
    buffer.writeFloat(-99.0)
    assert(buffer.readFloat() == -99.0)
    buffer.writeDouble(99.0)
    assert(buffer.readDouble() == 99.0)
    buffer.writeDouble(-99.0)
    assert(buffer.readDouble() == -99.0)
    buffer.writeString("Hello World! 你好，世界！")
    assert(buffer.readString() == "Hello World! 你好，世界！")
}
