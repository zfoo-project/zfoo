import IByteBuffer from "../IByteBuffer";
import ProtocolManager from "../ProtocolManager";

import {writeInt64, readInt64} from "./Longbits";

const empty_str = '';
const initSize = 128;
const maxSize = 655537;

const maxShort = 32767;
const minShort = -32768;

const maxInt = 2147483647;
const minInt = -2147483648;

// UTF-8编码与解码
// const encoder = new TextEncoder();
// const decoder = new TextDecoder();

// nodejs的测试环境需要用以下方式特殊处理
const util = require('util');
const encoder = new util.TextEncoder('utf-8');
const decoder = new util.TextDecoder('utf-8');

// 在js中long可以支持的最大值
// const maxLong = 9007199254740992;
// const minLong = -9007199254740992;

function copy(original: ArrayBuffer, newLength: number) {
    if (original.byteLength > newLength) {
        throw new Error('newLength is too small');
    }
    const dst = new ArrayBuffer(newLength);
    new Uint8Array(dst).set(new Uint8Array(original));
    return dst;
}

function encodeZigzagInt(n: number) {
    // 有效位左移一位+符号位右移31位
    return (n << 1) ^ (n >> 31);
}

function decodeZigzagInt(n: number) {
    return (n >>> 1) ^ -(n & 1);
}


class ByteBuffer implements IByteBuffer{
    writeOffset: number;
    readOffset: number;
    buffer: ArrayBuffer;
    bufferView: DataView;

    constructor() {
        this.writeOffset = 0;
        this.readOffset = 0;
        this.buffer = new ArrayBuffer(initSize);
        this.bufferView = new DataView(this.buffer, 0, this.buffer.byteLength);
    }

    adjustPadding(predictionLength: number, beforeWriteIndex: number): void {
        const currentWriteIndex = this.writeOffset;
        const predictionCount = this.writeIntCount(predictionLength);
        const length = currentWriteIndex - beforeWriteIndex - predictionCount;
        const lengthCount = this.writeIntCount(length);
        const padding = lengthCount - predictionCount;
        if (padding == 0) {
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

    compatibleRead(beforeReadIndex: number, length: number): boolean {
        return length !== -1 && this.getReadOffset() < length + beforeReadIndex;
    }

    setWriteOffset(writeOffset: number): void {
        if (writeOffset > this.buffer.byteLength) {
            throw new Error('index out of bounds exception:readerIndex:' + this.readOffset +
                ', writerIndex:' + this.writeOffset +
                '(expected:0 <= readerIndex <= writerIndex <= capacity:' + this.buffer.byteLength);
        }
        this.writeOffset = writeOffset;
    }

    getWriteOffset(): number {
        return this.writeOffset;
    }

    setReadOffset(readOffset: number): void {
        if (readOffset > this.writeOffset) {
            throw new Error('index out of bounds exception:readerIndex:' + this.readOffset +
                ', writerIndex:' + this.writeOffset +
                '(expected:0 <= readerIndex <= writerIndex <= capacity:' + this.buffer.byteLength);
        }
        this.readOffset = readOffset;
    }

    getReadOffset(): number {
        return this.readOffset;
    }

    getCapacity(): number {
        return this.buffer.byteLength - this.writeOffset;
    }

    ensureCapacity(minCapacity: number): void {
        while (minCapacity - this.getCapacity() > 0) {
            const newSize = this.buffer.byteLength * 2;
            if (newSize > maxSize) {
                throw new Error('out of memory error');
            }
            this.buffer = copy(this.buffer, newSize);
            this.bufferView = new DataView(this.buffer, 0, this.buffer.byteLength);
        }
    }

    isReadable(): boolean {
        return this.writeOffset > this.readOffset;
    }

    writeBytes(byteArray: ArrayBuffer): void {
        const length = byteArray.byteLength;
        this.ensureCapacity(length);
        new Uint8Array(this.buffer).set(new Uint8Array(byteArray), this.writeOffset);
        this.writeOffset += length;
    }

    toBytes(): ArrayBuffer {
        const result = new ArrayBuffer(this.writeOffset);
        new Uint8Array(result).set(new Uint8Array(this.buffer.slice(0, this.writeOffset)));
        return result;
    }

    writeBoolean(value: boolean): void {
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

    readBoolean(): boolean {
        const value = this.bufferView.getInt8(this.readOffset);
        this.readOffset++;
        return (value === 1);
    }

    writeByte(value: number): void {
        this.ensureCapacity(1);
        this.bufferView.setInt8(this.writeOffset, value);
        this.writeOffset++;
    }

    readByte(): number {
        const value = this.bufferView.getInt8(this.readOffset);
        this.readOffset++;
        return value;
    }

    writeShort(value: number): void {
        if (!(minShort <= value && value <= maxShort)) {
            throw new Error('value must range between minShort:-32768 and maxShort:32767');
        }
        this.ensureCapacity(2);
        this.bufferView.setInt16(this.writeOffset, value);
        this.writeOffset += 2;
    }

    readShort(): number {
        const value = this.bufferView.getInt16(this.readOffset);
        this.readOffset += 2;
        return value;
    }

    writeRawInt(value: number): void {
        if (!(minInt <= value && value <= maxInt)) {
            throw new Error('value must range between minInt:-2147483648 and maxInt:2147483647');
        }
        this.ensureCapacity(4);
        this.bufferView.setInt32(this.writeOffset, value);
        this.writeOffset += 4;
    }

    readRawInt(): number {
        const value = this.bufferView.getInt32(this.readOffset);
        this.readOffset += 4;
        return value;
    }

    writeInt(value: number): void {
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

    writeIntCount(value: number): number {
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

    readInt(): number {
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

    writeLong(value: number): void {
        if (value === null || value === undefined) {
            throw new Error('value must not be null');
        }
        this.ensureCapacity(9);

        writeInt64(this, value);
    }

    readLong(): number {
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

    writeFloat(value: number): void {
        if (value === null || value === undefined) {
            throw new Error('value must not be null');
        }
        this.ensureCapacity(4);
        this.bufferView.setFloat32(this.writeOffset, value);
        this.writeOffset += 4;
    }

    readFloat(): number {
        const value = this.bufferView.getFloat32(this.readOffset);
        this.readOffset += 4;
        return value;
    }

    writeDouble(value: number): void {
        if (value === null || value === undefined) {
            throw new Error('value must not be null');
        }
        this.ensureCapacity(8);
        this.bufferView.setFloat64(this.writeOffset, value);
        this.writeOffset += 8;
    }

    readDouble(): number {
        const value = this.bufferView.getFloat64(this.readOffset);
        this.readOffset += 8;
        return value;
    }

    writeString(value: string): void {
        if (value === null || value === undefined || value.trim().length === 0) {
            this.writeInt(0);
            return;
        }

        const uint8Array = encoder.encode(value);

        this.ensureCapacity(5 + uint8Array.length);

        this.writeInt(uint8Array.length);
        uint8Array.forEach((value: number) => this.writeByte(value));
    }

    readString(): string {
        const length = this.readInt();
        if (length <= 0) {
            return empty_str;
        }
        const uint8Array = new Uint8Array(this.buffer.slice(this.readOffset, this.readOffset + length));
        const value = decoder.decode(uint8Array);
        this.readOffset += length;
        return value;
    }

    writePacket(packet: any, protocolId: number): void {
        const protocolRegistration = ProtocolManager.getProtocol(protocolId);
        protocolRegistration.write(this, packet);
    }

    readPacket(protocolId: number): any {
        const protocolRegistration = ProtocolManager.getProtocol(protocolId);
        return protocolRegistration.read(this);
    }

    writeBooleanArray(array: Array<boolean> | null) {
        if (array === null) {
            this.writeInt(0);
        } else {
            this.writeInt(array.length);
            array.forEach(element => {
                this.writeBoolean(element);
            });
        }
    }

    readBooleanArray(): Array<boolean> {
        const array: boolean[] = [];
        const length = this.readInt();
        if (length > 0) {
            for (let index = 0; index < length; index++) {
                array.push(this.readBoolean());
            }
        }
        return array;
    }

    writeByteArray(array: Array<number> | null): void {
        if (array === null) {
            this.writeInt(0);
        } else {
            this.writeInt(array.length);
            array.forEach(element => {
                this.writeByte(element);
            });
        }
    }

    readByteArray(): Array<number> {
        const array: number[] = [];
        const length = this.readInt();
        if (length > 0) {
            for (let index = 0; index < length; index++) {
                array.push(this.readByte());
            }
        }
        return array;
    }

    writeShortArray(array: Array<number> | null): void {
        if (array === null) {
            this.writeInt(0);
        } else {
            this.writeInt(array.length);
            array.forEach(element => {
                this.writeShort(element);
            });
        }
    }

    readShortArray(): number[] {
        const array: number[] = [];
        const length = this.readInt();
        if (length > 0) {
            for (let index = 0; index < length; index++) {
                array.push(this.readShort());
            }
        }
        return array;
    }

    writeIntArray(array: Array<number> | null): void {
        if (array === null) {
            this.writeInt(0);
        } else {
            this.writeInt(array.length);
            array.forEach(element => {
                this.writeInt(element);
            });
        }
    }

    readIntArray(): number[] {
        const array: number[] = [];
        const length = this.readInt();
        if (length > 0) {
            for (let index = 0; index < length; index++) {
                array.push(this.readInt());
            }
        }
        return array;
    }

    writeLongArray(array: Array<number> | null): void {
        if (array === null) {
            this.writeInt(0);
        } else {
            this.writeInt(array.length);
            array.forEach(element => {
                this.writeLong(element);
            });
        }
    }

    readLongArray(): number[] {
        const array: number[] = [];
        const length = this.readInt();
        if (length > 0) {
            for (let index = 0; index < length; index++) {
                array.push(this.readLong());
            }
        }
        return array;
    }

    writeFloatArray(array: Array<number> | null): void {
        if (array === null) {
            this.writeInt(0);
        } else {
            this.writeInt(array.length);
            array.forEach(element => {
                this.writeFloat(element);
            });
        }
    }

    readFloatArray(): number[] {
        const array: number[] = [];
        const length = this.readInt();
        if (length > 0) {
            for (let index = 0; index < length; index++) {
                array.push(this.readFloat());
            }
        }
        return array;
    }

    writeDoubleArray(array: Array<number> | null): void {
        if (array === null) {
            this.writeInt(0);
        } else {
            this.writeInt(array.length);
            array.forEach(element => {
                this.writeDouble(element);
            });
        }
    }

    readDoubleArray(): number[] {
        const array: number[] = [];
        const length = this.readInt();
        if (length > 0) {
            for (let index = 0; index < length; index++) {
                array.push(this.readDouble());
            }
        }
        return array;
    }

    writeStringArray(array: Array<string> | null): void {
        if (array === null) {
            this.writeInt(0);
        } else {
            this.writeInt(array.length);
            array.forEach(element => {
                this.writeString(element);
            });
        }
    }

    readStringArray(): string[] {
        const array: string[] = [];
        const length = this.readInt();
        if (length > 0) {
            for (let index = 0; index < length; index++) {
                array.push(this.readString());
            }
        }
        return array;
    }

    writePacketArray(array: Array<any> | null, protocolId: number): void {
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

    readPacketArray(protocolId: number): any[] {
        const array: any[] = [];
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
    writeBooleanList(list: Array<boolean> | null): void {
        this.writeBooleanArray(list);
    }

    readBooleanList(): boolean[] {
        return this.readBooleanArray();
    }

    writeByteList(list: Array<number> | null): void {
        this.writeByteArray(list);
    }

    readByteList(): number[] {
        return this.readByteArray();
    }

    writeShortList(list: Array<number> | null): void {
        this.writeShortArray(list);
    }

    readShortList(): number[] {
        return this.readShortArray();
    }

    writeIntList(list: Array<number> | null): void {
        this.writeIntArray(list);
    }

    readIntList(): number[] {
        return this.readIntArray();
    }

    writeLongList(list: Array<number> | null): void {
        this.writeLongArray(list);
    }

    readLongList(): number[] {
        return this.readLongArray();
    }

    writeFloatList(list: Array<number> | null): void {
        this.writeFloatArray(list);
    }

    readFloatList(): number[] {
        return this.readFloatArray();
    }

    writeDoubleList(list: Array<number> | null): void {
        this.writeDoubleArray(list);
    }

    readDoubleList(): number[] {
        return this.readDoubleArray();
    }

    writeStringList(list: Array<string> | null): void {
        this.writeStringArray(list);
    }

    readStringList(): string[] {
        return this.readStringArray();
    }

    writePacketList(list: Array<any> | null, protocolId: number): void {
        this.writePacketArray(list, protocolId);
    }

    readPacketList(protocolId: number): any[] {
        return this.readPacketArray(protocolId);
    }

    // ---------------------------------------------set-------------------------------------------
    writeBooleanSet(set: Set<boolean> | null): void {
        if (set === null) {
            this.writeInt(0);
        } else {
            this.writeInt(set.size);
            set.forEach(element => {
                this.writeBoolean(element);
            });
        }
    }

    readBooleanSet(): Set<boolean> {
        return new Set(this.readBooleanArray());
    }

    writeByteSet(set: Set<number> | null): void {
        if (set === null) {
            this.writeInt(0);
        } else {
            this.writeInt(set.size);
            set.forEach(element => {
                this.writeByte(element);
            });
        }
    }

    readByteSet(): Set<number> {
        return new Set(this.readByteArray());
    }

    writeShortSet(set: Set<number> | null): void {
        if (set === null) {
            this.writeInt(0);
        } else {
            this.writeInt(set.size);
            set.forEach(element => {
                this.writeShort(element);
            });
        }
    }

    readShortSet(): Set<number> {
        return new Set(this.readShortArray());
    }

    writeIntSet(set: Set<number> | null): void {
        if (set === null) {
            this.writeInt(0);
        } else {
            this.writeInt(set.size);
            set.forEach(element => {
                this.writeInt(element);
            });
        }
    }

    readIntSet(): Set<number> {
        return new Set(this.readIntArray());
    }

    writeLongSet(set: Set<number> | null): void {
        if (set === null) {
            this.writeInt(0);
        } else {
            this.writeInt(set.size);
            set.forEach(element => {
                this.writeLong(element);
            });
        }
    }

    readLongSet(): Set<number> {
        return new Set(this.readLongArray());
    }

    writeFloatSet(set: Set<number> | null): void {
        if (set === null) {
            this.writeInt(0);
        } else {
            this.writeInt(set.size);
            set.forEach(element => {
                this.writeFloat(element);
            });
        }
    }

    readFloatSet(): Set<number> {
        return new Set(this.readFloatArray());
    }

    writeDoubleSet(set: Set<number> | null): void {
        if (set === null) {
            this.writeInt(0);
        } else {
            this.writeInt(set.size);
            set.forEach(element => {
                this.writeDouble(element);
            });
        }
    }

    readDoubleSet(): Set<number> {
        return new Set(this.readDoubleArray());
    }

    writeStringSet(set: Set<string> | null): void {
        if (set === null) {
            this.writeInt(0);
        } else {
            this.writeInt(set.size);
            set.forEach(element => {
                this.writeString(element);
            });
        }
    }

    readStringSet(): Set<string> {
        return new Set(this.readStringArray());
    }

    writePacketSet(set: Set<any> | null, protocolId: number): void {
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

    readPacketSet(protocolId: number): Set<any> {
        return new Set(this.readPacketArray(protocolId));
    }

    // ---------------------------------------------map-------------------------------------------
    writeIntIntMap(map: Map<number, number> | null): void {
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

    readIntIntMap(): Map<number, number> {
        const map = new Map<number, number>();
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

    writeIntLongMap(map: Map<number, number> | null): void {
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

    readIntLongMap(): Map<number, number> {
        const map = new Map<number, number>();
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

    writeIntStringMap(map: Map<number, string> | null): void {
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

    readIntStringMap(): Map<number, string> {
        const map = new Map<number, string>();
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

    writeIntPacketMap(map: Map<number, any> | null, protocolId: number): void {
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

    readIntPacketMap(protocolId: number): Map<number, any> {
        const map = new Map<number, any>();
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

    writeLongIntMap(map: Map<number, number> | null): void {
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

    readLongIntMap(): Map<number, number> {
        const map = new Map<number, number>();
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

    writeLongLongMap(map: Map<number, number> | null): void {
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

    readLongLongMap(): Map<number, number> {
        const map = new Map<number, number>();
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

    writeLongStringMap(map: Map<number, string> | null): void {
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

    readLongStringMap(): Map<number, string> {
        const map = new Map<number, string>();
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

    writeLongPacketMap(map: Map<number, any> | null, protocolId: number): any {
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

    readLongPacketMap(protocolId: number): Map<number, any> {
        const map = new Map<number, any>();
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

    writeStringIntMap(map: Map<string, number> | null): void {
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

    readStringIntMap(): Map<string, number> {
        const map = new Map<string, number>();
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

    writeStringLongMap(map: Map<string, number> | null): void {
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

    readStringLongMap(): Map<string, number> {
        const map = new Map<string, number>();
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

    writeStringStringMap(map: Map<string, string> | null): void {
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

    readStringStringMap(): Map<string, string> {
        const map = new Map<string, string>();
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

    writeStringPacketMap(map: Map<string, any> | null, protocolId: number): void {
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

    readStringPacketMap(protocolId: number): Map<string, any> {
        const map = new Map<string, any>();
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
