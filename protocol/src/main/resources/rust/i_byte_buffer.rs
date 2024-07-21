#![allow(unused_imports)]
#![allow(dead_code)]
#![allow(non_snake_case)]
#![allow(non_camel_case_types)]
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
}
