import {readInt64, writeInt64} from './longbits.js';

const initSize = 128;
const maxSize = 655537;

const maxShort = 32767;
const minShort = -32768;

const maxInt = 2147483647;
const minInt = -2147483648;

// UTF-8编码与解码
// const encoder = new TextEncoder('utf-8');
// const decoder = new TextDecoder('utf-8');

// nodejs的测试环境需要用以下方式特殊处理
const util = require('util');
const encoder = new util.TextEncoder('utf-8');
const decoder = new util.TextDecoder('utf-8');

// 在js中long可以支持的最大值
// const maxLong = 9007199254740992;
// const minLong = -9007199254740992;

const copy = function copy(original, newLength) {
    if (original.byteLength > newLength) {
        throw new Error('newLength is too small');
    }
    const dst = new ArrayBuffer(newLength);
    new Uint8Array(dst).set(new Uint8Array(original));
    return dst;
};

function encodeZigzagInt(n) {
    // 有效位左移一位+符号位右移31位
    return (n << 1) ^ (n >> 31);
}

function decodeZigzagInt(n) {
    return (n >>> 1) ^ -(n & 1);
}


const ByteBuffer = function () {
    this.writeOffset = 0;
    this.readOffset = 0;
    this.buffer = new ArrayBuffer(initSize);
    this.bufferView = new DataView(this.buffer, 0, this.buffer.byteLength);

    this.setWriteOffset = function (writeOffset) {
        if (writeOffset > this.buffer.byteLength) {
            throw new Error('index out of bounds exception: readerIndex: ' + this.readOffset +
                ', writerIndex: ' + this.writeOffset +
                '(expected: 0 <= readerIndex <= writerIndex <= capacity:' + this.buffer.byteLength);
        }
        this.writeOffset = writeOffset;
    };

    this.setReadOffset = function (readOffset) {
        if (readOffset > this.writeOffset) {
            throw new Error('index out of bounds exception: readerIndex: ' + this.readOffset +
                ', writerIndex: ' + this.writeOffset +
                '(expected: 0 <= readerIndex <= writerIndex <= capacity:' + this.buffer.byteLength);
        }
        this.readOffset = readOffset;
    };

    this.getCapacity = function () {
        return this.buffer.byteLength - this.writeOffset;
    };

    this.ensureCapacity = function (minCapacity) {
        while (minCapacity - this.getCapacity() > 0) {
            const newSize = this.buffer.byteLength * 2;
            if (newSize > maxSize) {
                throw new Error('out of memory error');
            }
            this.buffer = copy(this.buffer, newSize);
            this.bufferView = new DataView(this.buffer, 0, this.buffer.byteLength);
        }
    };

    this.writeBoolean = function (value) {
        if (!(value === true || value === false)) {
            throw new Error('value must be true of false');
        }
        this.ensureCapacity(1);
        if (value === true) {
            this.bufferView.setInt8(this.writeOffset, 1);
        } else {
            this.bufferView.setInt8(this.writeOffset, 0);
        }
        this.writeOffset++;
    };

    this.readBoolean = function () {
        const value = this.bufferView.getInt8(this.readOffset);
        this.readOffset++;
        return (value === 1);
    };

    this.writeBytes = function (byteArray) {
        const length = byteArray.byteLength;
        this.ensureCapacity(length);
        new Uint8Array(this.buffer).set(new Uint8Array(byteArray), this.writeOffset);
        this.writeOffset += length;
    };

    this.writeByte = function (value) {
        this.ensureCapacity(1);
        this.bufferView.setInt8(this.writeOffset, value);
        this.writeOffset++;
    };

    this.readByte = function () {
        const value = this.bufferView.getInt8(this.readOffset);
        this.readOffset++;
        return value;
    };

    this.writeShort = function (value) {
        if (!(minShort <= value && value <= maxShort)) {
            throw new Error('value must range between minShort:-32768 and maxShort:32767');
        }
        this.ensureCapacity(2);
        this.bufferView.setInt16(this.writeOffset, value);
        this.writeOffset += 2;
    };

    this.readShort = function () {
        const value = this.bufferView.getInt16(this.readOffset);
        this.readOffset += 2;
        return value;
    };

    this.writeRawInt = function (value) {
        if (!(minInt <= value && value <= maxInt)) {
            throw new Error('value must range between minInt:-2147483648 and maxInt:2147483647');
        }
        this.ensureCapacity(4);
        this.bufferView.setInt32(this.writeOffset, value);
        this.writeOffset += 4;
    };

    this.readRawInt = function () {
        const value = this.bufferView.getInt32(this.readOffset);
        this.readOffset += 4;
        return value;
    };

    this.writeInt = function (value) {
        if (!(minInt <= value && value <= maxInt)) {
            throw new Error('value must range between minInt:-2147483648 and maxInt:2147483647');
        }
        this.ensureCapacity(5);

        value = encodeZigzagInt(value);

        if (value >>> 7 === 0) {
            this.writeByte(value);
            return;
        }

        if (value >>> 14 === 0) {
            this.writeByte((value & 0x7F) | 0x80);
            this.writeByte((value >>> 7));
            return;
        }

        if (value >>> 21 === 0) {
            this.writeByte((value & 0x7F) | 0x80);
            this.writeByte((value >>> 7 | 0x80));
            this.writeByte(value >>> 14);
            return;
        }

        if (value >>> 28 === 0) {
            this.writeByte((value & 0x7F) | 0x80);
            this.writeByte((value >>> 7 | 0x80));
            this.writeByte((value >>> 14 | 0x80));
            this.writeByte(value >>> 21);
            return;
        }

        this.writeByte((value & 0x7F) | 0x80);
        this.writeByte((value >>> 7 | 0x80));
        this.writeByte((value >>> 14 | 0x80));
        this.writeByte((value >>> 21 | 0x80));
        this.writeByte(value >>> 28);
    };

    this.readInt = function () {
        let b = this.readByte();
        let value = b & 0x7F;
        if ((b & 0x80) !== 0) {
            b = this.readByte();
            value |= (b & 0x7F) << 7;
            if ((b & 0x80) !== 0) {
                b = this.readByte();
                value |= (b & 0x7F) << 14;
                if ((b & 0x80) !== 0) {
                    b = this.readByte();
                    value |= (b & 0x7F) << 21;
                    if ((b & 0x80) !== 0) {
                        b = this.readByte();
                        value |= (b & 0x7F) << 28;
                    }
                }
            }
        }

        return decodeZigzagInt(value);
    };

    this.writeLong = function (value) {
        if (value === null || value === undefined) {
            throw new Error('value must not be null');
        }
        this.ensureCapacity(9);

        writeInt64(this, value);
    };

    this.readLong = function () {
        const buffer = new ArrayBuffer(9);
        const bufferView = new DataView(buffer, 0, buffer.byteLength);

        let count = 0;
        let b = this.readByte();
        bufferView.setUint8(count++, b);
        if ((b & 0x80) !== 0) {
            b = this.readByte();
            bufferView.setUint8(count++, b);
            if ((b & 0x80) !== 0) {
                b = this.readByte();
                bufferView.setUint8(count++, b);
                if ((b & 0x80) !== 0) {
                    b = this.readByte();
                    bufferView.setUint8(count++, b);
                    if ((b & 0x80) !== 0) {
                        b = this.readByte();
                        bufferView.setUint8(count++, b);
                        if ((b & 0x80) !== 0) {
                            b = this.readByte();
                            bufferView.setUint8(count++, b);
                            if ((b & 0x80) !== 0) {
                                b = this.readByte();
                                bufferView.setUint8(count++, b);
                                if ((b & 0x80) !== 0) {
                                    b = this.readByte();
                                    bufferView.setUint8(count++, b);
                                    if ((b & 0x80) !== 0) {
                                        b = this.readByte();
                                        bufferView.setUint8(count++, b);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return readInt64(new Uint8Array(buffer.slice(0, count))).toString();
    };

    this.writeFloat = function (value) {
        if (value === null || value === undefined) {
            throw new Error('value must not be null');
        }
        this.ensureCapacity(4);
        this.bufferView.setFloat32(this.writeOffset, value);
        this.writeOffset += 4;
    };

    this.readFloat = function () {
        const value = this.bufferView.getFloat32(this.readOffset);
        this.readOffset += 4;
        return value;
    };

    this.writeDouble = function (value) {
        if (value === null || value === undefined) {
            throw new Error('value must not be null');
        }
        this.ensureCapacity(8);
        this.bufferView.setFloat64(this.writeOffset, value);
        this.writeOffset += 8;
    };

    this.readDouble = function () {
        const value = this.bufferView.getFloat64(this.readOffset);
        this.readOffset += 8;
        return value;
    };

    this.writeChar = function (value) {
        if (value === null || value === undefined || value.length === 0) {
            this.writeInt(0);
            return;
        }
        this.writeString(value.charAt(0));
    };

    this.readChar = function () {
        return this.readString();
    };

    this.writeString = function (value) {
        if (value === null || value === undefined || value.trim().length === 0) {
            this.writeInt(0);
            return;
        }

        const uint8Array = encoder.encode(value);

        this.ensureCapacity(5 + uint8Array.length);

        this.writeInt(uint8Array.length);
        uint8Array.forEach((value) => this.writeByte(value));
    };

    this.readString = function () {
        const length = this.readInt();
        if (length <= 0) {
            return '';
        }
        const uint8Array = new Uint8Array(this.buffer.slice(this.readOffset, this.readOffset + length));
        const value = decoder.decode(uint8Array);
        this.readOffset += length;
        return value;
    };

    this.toBytes = function () {
        const result = new ArrayBuffer(this.writeOffset);
        new Uint8Array(result).set(new Uint8Array(this.buffer.slice(0, this.writeOffset)));
        return result;
    };
};

export default ByteBuffer;
