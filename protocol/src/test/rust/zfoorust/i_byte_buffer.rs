#![allow(unused_imports)]
#![allow(dead_code)]
#![allow(non_snake_case)]
#![allow(non_camel_case_types)]
use std::collections::HashMap;
use std::collections::HashSet;
use std::any::Any;

pub trait IPacket {
    fn protocolId(&self) -> i16;
}

#[allow(non_snake_case)]
#[allow(dead_code)]
#[allow(unused_parens)]
pub trait IByteBuffer {
    fn adjustPadding(&mut self, predictionLength: i32, beforeWriteIndex: i32);
    fn compatibleRead(&mut self, beforeReadIndex: i32, length: i32) -> bool;
    fn getBuffer(&self) -> &Vec<i8>;
    fn getWriteOffset(&self) -> i32;
    fn setWriteOffset(&mut self, writeIndex: i32);
    fn getReadOffset(&self) -> i32;
    fn setReadOffset(&mut self, readIndex: i32);
    fn getCapacity(&self) -> i32;
    fn ensureCapacity(&mut self, capacity: i32);
    fn isReadable(&self) -> bool;
    fn writeBytes(&mut self, bytes: &[i8]);
    fn readBytes(&mut self, count: i32) -> &[i8];
    fn writeUBytes(&mut self, bytes: &[u8]);
    fn readUBytes(&mut self, count: i32) -> Vec<u8>;
    fn toBytes(&self) -> &[i8];
    fn writeBool(&mut self, value: bool);
    fn readBool(&mut self) -> bool;
    fn writeByte(&mut self, value: i8);
    fn readByte(&mut self) -> i8;
    fn writeUByte(&mut self, value: u8);
    fn readUByte(&mut self) -> u8;
    fn writeShort(&mut self, value: i16);
    fn readShort(&mut self) -> i16;
    fn writeRawInt(&mut self, value: i32);
    fn readRawInt(&mut self) -> i32;
    fn writeInt(&mut self, intValue: i32);
    fn writeIntCount(&mut self, intValue: i32) -> i32;
    fn readInt(&mut self) -> i32;
    fn writeRawLong(&mut self, value: i64);
    fn readRawLong(&mut self) -> i64;
    fn writeLong(&mut self, longValue: i64);
    fn readLong(&mut self) -> i64;
    fn writeFloat(&mut self, value: f32);
    fn readFloat(&mut self) -> f32;
    fn writeDouble(&mut self, value: f64);
    fn readDouble(&mut self) -> f64;
    fn writeString(&mut self, value: String);
    fn readString(&mut self) -> String;
    fn writePacket(&mut self, packet: &dyn Any, protocolId: i16);
    fn readPacket(&mut self, protocolId: i16) -> Box<dyn Any>;
    fn writeBoolArray(&mut self, array: &Vec<bool>);
    fn readBoolArray(&mut self) -> Vec<bool>;
    fn writeByteArray(&mut self, array: &Vec<i8>);
    fn readByteArray(&mut self) -> Vec<i8>;
    fn writeShortArray(&mut self, array: &Vec<i16>);
    fn readShortArray(&mut self) -> Vec<i16>;
    fn writeIntArray(&mut self, array: &Vec<i32>);
    fn readIntArray(&mut self) -> Vec<i32>;
    fn writeLongArray(&mut self, array: &Vec<i64>);
    fn readLongArray(&mut self) -> Vec<i64>;
    fn writeFloatArray(&mut self, array: &Vec<f32>);
    fn readFloatArray(&mut self) -> Vec<f32>;
    fn writeDoubleArray(&mut self, array: &Vec<f64>);
    fn readDoubleArray(&mut self) -> Vec<f64>;
    fn writeStringArray(&mut self, array: &Vec<String>);
    fn readStringArray(&mut self) -> Vec<String>;
    fn writeBoolSet(&mut self, set: &HashSet<bool>);
    fn readBoolSet(&mut self) -> HashSet<bool>;
    fn writeByteSet(&mut self, set: &HashSet<i8>);
    fn readByteSet(&mut self) -> HashSet<i8>;
    fn writeShortSet(&mut self, set: &HashSet<i16>);
    fn readShortSet(&mut self) -> HashSet<i16>;
    fn writeIntSet(&mut self, set: &HashSet<i32>);
    fn readIntSet(&mut self) -> HashSet<i32>;
    fn writeLongSet(&mut self, set: &HashSet<i64>);
    fn readLongSet(&mut self) -> HashSet<i64>;
    fn writeStringSet(&mut self, set: &HashSet<String>);
    fn readStringSet(&mut self) -> HashSet<String>;
    fn writeIntIntMap(&mut self, map: &HashMap<i32, i32>);
    fn readIntIntMap(&mut self) -> HashMap<i32, i32>;
    fn writeIntLongMap(&mut self, map: &HashMap<i32, i64>);
    fn readIntLongMap(&mut self) -> HashMap<i32, i64>;
    fn writeIntStringMap(&mut self, map: &HashMap<i32, String>);
    fn readIntStringMap(&mut self) -> HashMap<i32, String>;
    fn writeLongIntMap(&mut self, map: &HashMap<i64, i32>);
    fn readLongIntMap(&mut self) -> HashMap<i64, i32>;
    fn writeLongLongMap(&mut self, map: &HashMap<i64, i64>);
    fn readLongLongMap(&mut self) -> HashMap<i64, i64>;
    fn writeLongStringMap(&mut self, map: &HashMap<i64, String>);
    fn readLongStringMap(&mut self) -> HashMap<i64, String>;
    fn writeStringIntMap(&mut self, map: &HashMap<String, i32>);
    fn readStringIntMap(&mut self) -> HashMap<String, i32>;
    fn writeStringLongMap(&mut self, map: &HashMap<String, i64>);
    fn readStringLongMap(&mut self) -> HashMap<String, i64>;
    fn writeStringStringMap(&mut self, map: &HashMap<String, String>);
    fn readStringStringMap(&mut self) -> HashMap<String, String>;
}