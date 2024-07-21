#![allow(unused_imports)]
#![allow(dead_code)]
#![allow(non_snake_case)]
#![allow(non_camel_case_types)]
use std::any::Any;
use crate::${protocol_root_path}::i_byte_buffer::IByteBuffer;
use crate::${protocol_root_path}::protocol_manager::write;
use crate::${protocol_root_path}::protocol_manager::readByProtocolId;

#[allow(non_snake_case)]
pub struct ByteBuffer {
    buffer: Vec<i8>,
    writeOffset: i32,
    readOffset: i32,
}

#[allow(non_snake_case)]
#[allow(dead_code)]
#[allow(unused_parens)]
impl ByteBuffer {
    pub fn new() -> ByteBuffer {
        let mut buffer = ByteBuffer {
            buffer: Vec::new(),
            writeOffset: 0,
            readOffset: 0,
        };
        buffer.buffer.resize(128, 0);
        return buffer;
    }
}
#[allow(non_snake_case)]
#[allow(dead_code)]
#[allow(unused_parens)]
impl IByteBuffer for ByteBuffer {
    fn adjustPadding(&mut self, predictionLength: i32, beforeWriteIndex: i32) {
        let currentWriteIndex = self.getWriteOffset();
        let predictionCount = self.writeIntCount(predictionLength);
        let length = currentWriteIndex - beforeWriteIndex - predictionCount;
        let lengthCount = self.writeIntCount(length);
        let padding = lengthCount - predictionCount;
        if (padding == 0) {
            self.setWriteOffset(beforeWriteIndex);
            self.writeInt(length);
            self.setWriteOffset(currentWriteIndex);
        } else {
            let mut bytes: Vec<i8> = Vec::with_capacity(length as usize);
            bytes.extend(&self.buffer[(currentWriteIndex - length) as usize..currentWriteIndex as usize]);
            self.setWriteOffset(beforeWriteIndex);
            self.writeInt(length);
            self.writeBytes(bytes.as_slice());
        }
    }

    fn compatibleRead(&mut self, beforeReadIndex: i32, length: i32) -> bool{
        return length != -1 && self.getReadOffset() < length + beforeReadIndex;
    }

    fn getBuffer(&self) -> &Vec<i8> {
        return &self.buffer;
    }

    fn getWriteOffset(&self) -> i32 {
        return self.writeOffset;
    }

    fn setWriteOffset(&mut self, writeIndex: i32) {
        self.writeOffset = writeIndex;
    }

    fn getReadOffset(&self) -> i32 {
        return self.readOffset;
    }

    fn setReadOffset(&mut self, readIndex: i32) {
        self.readOffset = readIndex;
    }

    fn getCapacity(&self) -> i32 {
        return self.buffer.capacity() as i32 - self.writeOffset;
    }

    fn ensureCapacity(&mut self, capacity: i32) {
        while capacity > self.getCapacity() {
            self.buffer.resize(self.buffer.capacity() * 2, 0);
        }
    }

    fn isReadable(&self) -> bool {
        return self.writeOffset > self.readOffset;
    }

    fn writeBytes(&mut self, bytes: &[i8]) {
        let length = bytes.len() as i32;
        self.ensureCapacity(length);
        for byte in bytes {
            self.writeByte(*byte);
        }
    }

    fn readBytes(&mut self, count: i32) -> &[i8] {
        let value = &self.buffer[self.readOffset as usize..(self.readOffset + count) as usize];
        self.readOffset += count;
        return value;
    }

    fn writeUBytes(&mut self, bytes: &[u8]) {
        let length = bytes.len() as i32;
        self.ensureCapacity(length);
        for byte in bytes {
            self.writeUByte(*byte);
        }
    }

    fn readUBytes(&mut self, count: i32) -> Vec<u8> {
        let mut bytes: Vec<u8> = Vec::new();
        bytes.resize(count as usize, 0);
        for i in 0..count {
            bytes[i as usize] = self.readUByte();
        }
        return bytes;
    }

    fn toBytes(&self) -> &[i8] {
        return &self.buffer[0..self.writeOffset as usize];
    }

    fn writeBool(&mut self, value: bool) {
        self.ensureCapacity(1);
        self.buffer[self.writeOffset as usize] = if value { 1 } else { 0 };
        self.writeOffset += 1;
    }

    fn readBool(&mut self) -> bool {
        let value = self.buffer[self.readOffset as usize];
        self.readOffset += 1;
        return value != 0;
    }

    fn writeByte(&mut self, value: i8) {
        self.ensureCapacity(1);
        self.buffer[self.writeOffset as usize] = value;
        self.writeOffset += 1;
    }

    fn readByte(&mut self) -> i8 {
        let value = self.buffer[self.readOffset as usize];
        self.readOffset += 1;
        return value;
    }

    fn writeUByte(&mut self, value: u8) {
        self.ensureCapacity(1);
        self.buffer[self.writeOffset as usize] = value as i8;
        self.writeOffset += 1;
    }

    fn readUByte(&mut self) -> u8 {
        let value = self.buffer[self.readOffset as usize];
        self.readOffset += 1;
        return value as u8;
    }

    fn writeShort(&mut self, value: i16) {
        self.ensureCapacity(2);
        self.buffer[self.writeOffset as usize] = (value >> 8) as i8;
        self.buffer[self.writeOffset as usize + 1] = value as i8;
        self.writeOffset += 2;
    }

    fn readShort(&mut self) -> i16 {
        let value = (self.buffer[self.readOffset as usize] as i16) << 8
            | (self.buffer[self.readOffset as usize + 1] as u8) as i16;
        self.readOffset += 2;
        return value;
    }

    fn writeRawInt(&mut self, value: i32) {
        self.writeByte((value >> 24) as i8);
        self.writeByte((value >> 16) as i8);
        self.writeByte((value >> 8) as i8);
        self.writeByte(value as i8);
    }

    fn readRawInt(&mut self) -> i32 {
        let value = (self.readUByte() as i32) << 24
            | (self.readUByte() as i32) << 16
            | (self.readUByte() as i32) << 8
            | (self.readUByte() as i32);
        return value;
    }

    fn writeInt(&mut self, intValue: i32) {
        let value = ((intValue << 1) ^ (intValue >> 31)) as u32;

        if (value >> 7 == 0) {
            self.writeByte(value as i8);
            return;
        }

        if (value >> 14 == 0) {
            self.writeByte((value | 0x80) as i8);
            self.writeByte((value >> 7) as i8);
            return;
        }

        if (value >> 21 == 0) {
            self.writeByte((value | 0x80) as i8);
            self.writeByte(((value >> 7) | 0x80) as i8);
            self.writeByte((value >> 14) as i8);
            return;
        }

        if (value >> 28 == 0) {
            self.writeByte((value | 0x80) as i8);
            self.writeByte(((value >> 7) | 0x80) as i8);
            self.writeByte(((value >> 14) | 0x80) as i8);
            self.writeByte((value >> 21) as i8);
            return;
        }

        self.writeByte((value | 0x80) as i8);
        self.writeByte(((value >> 7) | 0x80) as i8);
        self.writeByte(((value >> 14) | 0x80) as i8);
        self.writeByte(((value >> 21) | 0x80) as i8);
        self.writeByte((value >> 28) as i8);
    }

    fn writeIntCount(&mut self, intValue: i32) -> i32 {
        let value = ((intValue << 1) ^ (intValue >> 31)) as u32;
        if (value >> 7 == 0) {
            return 1;
        }
        if (value >> 14 == 0) {
            return 2;
        }
        if (value >> 21 == 0) {
            return 3;
        }
        if (value >> 28 == 0) {
            return 4;
        }
        return 5;
    }

    fn readInt(&mut self) -> i32 {
        let mut b = self.readUByte() as u32;
        let mut value = b & 0x7F;
        if ((b & 0x80) != 0) {
            b = self.readUByte() as u32;
            value |= (b & 0x7F) << 7;
            if ((b & 0x80) != 0) {
                b = self.readUByte() as u32;
                value |= (b & 0x7F) << 14;
                if ((b & 0x80) != 0) {
                    b = self.readUByte() as u32;
                    value |= (b & 0x7F) << 21;
                    if ((b & 0x80) != 0) {
                        b = self.readUByte() as u32;
                        value |= (b & 0x7F) << 28;
                    }
                }
            }
        }
        return (value >> 1) as i32 ^ -((value as i32) & 1);
    }

    fn writeRawLong(&mut self, value: i64) {
        self.writeByte((value >> 56) as i8);
        self.writeByte((value >> 48) as i8);
        self.writeByte((value >> 40) as i8);
        self.writeByte((value >> 32) as i8);
        self.writeByte((value >> 24) as i8);
        self.writeByte((value >> 16) as i8);
        self.writeByte((value >> 8) as i8);
        self.writeByte(value as i8);
    }

    fn readRawLong(&mut self) -> i64 {
        let value = (self.readUByte() as i64) << 56
            | (self.readUByte() as i64) << 48
            | (self.readUByte() as i64) << 40
            | (self.readUByte() as i64) << 32
            | (self.readUByte() as i64) << 24
            | (self.readUByte() as i64) << 16
            | (self.readUByte() as i64) << 8
            | (self.readUByte() as i64);
        return value;
    }

    fn writeLong(&mut self, longValue: i64) {
        let value = ((longValue << 1) ^ (longValue >> 63)) as u64;

        if (value >> 7 == 0) {
            self.writeByte(value as i8);
            return;
        }

        if (value >> 14 == 0) {
            self.writeByte(((value & 0x7F) | 0x80) as i8);
            self.writeByte((value >> 7) as i8);
            return;
        }

        if (value >> 21 == 0) {
            self.writeByte((value | 0x80) as i8);
            self.writeByte(((value >> 7) | 0x80) as i8);
            self.writeByte((value >> 14) as i8);
            return;
        }

        if ((value >> 28) == 0) {
            self.writeByte((value | 0x80) as i8);
            self.writeByte(((value >> 7) | 0x80) as i8);
            self.writeByte(((value >> 14) | 0x80) as i8);
            self.writeByte((value >> 21) as i8);
            return;
        }

        if (value >> 35 == 0) {
            self.writeByte((value | 0x80) as i8);
            self.writeByte(((value >> 7) | 0x80) as i8);
            self.writeByte(((value >> 14) | 0x80) as i8);
            self.writeByte(((value >> 21) | 0x80) as i8);
            self.writeByte((value >> 28) as i8);
            return;
        }

        if (value >> 42 == 0) {
            self.writeByte((value | 0x80) as i8);
            self.writeByte(((value >> 7) | 0x80) as i8);
            self.writeByte(((value >> 14) | 0x80) as i8);
            self.writeByte(((value >> 21) | 0x80) as i8);
            self.writeByte(((value >> 28) | 0x80) as i8);
            self.writeByte((value >> 35) as i8);
            return;
        }

        if (value >> 49 == 0) {
            self.writeByte((value | 0x80) as i8);
            self.writeByte(((value >> 7) | 0x80) as i8);
            self.writeByte(((value >> 14) | 0x80) as i8);
            self.writeByte(((value >> 21) | 0x80) as i8);
            self.writeByte(((value >> 28) | 0x80) as i8);
            self.writeByte(((value >> 35) | 0x80) as i8);
            self.writeByte((value >> 42) as i8);
            return;
        }

        if ((value >> 56) == 0) {
            self.writeByte((value | 0x80) as i8);
            self.writeByte(((value >> 7) | 0x80) as i8);
            self.writeByte(((value >> 14) | 0x80) as i8);
            self.writeByte(((value >> 21) | 0x80) as i8);
            self.writeByte(((value >> 28) | 0x80) as i8);
            self.writeByte(((value >> 35) | 0x80) as i8);
            self.writeByte(((value >> 42) | 0x80) as i8);
            self.writeByte((value >> 49) as i8);
            return;
        }

        self.writeByte((value | 0x80) as i8);
        self.writeByte(((value >> 7) | 0x80) as i8);
        self.writeByte(((value >> 14) | 0x80) as i8);
        self.writeByte(((value >> 21) | 0x80) as i8);
        self.writeByte(((value >> 28) | 0x80) as i8);
        self.writeByte(((value >> 35) | 0x80) as i8);
        self.writeByte(((value >> 42) | 0x80) as i8);
        self.writeByte(((value >> 49) | 0x80) as i8);
        self.writeByte((value >> 56) as i8);
    }

    fn readLong(&mut self) -> i64 {
        let mut b = self.readUByte() as u64;
        let mut value = b & 0x7F;
        if ((b & 0x80) != 0) {
            b = self.readUByte() as u64;
            value |= (b & 0x7F) << 7;
            if ((b & 0x80) != 0) {
                b = self.readUByte() as u64;
                value |= (b & 0x7F) << 14;
                if ((b & 0x80) != 0) {
                    b = self.readUByte() as u64;
                    value |= (b & 0x7F) << 21;
                    if ((b & 0x80) != 0) {
                        b = self.readUByte() as u64;
                        value |= (b & 0x7F) << 28;
                        if ((b & 0x80) != 0) {
                            b = self.readUByte() as u64;
                            value |= (b & 0x7F) << 35;
                            if ((b & 0x80) != 0) {
                                b = self.readUByte() as u64;
                                value |= (b & 0x7F) << 42;
                                if ((b & 0x80) != 0) {
                                    b = self.readUByte() as u64;
                                    value |= (b & 0x7F) << 49;
                                    if ((b & 0x80) != 0) {
                                        b = self.readUByte() as u64;
                                        value |= b << 56;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return (value >> 1) as i64 ^ -(value as i64 & 1);
    }

    fn writeFloat(&mut self, value: f32) {
        self.writeRawInt(value.to_bits() as i32);
    }

    fn readFloat(&mut self) -> f32 {
        return f32::from_bits(self.readRawInt() as u32);
    }

    fn writeDouble(&mut self, value: f64) {
        self.writeRawLong(value.to_bits() as i64);
    }

    fn readDouble(&mut self) -> f64 {
        return f64::from_bits(self.readRawLong() as u64);
    }

    fn writeString(&mut self, value: String) {
        if (value == "" || value.is_empty()) {
            self.writeInt(0);
        }
        let bytes = value.as_bytes();
        self.writeInt(bytes.len() as i32);
        self.writeUBytes(bytes);
    }

    fn readString(&mut self) -> String {
        let length = self.readInt();
        if (length <= 0) {
            return String::from("");
        }
        let bytes = self.readUBytes(length);
        return String::from_utf8(bytes).unwrap();
    }

    fn writePacket(&mut self, packet: &dyn Any, protocolId: i16) {
        write(self, packet, protocolId);
    }

    fn readPacket(&mut self, protocolId: i16) -> Box<dyn Any> {
        return readByProtocolId(self, protocolId);
    }
}
