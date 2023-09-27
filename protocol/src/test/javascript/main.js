import ByteBuffer from './zfoojs/buffer/ByteBuffer.js';
import ProtocolManager from './zfoojs/ProtocolManager.js';
import fs from "fs";

function assert(flag) {
    if (!flag) {
        console.error("exception happen");
    }
}

// const data = fs.readFileSync('D:\\Project\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-no-compatible.bytes');
// const data = fs.readFileSync('D:\\Project\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-out-compatible.bytes');
// const data = fs.readFileSync('D:\\Project\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-inner-compatible.bytes');
// const data = fs.readFileSync('D:\\Project\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-out-inner-compatible.bytes');
const data = fs.readFileSync('D:\\Project\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-out-inner-inner-compatible.bytes');

const arrayBytes = new Uint8Array(data.length);
data.copy(arrayBytes, 0, 0, data.length);

const byteBuffer = new ByteBuffer();
byteBuffer.writeBytes(arrayBytes);

const packet = ProtocolManager.read(byteBuffer);
console.log(packet);

const newByteBuffer = new ByteBuffer();
ProtocolManager.write(newByteBuffer, packet);

const newPacket = ProtocolManager.read(newByteBuffer);
console.log(newPacket);

// set和map是无序的，所以有的时候输入和输出的字节流有可能不一致，但是长度一定是一致的
// assert(byteBuffer.readOffset == newByteBuffer.writeOffset)
byteBufferTest();


function byteBufferTest() {
    let buffer = new ByteBuffer();
    assert(buffer.getCapacity() == 128);

    // boolean
    const testBoolean = [false, true];
    testBoolean.forEach((value) => {
        buffer.writeBoolean(value);
        assert(buffer.readBoolean() == value);
    });

    assert(buffer.writeOffset == testBoolean.length);
    assert(buffer.readOffset == testBoolean.length);

    // byte
    buffer = new ByteBuffer();
    const testByte = [-128, -99, 0, 99, 127];
    testByte.forEach((value) => {
        buffer.writeByte(value);
        assert(buffer.readByte() == value);
    });
    assert(buffer.writeOffset == testByte.length);
    assert(buffer.readOffset == testByte.length);

    // short
    buffer = new ByteBuffer();
    const testShort = [-32768, -99, 0, 99, 32767];
    testShort.forEach((value) => {
        buffer.writeShort(value);
        assert(buffer.readShort() == value);
    });
    assert(buffer.writeOffset == testShort.length * 2);
    assert(buffer.readOffset == testShort.length * 2);

    // int
    buffer = new ByteBuffer();
    const testInt = [-2147483648, -99, 0, 99, 2147483647];
    testInt.forEach((value) => {
        buffer.writeInt(value);
        assert(buffer.readInt() == value);
    });

    // float
    buffer = new ByteBuffer();
    const testFloat = [-999.5, -99.5, 0, 99.5, 999.5];
    testFloat.forEach((value) => {
        buffer.writeFloat(value);
        assert(buffer.readFloat() == value);
    });

    // double
    buffer = new ByteBuffer();
    const testDouble = [-999.5, -99.5, 0, 99.5, 999.5];
    testDouble.forEach((value) => {
        buffer.writeDouble(value);
        assert(buffer.readDouble() == value);
    });

    // string
    buffer = new ByteBuffer();
    const testString = 'hello world!';
    buffer.writeString(testString);
    assert(buffer.readString() == testString);
}