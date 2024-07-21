#![allow(dead_code)]
#![allow(unused_imports)]
#![allow(unused_mut)]
#![allow(unused_variables)]
#![allow(non_snake_case)]
#![allow(non_camel_case_types)]
use std::any::Any;
use std::collections::HashMap;
use std::collections::HashSet;
use crate::zfoorust::i_byte_buffer::{IByteBuffer, IPacket};
use crate::zfoorust::objectA::ObjectA;
// 常规的对象，取所有语言语法的交集，基本上所有语言都支持下面的语法
#[derive(Clone)]
pub struct NormalObject {
    pub a: i8,
    pub aaa: Vec<i8>,
    pub b: i16,
    // 整数类型
    pub c: i32,
    pub d: i64,
    pub e: f32,
    pub f: f64,
    pub g: bool,
    pub jj: String,
    pub kk: ObjectA,
    pub l: Vec<i32>,
    pub ll: Vec<i64>,
    pub lll: Vec<ObjectA>,
    pub llll: Vec<String>,
    pub m: HashMap<i32, String>,
    pub mm: HashMap<i32, ObjectA>,
    pub s: HashSet<i32>,
    pub ssss: HashSet<String>,
    pub outCompatibleValue: i32,
    pub outCompatibleValue2: i32,
}

impl IPacket for NormalObject {
    fn protocolId(&self) -> i16 {
        return 101;
    }
}

impl NormalObject {
    pub fn new() -> NormalObject {
        let packet = NormalObject {
            a: 0,
            aaa: Vec::new(),
            b: 0,
            // 整数类型
            c: 0,
            d: 0,
            e: 0f32,
            f: 0f64,
            g: false,
            jj: String::from(""),
            kk: ObjectA::new(),
            l: Vec::new(),
            ll: Vec::new(),
            lll: Vec::new(),
            llll: Vec::new(),
            m: HashMap::new(),
            mm: HashMap::new(),
            s: HashSet::new(),
            ssss: HashSet::new(),
            outCompatibleValue: 0,
            outCompatibleValue2: 0,
        };
        return packet;
    }
}

pub fn writeNormalObject(buffer: &mut dyn IByteBuffer, packet: &dyn Any) {
    let message = packet.downcast_ref::<NormalObject>().unwrap();
    let beforeWriteIndex = buffer.getWriteOffset();
    buffer.writeInt(857);
    buffer.writeByte(message.a);
    buffer.writeByteArray(&message.aaa);
    buffer.writeShort(message.b);
    buffer.writeInt(message.c);
    buffer.writeLong(message.d);
    buffer.writeFloat(message.e);
    buffer.writeDouble(message.f);
    buffer.writeBool(message.g);
    buffer.writeString(message.jj.clone());
    buffer.writePacket(&message.kk, 102);
    buffer.writeIntArray(&message.l);
    buffer.writeLongArray(&message.ll);
    if message.lll.is_empty() {
        buffer.writeInt(0);
    } else {
        buffer.writeInt(message.lll.len() as i32);
        for element0 in message.lll.clone() {
            buffer.writePacket(&element0, 102);
        }
    }
    buffer.writeStringArray(&message.llll);
    buffer.writeIntStringMap(&message.m);
    if message.mm.is_empty() {
        buffer.writeInt(0);
    } else {
        buffer.writeInt(message.mm.len() as i32);
        for (key1, value2) in message.mm.clone() {
            buffer.writeInt(key1);
            buffer.writePacket(&value2, 102);
        }
    }
    buffer.writeIntSet(&message.s);
    buffer.writeStringSet(&message.ssss);
    buffer.writeInt(message.outCompatibleValue);
    buffer.writeInt(message.outCompatibleValue2);
    buffer.adjustPadding(857, beforeWriteIndex);
}

pub fn readNormalObject(buffer: &mut dyn IByteBuffer) -> Box<dyn Any> {
    let length = buffer.readInt();
    let mut packet = NormalObject::new();
    if length == 0 {
        return Box::new(packet);
    }
    let beforeReadIndex = buffer.getReadOffset();
    let result0 = buffer.readByte();
    packet.a = result0;
    let array1 = buffer.readByteArray();
    packet.aaa = array1;
    let result2 = buffer.readShort();
    packet.b = result2;
    let result3 = buffer.readInt();
    packet.c = result3;
    let result4 = buffer.readLong();
    packet.d = result4;
    let result5 = buffer.readFloat();
    packet.e = result5;
    let result6 = buffer.readDouble();
    packet.f = result6;
    let result7 = buffer.readBool(); 
    packet.g = result7;
    let result8 = buffer.readString();
    packet.jj = result8;
    let result9 = buffer.readPacket(102);
    let result10 = result9.downcast_ref::<ObjectA>().unwrap().clone();
    packet.kk = result10;
    let list11 = buffer.readIntArray();
    packet.l = list11;
    let list12 = buffer.readLongArray();
    packet.ll = list12;
    let mut result13: Vec<ObjectA> = Vec::new();
    let size14 = buffer.readInt();
    if size14 > 0 {
        for index15 in 0..size14 {
            let result16 = buffer.readPacket(102);
            let result17 = result16.downcast_ref::<ObjectA>().unwrap().clone();
            result13.push(result17);
        }
    }
    packet.lll = result13;
    let list18 = buffer.readStringArray();
    packet.llll = list18;
    let map19 = buffer.readIntStringMap();
    packet.m = map19;
    let mut result20: HashMap<i32, ObjectA> = HashMap::new();
    let size21 = buffer.readInt();
    if size21 > 0 {
        for index22 in 0..size21 {
            let result23 = buffer.readInt();
            let result24 = buffer.readPacket(102);
            let result25 = result24.downcast_ref::<ObjectA>().unwrap().clone();
            result20.insert(result23, result25);
        }
    }
    packet.mm = result20;
    let set26 = buffer.readIntSet();
    packet.s = set26;
    let set27 = buffer.readStringSet();
    packet.ssss = set27;
    if buffer.compatibleRead(beforeReadIndex, length) {
        let result28 = buffer.readInt();
        packet.outCompatibleValue = result28;
    }
    if buffer.compatibleRead(beforeReadIndex, length) {
        let result29 = buffer.readInt();
        packet.outCompatibleValue2 = result29;
    }
    if length > 0 {
        buffer.setReadOffset(beforeReadIndex + length);
    }
    return Box::new(packet);
}