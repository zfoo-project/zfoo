import ByteBuffer from './zfoojs/buffer/ByteBuffer.js';
import ProtocolManager from './zfoojs/ProtocolManager.js';

const fs = require('fs');

describe('jsProtocolTest', () => {
    it('complexObjectTest', () => {
        const data = fs.readFileSync('../resources/ComplexObject.bytes');

        const arrayBytes = new Uint8Array(data.length);
        data.copy(arrayBytes, 0, 0, data.length);

        const byteBuffer = new ByteBuffer();
        byteBuffer.writeBytes(arrayBytes);

        const packet = ProtocolManager.read(byteBuffer);
        // complexObjec是老的协议，所以序列化回来myCompatible是nil，所以要重新赋值
        packet.myCompatible = 0
        console.log(packet);

        const newByteBuffer = new ByteBuffer();
        ProtocolManager.write(newByteBuffer, packet);

        const newPacket = ProtocolManager.read(newByteBuffer);
        console.log(newPacket);

        expect(byteBuffer.readOffset).toBeLessThan(newByteBuffer.writeOffset);

        // set和map是无序的，所以有的时候输入和输出的字节流有可能不一致，但是长度一定是一致的
        const length = newByteBuffer.writeOffset;
        byteBuffer.setReadOffset(0);
        newByteBuffer.setReadOffset(0);
        for (let i = 0; i < length; i++) {
            expect(byteBuffer.readByte()).toBe(newByteBuffer.readByte());
        }
    });

    it('byteBufferTest', () => {
        let buffer = new ByteBuffer();
        expect(buffer.getCapacity()).toBe(128);

        // boolean
        const testBoolean = [false, true];
        testBoolean.forEach((value) => {
            buffer.writeBoolean(value);
            expect(buffer.readBoolean()).toBe(value);
        });
        expect(buffer.writeOffset).toBe(testBoolean.length);
        expect(buffer.readOffset).toBe(testBoolean.length);

        // byte
        buffer = new ByteBuffer();
        const testByte = [-128, -99, 0, 99, 127];
        testByte.forEach((value) => {
            buffer.writeByte(value);
            expect(buffer.readByte()).toBe(value);
        });
        expect(buffer.writeOffset).toBe(testByte.length);
        expect(buffer.readOffset).toBe(testByte.length);

        // short
        buffer = new ByteBuffer();
        const testShort = [-32768, -99, 0, 99, 32767];
        testShort.forEach((value) => {
            buffer.writeShort(value);
            expect(buffer.readShort()).toBe(value);
        });
        expect(buffer.writeOffset).toBe(testShort.length * 2);
        expect(buffer.readOffset).toBe(testShort.length * 2);

        // int
        buffer = new ByteBuffer();
        const testInt = [-2147483648, -99, 0, 99, 2147483647];
        testInt.forEach((value) => {
            buffer.writeInt(value);
            expect(buffer.readInt()).toBe(value);
        });

        // float
        buffer = new ByteBuffer();
        const testFloat = [-999.5, -99.5, 0, 99.5, 999.5];
        testFloat.forEach((value) => {
            buffer.writeFloat(value);
            expect(buffer.readFloat()).toBe(value);
        });

        // double
        buffer = new ByteBuffer();
        const testDouble = [-999.5, -99.5, 0, 99.5, 999.5];
        testDouble.forEach((value) => {
            buffer.writeDouble(value);
            expect(buffer.readDouble()).toBe(value);
        });

        // string
        buffer = new ByteBuffer();
        const testString = 'hello world!';
        buffer.writeString(testString);
        expect(buffer.readString()).toBe(testString);

        // char
        buffer = new ByteBuffer();
        const testChar = 'h';
        buffer.writeChar(testString);
        expect(buffer.readChar()).toBe(testChar);
    });
});
