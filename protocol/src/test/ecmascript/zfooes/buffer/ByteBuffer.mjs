import {readInt64, writeInt64} from './longbits.mjs';
import ProtocolManager from '../ProtocolManager.mjs';

const empty_str = '';
const initSize = 128;
const maxSize = 655537;

const maxShort = 32767;
const minShort = -32768;

const maxInt = 2147483647;
const minInt = -2147483648;

// UTF-8编码与解码
const encoder = new TextEncoder('utf-8');
const decoder = new TextDecoder('utf-8');

// nodejs的测试环境需要用以下方式特殊处理
// const util = require('util');
// const encoder = new util.TextEncoder('utf-8');
// const decoder = new util.TextDecoder('utf-8');

// 现在所有主流浏览器都支持TextDecoder，只有微信小程序不支持TextDecoder（微信浏览器也支持，微信的小程序和浏览器不是同一个js环境）
// https://developers.weixin.qq.com/community/develop/doc/000ca85023ce78c8484e0d1d256400
// 如果在微信小程序中使用，需要按照上面的链接全局引入TextEncoder相关依赖

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


class ByteBuffer {
    writeOffset = 0;
    readOffset = 0;
    buffer = new ArrayBuffer(initSize);
    bufferView = new DataView(this.buffer, 0, this.buffer.byteLength);

    adjustPadding(predictionLength, beforeWriteIndex) {
        const currentWriteIndex = this.writeOffset;
        const predictionCount = this.writeIntCount(predictionLength);
        const length = currentWriteIndex - beforeWriteIndex - predictionCount;
        const lengthCount = this.writeIntCount(length);
        const padding = lengthCount - predictionCount;
        if (padding === 0) {
            this.setWriteOffset(beforeWriteIndex);
            this.writeInt(length);
            this.setWriteOffset(currentWriteIndex);
        } else {
            const retainedByteBuf = this.buffer.slice(currentWriteIndex - length, currentWriteIndex);
            this.setWriteOffset(beforeWriteIndex);
            this.writeInt(length);
            this.writeBytes(retainedByteBuf);
        }
    }

    compatibleRead(beforeReadIndex, length) {
        return length !== -1 && this.getReadOffset() < length + beforeReadIndex;
    }

    getWriteOffset() {
        return this.writeOffset;
    }

    setWriteOffset(writeOffset) {
        if (writeOffset > this.buffer.byteLength) {
            throw new Error('index out of bounds exception: readerIndex: ' + this.readOffset +
            ', writerIndex: ' + this.writeOffset +
            '(expected: 0 <= readerIndex <= writerIndex <= capacity:' + this.buffer.byteLength);
        }
        this.writeOffset = writeOffset;
    }

    getReadOffset() {
        return this.readOffset;
    }

    setReadOffset(readOffset) {
        if (readOffset > this.writeOffset) {
            throw new Error('index out of bounds exception: readerIndex: ' + this.readOffset +
                ', writerIndex: ' + this.writeOffset +
                '(expected: 0 <= readerIndex <= writerIndex <= capacity:' + this.buffer.byteLength);
        }
        this.readOffset = readOffset;
    }

    getCapacity() {
        return this.buffer.byteLength - this.writeOffset;
    }

    ensureCapacity(minCapacity) {
        while (minCapacity - this.getCapacity() > 0) {
            const newSize = this.buffer.byteLength * 2;
            if (newSize > maxSize) {
                throw new Error('out of memory error');
            }
            this.buffer = copy(this.buffer, newSize);
            this.bufferView = new DataView(this.buffer, 0, this.buffer.byteLength);
        }
    }

    isReadable() {
        return this.writeOffset > this.readOffset;
    }

    writeBoolean(value) {
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
    }

    readBoolean() {
        const value = this.bufferView.getInt8(this.readOffset);
        this.readOffset++;
        return (value === 1);
    }

    writeBytes(byteArray) {
        const length = byteArray.byteLength;
        this.ensureCapacity(length);
        new Uint8Array(this.buffer).set(new Uint8Array(byteArray), this.writeOffset);
        this.writeOffset += length;
    }

    toBytes() {
        const result = new ArrayBuffer(this.writeOffset);
        new Uint8Array(result).set(new Uint8Array(this.buffer.slice(0, this.writeOffset)));
        return result;
    }

    writeByte(value) {
        this.ensureCapacity(1);
        this.bufferView.setInt8(this.writeOffset, value);
        this.writeOffset++;
    }

    readByte() {
        const value = this.bufferView.getInt8(this.readOffset);
        this.readOffset++;
        return value;
    }

    writeShort(value) {
        if (!(minShort <= value && value <= maxShort)) {
            throw new Error('value must range between minShort:-32768 and maxShort:32767');
        }
        this.ensureCapacity(2);
        this.bufferView.setInt16(this.writeOffset, value);
        this.writeOffset += 2;
    }

    readShort() {
        const value = this.bufferView.getInt16(this.readOffset);
        this.readOffset += 2;
        return value;
    }

    writeRawInt(value) {
        if (!(minInt <= value && value <= maxInt)) {
            throw new Error('value must range between minInt:-2147483648 and maxInt:2147483647');
        }
        this.ensureCapacity(4);
        this.bufferView.setInt32(this.writeOffset, value);
        this.writeOffset += 4;
    }

    readRawInt() {
        const value = this.bufferView.getInt32(this.readOffset);
        this.readOffset += 4;
        return value;
    }

    writeInt(value) {
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
    }

    writeIntCount(value) {
        if (!(minInt <= value && value <= maxInt)) {
            throw new Error('value must range between minInt:-2147483648 and maxInt:2147483647');
        }
        value = encodeZigzagInt(value);
        if (value >>> 7 === 0) {
            return 1;
        }
        if (value >>> 14 === 0) {
            return 2;
        }
        if (value >>> 21 === 0) {
            return 3;
        }
        if (value >>> 28 === 0) {
            return 4;
        }
        return 5;
    }

    readInt() {
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
    }

    writeLong(value) {
        if (value === null || value === undefined) {
            throw new Error('value must not be null');
        }
        this.ensureCapacity(9);

        writeInt64(this, value);
    }

    readLong() {
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
        return readInt64(new Uint8Array(buffer.slice(0, count))).toNumber();
    }

    writeFloat(value) {
        if (value === null || value === undefined) {
            throw new Error('value must not be null');
        }
        this.ensureCapacity(4);
        this.bufferView.setFloat32(this.writeOffset, value);
        this.writeOffset += 4;
    }

    readFloat() {
        const value = this.bufferView.getFloat32(this.readOffset);
        this.readOffset += 4;
        return value;
    }

    writeDouble(value) {
        if (value === null || value === undefined) {
            throw new Error('value must not be null');
        }
        this.ensureCapacity(8);
        this.bufferView.setFloat64(this.writeOffset, value);
        this.writeOffset += 8;
    }

    readDouble() {
        const value = this.bufferView.getFloat64(this.readOffset);
        this.readOffset += 8;
        return value;
    }

    writeString(value) {
        if (value === null || value === undefined || value.trim().length === 0) {
            this.writeInt(0);
            return;
        }

        const uint8Array = encoder.encode(value);

        this.ensureCapacity(5 + uint8Array.length);

        this.writeInt(uint8Array.length);
        uint8Array.forEach((value) => this.writeByte(value));
    }

    readString() {
        const length = this.readInt();
        if (length <= 0) {
            return empty_str;
        }
        const uint8Array = new Uint8Array(this.buffer.slice(this.readOffset, this.readOffset + length));
        const value = decoder.decode(uint8Array);
        this.readOffset += length;
        return value;
    }

    writePacketFlag(value) {
        const flag = (value === null) || (value === undefined);
        this.writeBoolean(!flag);
        return flag;
    }

    writePacket(packet, protocolId) {
        const protocolRegistration = ProtocolManager.getProtocol(protocolId);
        protocolRegistration.write(this, packet);
    }

    readPacket(protocolId) {
        const protocolRegistration = ProtocolManager.getProtocol(protocolId);
        return protocolRegistration.read(this);
    }

    writeBooleanArray(array) {
        if (array === null) {
            this.writeInt(0);
        } else {
            this.writeInt(array.length);
            array.forEach(element => {
                this.writeBoolean(element);
            });
        }
    }

    readBooleanArray() {
        const array = [];
        const length = this.readInt();
        if (length > 0) {
            for (let index = 0; index < length; index++) {
                array.push(this.readBoolean());
            }
        }
        return array;
    }

    writeByteArray(array) {
        if (array === null) {
            this.writeInt(0);
        } else {
            this.writeInt(array.length);
            array.forEach(element => {
                this.writeByte(element);
            });
        }
    }

    readByteArray() {
        const array = [];
        const length = this.readInt();
        if (length > 0) {
            for (let index = 0; index < length; index++) {
                array.push(this.readByte());
            }
        }
        return array;
    }

    writeShortArray(array) {
        if (array === null) {
            this.writeInt(0);
        } else {
            this.writeInt(array.length);
            array.forEach(element => {
                this.writeShort(element);
            });
        }
    }

    readShortArray() {
        const array = [];
        const length = this.readInt();
        if (length > 0) {
            for (let index = 0; index < length; index++) {
                array.push(this.readShort());
            }
        }
        return array;
    }

    writeIntArray(array) {
        if (array === null) {
            this.writeInt(0);
        } else {
            this.writeInt(array.length);
            array.forEach(element => {
                this.writeInt(element);
            });
        }
    }

    readIntArray() {
        const array = [];
        const length = this.readInt();
        if (length > 0) {
            for (let index = 0; index < length; index++) {
                array.push(this.readInt());
            }
        }
        return array;
    }

    writeLongArray(array) {
        if (array === null) {
            this.writeInt(0);
        } else {
            this.writeInt(array.length);
            array.forEach(element => {
                this.writeLong(element);
            });
        }
    }

    readLongArray() {
        const array = [];
        const length = this.readInt();
        if (length > 0) {
            for (let index = 0; index < length; index++) {
                array.push(this.readLong());
            }
        }
        return array;
    }

    writeFloatArray(array) {
        if (array === null) {
            this.writeInt(0);
        } else {
            this.writeInt(array.length);
            array.forEach(element => {
                this.writeFloat(element);
            });
        }
    }

    readFloatArray() {
        const array = [];
        const length = this.readInt();
        if (length > 0) {
            for (let index = 0; index < length; index++) {
                array.push(this.readFloat());
            }
        }
        return array;
    }

    writeDoubleArray(array) {
        if (array === null) {
            this.writeInt(0);
        } else {
            this.writeInt(array.length);
            array.forEach(element => {
                this.writeDouble(element);
            });
        }
    }

    readDoubleArray() {
        const array = [];
        const length = this.readInt();
        if (length > 0) {
            for (let index = 0; index < length; index++) {
                array.push(this.readDouble());
            }
        }
        return array;
    }

    writeStringArray(array) {
        if (array === null) {
            this.writeInt(0);
        } else {
            this.writeInt(array.length);
            array.forEach(element => {
                this.writeString(element);
            });
        }
    }

    readStringArray() {
        const array = [];
        const length = this.readInt();
        if (length > 0) {
            for (let index = 0; index < length; index++) {
                array.push(this.readString());
            }
        }
        return array;
    }

    writePacketArray(array, protocolId) {
        if (array === null) {
            this.writeInt(0);
        } else {
            const protocolRegistration = ProtocolManager.getProtocol(protocolId);
            this.writeInt(array.length);
            array.forEach(element => {
                protocolRegistration.write(this, element);
            });
        }
    }

    readPacketArray(protocolId) {
        const array = [];
        const length = this.readInt();
        if (length > 0) {
            const protocolRegistration = ProtocolManager.getProtocol(protocolId);
            for (let index = 0; index < length; index++) {
                array.push(protocolRegistration.read(this));
            }
        }
        return array;
    }

    // ---------------------------------------------list-------------------------------------------
    writeBooleanList(list) {
        this.writeBooleanArray(list);
    }

    readBooleanList() {
        return this.readBooleanArray();
    }

    writeByteList(list) {
        this.writeByteArray(list);
    }

    readByteList() {
        return this.readByteArray();
    }

    writeShortList(list) {
        this.writeShortArray(list);
    }

    readShortList() {
        return this.readShortArray();
    }

    writeIntList(list) {
        this.writeIntArray(list);
    }

    readIntList() {
        return this.readIntArray();
    }

    writeLongList(list) {
        this.writeLongArray(list);
    }

    readLongList() {
        return this.readLongArray();
    }

    writeFloatList(list) {
        this.writeFloatArray(list);
    }

    readFloatList() {
        return this.readFloatArray();
    }

    writeDoubleList(list) {
        this.writeDoubleArray(list);
    }

    readDoubleList() {
        return this.readDoubleArray();
    }

    writeStringList(list) {
        this.writeStringArray(list);
    }

    readStringList() {
        return this.readStringArray();
    }

    writePacketList(list, protocolId) {
        this.writePacketArray(list, protocolId);
    }

    readPacketList(protocolId) {
        return this.readPacketArray(protocolId);
    }

    // ---------------------------------------------set-------------------------------------------
    writeBooleanSet(set) {
        if (set === null) {
            this.writeInt(0);
        } else {
            this.writeInt(set.size);
            set.forEach(element => {
                this.writeBoolean(element);
            });
        }
    }

    readBooleanSet() {
        return new Set(this.readBooleanArray());
    }

    writeByteSet(set) {
        if (set === null) {
            this.writeInt(0);
        } else {
            this.writeInt(set.size);
            set.forEach(element => {
                this.writeByte(element);
            });
        }
    }

    readByteSet() {
        return new Set(this.readByteArray());
    }

    writeShortSet(set) {
        if (set === null) {
            this.writeInt(0);
        } else {
            this.writeInt(set.size);
            set.forEach(element => {
                this.writeShort(element);
            });
        }
    }

    readShortSet() {
        return new Set(this.readShortArray());
    }

    writeIntSet(set) {
        if (set === null) {
            this.writeInt(0);
        } else {
            this.writeInt(set.size);
            set.forEach(element => {
                this.writeInt(element);
            });
        }
    }

    readIntSet() {
        return new Set(this.readIntArray());
    }

    writeLongSet(set) {
        if (set === null) {
            this.writeInt(0);
        } else {
            this.writeInt(set.size);
            set.forEach(element => {
                this.writeLong(element);
            });
        }
    }

    readLongSet() {
        return new Set(this.readLongArray());
    }

    writeFloatSet(set) {
        if (set === null) {
            this.writeInt(0);
        } else {
            this.writeInt(set.size);
            set.forEach(element => {
                this.writeFloat(element);
            });
        }
    }

    readFloatSet() {
        return new Set(this.readFloatArray());
    }

    writeDoubleSet(set) {
        if (set === null) {
            this.writeInt(0);
        } else {
            this.writeInt(set.size);
            set.forEach(element => {
                this.writeDouble(element);
            });
        }
    }

    readDoubleSet() {
        return new Set(this.readDoubleArray());
    }

    writeStringSet(set) {
        if (set === null) {
            this.writeInt(0);
        } else {
            this.writeInt(set.size);
            set.forEach(element => {
                this.writeString(element);
            });
        }
    }

    readStringSet() {
        return new Set(this.readStringArray());
    }

    writePacketSet(set, protocolId) {
        if (set === null) {
            this.writeInt(0);
        } else {
            const protocolRegistration = ProtocolManager.getProtocol(protocolId);
            this.writeInt(set.size);
            set.forEach(element => {
                protocolRegistration.write(this, element);
            });
        }
    }

    readPacketSet(protocolId) {
        return new Set(this.readPacketArray(protocolId));
    }

    // ---------------------------------------------map-------------------------------------------
    writeIntIntMap(map) {
        if (map === null) {
            this.writeInt(0);
        } else {
            this.writeInt(map.size);
            map.forEach((value, key) => {
                this.writeInt(key);
                this.writeInt(value);
            });
        }
    }

    readIntIntMap() {
        const map = new Map();
        const size = this.readInt();
        if (size > 0) {
            for (let index = 0; index < size; index++) {
                const key = this.readInt();
                const value = this.readInt();
                map.set(key, value);
            }
        }
        return map;
    }

    writeIntLongMap(map) {
        if (map === null) {
            this.writeInt(0);
        } else {
            this.writeInt(map.size);
            map.forEach((value, key) => {
                this.writeInt(key);
                this.writeLong(value);
            });
        }
    }

    readIntLongMap() {
        const map = new Map();
        const size = this.readInt();
        if (size > 0) {
            for (let index = 0; index < size; index++) {
                const key = this.readInt();
                const value = this.readLong();
                map.set(key, value);
            }
        }
        return map;
    }

    writeIntStringMap(map) {
        if (map === null) {
            this.writeInt(0);
        } else {
            this.writeInt(map.size);
            map.forEach((value, key) => {
                this.writeInt(key);
                this.writeString(value);
            });
        }
    }

    readIntStringMap() {
        const map = new Map();
        const size = this.readInt();
        if (size > 0) {
            for (let index = 0; index < size; index++) {
                const key = this.readInt();
                const value = this.readString();
                map.set(key, value);
            }
        }
        return map;
    }

    writeIntPacketMap(map, protocolId) {
        if (map === null) {
            this.writeInt(0);
        } else {
            const protocolRegistration = ProtocolManager.getProtocol(protocolId);
            this.writeInt(map.size);
            map.forEach((value, key) => {
                this.writeInt(key);
                protocolRegistration.write(this, value);
            });
        }
    }

    readIntPacketMap(protocolId) {
        const map = new Map();
        const size = this.readInt();
        if (size > 0) {
            const protocolRegistration = ProtocolManager.getProtocol(protocolId);
            for (let index = 0; index < size; index++) {
                const key = this.readInt();
                const value = protocolRegistration.read(this);
                map.set(key, value);
            }
        }
        return map;
    }

    writeLongIntMap(map) {
        if (map === null) {
            this.writeInt(0);
        } else {
            this.writeInt(map.size);
            map.forEach((value, key) => {
                this.writeLong(key);
                this.writeInt(value);
            });
        }
    }

    readLongIntMap() {
        const map = new Map();
        const size = this.readInt();
        if (size > 0) {
            for (let index = 0; index < size; index++) {
                const key = this.readLong();
                const value = this.readInt();
                map.set(key, value);
            }
        }
        return map;
    }

    writeLongLongMap(map) {
        if (map === null) {
            this.writeInt(0);
        } else {
            this.writeInt(map.size);
            map.forEach((value, key) => {
                this.writeLong(key);
                this.writeLong(value);
            });
        }
    }

    readLongLongMap() {
        const map = new Map();
        const size = this.readInt();
        if (size > 0) {
            for (let index = 0; index < size; index++) {
                const key = this.readLong();
                const value = this.readLong();
                map.set(key, value);
            }
        }
        return map;
    }

    writeLongStringMap(map) {
        if (map === null) {
            this.writeInt(0);
        } else {
            this.writeInt(map.size);
            map.forEach((value, key) => {
                this.writeLong(key);
                this.writeString(value);
            });
        }
    }

    readLongStringMap() {
        const map = new Map();
        const size = this.readInt();
        if (size > 0) {
            for (let index = 0; index < size; index++) {
                const key = this.readLong();
                const value = this.readString();
                map.set(key, value);
            }
        }
        return map;
    }

    writeLongPacketMap(map, protocolId) {
        if (map === null) {
            this.writeInt(0);
        } else {
            const protocolRegistration = ProtocolManager.getProtocol(protocolId);
            this.writeInt(map.size);
            map.forEach((value, key) => {
                this.writeLong(key);
                protocolRegistration.write(this, value);
            });
        }
    }

    readLongPacketMap(protocolId) {
        const map = new Map();
        const size = this.readInt();
        if (size > 0) {
            const protocolRegistration = ProtocolManager.getProtocol(protocolId);
            for (let index = 0; index < size; index++) {
                const key = this.readLong();
                const value = protocolRegistration.read(this);
                map.set(key, value);
            }
        }
        return map;
    }

    writeStringIntMap(map) {
        if (map === null) {
            this.writeInt(0);
        } else {
            this.writeInt(map.size);
            map.forEach((value, key) => {
                this.writeString(key);
                this.writeInt(value);
            });
        }
    }

    readStringIntMap() {
        const map = new Map();
        const size = this.readInt();
        if (size > 0) {
            for (let index = 0; index < size; index++) {
                const key = this.readString();
                const value = this.readInt();
                map.set(key, value);
            }
        }
        return map;
    }

    writeStringLongMap(map) {
        if (map === null) {
            this.writeInt(0);
        } else {
            this.writeInt(map.size);
            map.forEach((value, key) => {
                this.writeString(key);
                this.writeLong(value);
            });
        }
    }

    readStringLongMap() {
        const map = new Map();
        const size = this.readInt();
        if (size > 0) {
            for (let index = 0; index < size; index++) {
                const key = this.readString();
                const value = this.readLong();
                map.set(key, value);
            }
        }
        return map;
    }

    writeStringStringMap(map) {
        if (map === null) {
            this.writeInt(0);
        } else {
            this.writeInt(map.size);
            map.forEach((value, key) => {
                this.writeString(key);
                this.writeString(value);
            });
        }
    }

    readStringStringMap() {
        const map = new Map();
        const size = this.readInt();
        if (size > 0) {
            for (let index = 0; index < size; index++) {
                const key = this.readString();
                const value = this.readString();
                map.set(key, value);
            }
        }
        return map;
    }

    writeStringPacketMap(map, protocolId) {
        if (map === null) {
            this.writeInt(0);
        } else {
            const protocolRegistration = ProtocolManager.getProtocol(protocolId);
            this.writeInt(map.size);
            map.forEach((value, key) => {
                this.writeString(key);
                protocolRegistration.write(this, value);
            });
        }
    }

    readStringPacketMap(protocolId) {
        const map = new Map();
        const size = this.readInt();
        if (size > 0) {
            const protocolRegistration = ProtocolManager.getProtocol(protocolId);
            for (let index = 0; index < size; index++) {
                const key = this.readString();
                const value = protocolRegistration.read(this);
                map.set(key, value);
            }
        }
        return map;
    }
}

export default ByteBuffer;
