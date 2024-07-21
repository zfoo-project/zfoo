#![allow(unused_imports)]
#![allow(dead_code)]
#![allow(non_snake_case)]
#![allow(non_camel_case_types)]
use std::any::Any;
use std::collections::HashMap;
use std::collections::HashSet;
use crate::${protocol_root_path}::i_byte_buffer::IByteBuffer;
use crate::${protocol_root_path}::protocol_manager::writeNoProtocolId;
use crate::${protocol_root_path}::protocol_manager::readNoProtocolId;

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

    fn compatibleRead(&mut self, beforeReadIndex: i32, length: i32) -> bool {
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
        writeNoProtocolId(self, packet, protocolId);
    }

    fn readPacket(&mut self, protocolId: i16) -> Box<dyn Any> {
        return readNoProtocolId(self, protocolId);
    }

    fn writeBoolArray(&mut self, array: &Vec<bool>) {
        if array.is_empty() {
            self.writeInt(0);
        } else {
            self.writeInt(array.len() as i32);
            for ele in array {
                self.writeBool(*ele);
            }
        }
    }

    fn readBoolArray(&mut self) -> Vec<bool> {
        let mut array: Vec<bool> = Vec::new();
        let length = self.readInt();
        if length > 0 {
            for _index in 0..length {
                array.push(self.readBool());
            }
        }
        return array;
    }

    fn writeByteArray(&mut self, array: &Vec<i8>) {
        if array.is_empty() {
            self.writeInt(0);
        } else {
            self.writeInt(array.len() as i32);
            for ele in array {
                self.writeByte(*ele);
            }
        }
    }

    fn readByteArray(&mut self) -> Vec<i8> {
        let mut array: Vec<i8> = Vec::new();
        let length = self.readInt();
        if length > 0 {
            for _index in 0..length {
                array.push(self.readByte());
            }
        }
        return array;
    }

    fn writeShortArray(&mut self, array: &Vec<i16>) {
        if array.is_empty() {
            self.writeInt(0);
        } else {
            self.writeInt(array.len() as i32);
            for ele in array {
                self.writeShort(*ele);
            }
        }
    }

    fn readShortArray(&mut self) -> Vec<i16> {
        let mut array: Vec<i16> = Vec::new();
        let length = self.readInt();
        if length > 0 {
            for _index in 0..length {
                array.push(self.readShort());
            }
        }
        return array;
    }

    fn writeIntArray(&mut self, array: &Vec<i32>) {
        if array.is_empty() {
            self.writeInt(0);
        } else {
            self.writeInt(array.len() as i32);
            for ele in array {
                self.writeInt(*ele);
            }
        }
    }

    fn readIntArray(&mut self) -> Vec<i32> {
        let mut array: Vec<i32> = Vec::new();
        let length = self.readInt();
        if length > 0 {
            for _index in 0..length {
                array.push(self.readInt());
            }
        }
        return array;
    }

    fn writeLongArray(&mut self, array: &Vec<i64>) {
        if array.is_empty() {
            self.writeInt(0);
        } else {
            self.writeInt(array.len() as i32);
            for ele in array {
                self.writeLong(*ele);
            }
        }
    }

    fn readLongArray(&mut self) -> Vec<i64> {
        let mut array: Vec<i64> = Vec::new();
        let length = self.readInt();
        if length > 0 {
            for _index in 0..length {
                array.push(self.readLong());
            }
        }
        return array;
    }

    fn writeFloatArray(&mut self, array: &Vec<f32>) {
        if array.is_empty() {
            self.writeInt(0);
        } else {
            self.writeInt(array.len() as i32);
            for ele in array {
                self.writeFloat(*ele);
            }
        }
    }

    fn readFloatArray(&mut self) -> Vec<f32> {
        let mut array: Vec<f32> = Vec::new();
        let length = self.readInt();
        if length > 0 {
            for _index in 0..length {
                array.push(self.readFloat());
            }
        }
        return array;
    }

    fn writeDoubleArray(&mut self, array: &Vec<f64>) {
        if array.is_empty() {
            self.writeInt(0);
        } else {
            self.writeInt(array.len() as i32);
            for ele in array {
                self.writeDouble(*ele);
            }
        }
    }

    fn readDoubleArray(&mut self) -> Vec<f64> {
        let mut array: Vec<f64> = Vec::new();
        let length = self.readInt();
        if length > 0 {
            for _index in 0..length {
                array.push(self.readDouble());
            }
        }
        return array;
    }

    fn writeStringArray(&mut self, array: &Vec<String>) {
        if array.is_empty() {
            self.writeInt(0);
        } else {
            self.writeInt(array.len() as i32);
            for ele in array {
                self.writeString(String::from(ele));
            }
        }
    }

    fn readStringArray(&mut self) -> Vec<String> {
        let mut array: Vec<String> = Vec::new();
        let length = self.readInt();
        if length > 0 {
            for _index in 0..length {
                array.push(self.readString());
            }
        }
        return array;
    }

    fn writeBoolSet(&mut self, set: &HashSet<bool>) {
        if set.is_empty() {
            self.writeInt(0);
        } else {
            self.writeInt(set.len() as i32);
            for ele in set {
                self.writeBool(*ele);
            }
        }
    }

    fn readBoolSet(&mut self) -> HashSet<bool> {
        let mut set: HashSet<bool> = HashSet::new();
        let length = self.readInt();
        if length > 0 {
            for _index in 0..length {
                set.insert(self.readBool());
            }
        }
        return set;
    }

    fn writeByteSet(&mut self, set: &HashSet<i8>) {
        if set.is_empty() {
            self.writeInt(0);
        } else {
            self.writeInt(set.len() as i32);
            for ele in set {
                self.writeByte(*ele);
            }
        }
    }

    fn readByteSet(&mut self) -> HashSet<i8> {
        let mut set: HashSet<i8> = HashSet::new();
        let length = self.readInt();
        if length > 0 {
            for _index in 0..length {
                set.insert(self.readByte());
            }
        }
        return set;
    }

    fn writeShortSet(&mut self, set: &HashSet<i16>) {
        if set.is_empty() {
            self.writeInt(0);
        } else {
            self.writeInt(set.len() as i32);
            for ele in set {
                self.writeShort(*ele);
            }
        }
    }

    fn readShortSet(&mut self) -> HashSet<i16> {
        let mut set: HashSet<i16> = HashSet::new();
        let length = self.readInt();
        if length > 0 {
            for _index in 0..length {
                set.insert(self.readShort());
            }
        }
        return set;
    }

    fn writeIntSet(&mut self, set: &HashSet<i32>) {
        if set.is_empty() {
            self.writeInt(0);
        } else {
            self.writeInt(set.len() as i32);
            for ele in set {
                self.writeInt(*ele);
            }
        }
    }

    fn readIntSet(&mut self) -> HashSet<i32> {
        let mut set: HashSet<i32> = HashSet::new();
        let length = self.readInt();
        if length > 0 {
            for _index in 0..length {
                set.insert(self.readInt());
            }
        }
        return set;
    }

    fn writeLongSet(&mut self, set: &HashSet<i64>) {
        if set.is_empty() {
            self.writeInt(0);
        } else {
            self.writeInt(set.len() as i32);
            for ele in set {
                self.writeLong(*ele);
            }
        }
    }

    fn readLongSet(&mut self) -> HashSet<i64> {
        let mut set: HashSet<i64> = HashSet::new();
        let length = self.readInt();
        if length > 0 {
            for _index in 0..length {
                set.insert(self.readLong());
            }
        }
        return set;
    }

    fn writeStringSet(&mut self, set: &HashSet<String>) {
        if set.is_empty() {
            self.writeInt(0);
        } else {
            self.writeInt(set.len() as i32);
            for ele in set {
                self.writeString(String::from(ele));
            }
        }
    }

    fn readStringSet(&mut self) -> HashSet<String> {
        let mut set: HashSet<String> = HashSet::new();
        let length = self.readInt();
        if length > 0 {
            for _index in 0..length {
                set.insert(self.readString());
            }
        }
        return set;
    }

    fn writeIntIntMap(&mut self, map: &HashMap<i32, i32>) {
        if map.is_empty() {
            self.writeInt(0);
        } else {
            self.writeInt(map.len() as i32);
            for (key, value) in map.clone() {
                self.writeInt(key);
                self.writeInt(value)
            }
        }
    }

    fn readIntIntMap(&mut self) -> HashMap<i32, i32> {
        let mut map: HashMap<i32, i32> = HashMap::new();
        let length = self.readInt();
        if length > 0 {
            for _index in 0..length {
                let key = self.readInt();
                let value = self.readInt();
                map.insert(key, value);
            }
        }
        return map;
    }

    fn writeIntLongMap(&mut self, map: &HashMap<i32, i64>) {
        if map.is_empty() {
            self.writeInt(0);
        } else {
            self.writeInt(map.len() as i32);
            for (key, value) in map.clone() {
                self.writeInt(key);
                self.writeLong(value)
            }
        }
    }

    fn readIntLongMap(&mut self) -> HashMap<i32, i64> {
        let mut map: HashMap<i32, i64> = HashMap::new();
        let length = self.readInt();
        if length > 0 {
            for _index in 0..length {
                let key = self.readInt();
                let value = self.readLong();
                map.insert(key, value);
            }
        }
        return map;
    }

    fn writeIntStringMap(&mut self, map: &HashMap<i32, String>) {
        if map.is_empty() {
            self.writeInt(0);
        } else {
            self.writeInt(map.len() as i32);
            for (key, value) in map.clone() {
                self.writeInt(key);
                self.writeString(value)
            }
        }
    }

    fn readIntStringMap(&mut self) -> HashMap<i32, String> {
        let mut map: HashMap<i32, String> = HashMap::new();
        let length = self.readInt();
        if length > 0 {
            for _index in 0..length {
                let key = self.readInt();
                let value = self.readString();
                map.insert(key, value);
            }
        }
        return map;
    }

    fn writeLongIntMap(&mut self, map: &HashMap<i64, i32>) {
        if map.is_empty() {
            self.writeInt(0);
        } else {
            self.writeInt(map.len() as i32);
            for (key, value) in map.clone() {
                self.writeLong(key);
                self.writeInt(value)
            }
        }
    }

    fn readLongIntMap(&mut self) -> HashMap<i64, i32> {
        let mut map: HashMap<i64, i32> = HashMap::new();
        let length = self.readInt();
        if length > 0 {
            for _index in 0..length {
                let key = self.readLong();
                let value = self.readInt();
                map.insert(key, value);
            }
        }
        return map;
    }

    fn writeLongLongMap(&mut self, map: &HashMap<i64, i64>) {
        if map.is_empty() {
            self.writeInt(0);
        } else {
            self.writeInt(map.len() as i32);
            for (key, value) in map.clone() {
                self.writeLong(key);
                self.writeLong(value)
            }
        }
    }

    fn readLongLongMap(&mut self) -> HashMap<i64, i64> {
        let mut map: HashMap<i64, i64> = HashMap::new();
        let length = self.readInt();
        if length > 0 {
            for _index in 0..length {
                let key = self.readLong();
                let value = self.readLong();
                map.insert(key, value);
            }
        }
        return map;
    }

    fn writeLongStringMap(&mut self, map: &HashMap<i64, String>) {
        if map.is_empty() {
            self.writeInt(0);
        } else {
            self.writeInt(map.len() as i32);
            for (key, value) in map.clone() {
                self.writeLong(key);
                self.writeString(value)
            }
        }
    }

    fn readLongStringMap(&mut self) -> HashMap<i64, String> {
        let mut map: HashMap<i64, String> = HashMap::new();
        let length = self.readInt();
        if length > 0 {
            for _index in 0..length {
                let key = self.readLong();
                let value = self.readString();
                map.insert(key, value);
            }
        }
        return map;
    }

    fn writeStringIntMap(&mut self, map: &HashMap<String, i32>) {
        if map.is_empty() {
            self.writeInt(0);
        } else {
            self.writeInt(map.len() as i32);
            for (key, value) in map.clone() {
                self.writeString(key);
                self.writeInt(value)
            }
        }
    }

    fn readStringIntMap(&mut self) -> HashMap<String, i32> {
        let mut map: HashMap<String, i32> = HashMap::new();
        let length = self.readInt();
        if length > 0 {
            for _index in 0..length {
                let key = self.readString();
                let value = self.readInt();
                map.insert(key, value);
            }
        }
        return map;
    }

    fn writeStringLongMap(&mut self, map: &HashMap<String, i64>) {
        if map.is_empty() {
            self.writeInt(0);
        } else {
            self.writeInt(map.len() as i32);
            for (key, value) in map.clone() {
                self.writeString(key);
                self.writeLong(value)
            }
        }
    }

    fn readStringLongMap(&mut self) -> HashMap<String, i64> {
        let mut map: HashMap<String, i64> = HashMap::new();
        let length = self.readInt();
        if length > 0 {
            for _index in 0..length {
                let key = self.readString();
                let value = self.readLong();
                map.insert(key, value);
            }
        }
        return map;
    }

    fn writeStringStringMap(&mut self, map: &HashMap<String, String>) {
        if map.is_empty() {
            self.writeInt(0);
        } else {
            self.writeInt(map.len() as i32);
            for (key, value) in map.clone() {
                self.writeString(key);
                self.writeString(value)
            }
        }
    }

    fn readStringStringMap(&mut self) -> HashMap<String, String> {
        let mut map: HashMap<String, String> = HashMap::new();
        let length = self.readInt();
        if length > 0 {
            for _index in 0..length {
                let key = self.readString();
                let value = self.readString();
                map.insert(key, value);
            }
        }
        return map;
    }
}