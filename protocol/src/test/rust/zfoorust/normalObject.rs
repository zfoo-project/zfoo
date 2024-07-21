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
    if message.aaa.is_empty() {
        buffer.writeInt(0);
    } else {
        buffer.writeInt(message.aaa.len() as i32);
        for element0 in message.aaa.clone() {
            buffer.writeByte(element0);
        }
    }
    buffer.writeShort(message.b);
    buffer.writeInt(message.c);
    buffer.writeLong(message.d);
    buffer.writeFloat(message.e);
    buffer.writeDouble(message.f);
    buffer.writeBool(message.g);
    buffer.writeString(message.jj.clone());
    buffer.writePacket(&message.kk, 102);
    if message.l.is_empty() {
        buffer.writeInt(0);
    } else {
        buffer.writeInt(message.l.len() as i32);
        for element1 in message.l.clone() {
            buffer.writeInt(element1);
        }
    }
    if message.ll.is_empty() {
        buffer.writeInt(0);
    } else {
        buffer.writeInt(message.ll.len() as i32);
        for element2 in message.ll.clone() {
            buffer.writeLong(element2);
        }
    }
    if message.lll.is_empty() {
        buffer.writeInt(0);
    } else {
        buffer.writeInt(message.lll.len() as i32);
        for element3 in message.lll.clone() {
            buffer.writePacket(&element3, 102);
        }
    }
    if message.llll.is_empty() {
        buffer.writeInt(0);
    } else {
        buffer.writeInt(message.llll.len() as i32);
        for element4 in message.llll.clone() {
            buffer.writeString(element4.clone());
        }
    }
    if message.m.is_empty() {
        buffer.writeInt(0);
    } else {
        buffer.writeInt(message.m.len() as i32);
        for (key5, value6) in message.m.clone() {
            buffer.writeInt(key5);
            buffer.writeString(value6.clone());
        }
    }
    if message.mm.is_empty() {
        buffer.writeInt(0);
    } else {
        buffer.writeInt(message.mm.len() as i32);
        for (key7, value8) in message.mm.clone() {
            buffer.writeInt(key7);
            buffer.writePacket(&value8, 102);
        }
    }
    if message.s.is_empty() {
        buffer.writeInt(0);
    } else {
        buffer.writeInt(message.s.len() as i32);
        for element9 in message.s.clone() {
            buffer.writeInt(element9);
        }
    }
    if message.ssss.is_empty() {
        buffer.writeInt(0);
    } else {
        buffer.writeInt(message.ssss.len() as i32);
        for element10 in message.ssss.clone() {
            buffer.writeString(element10.clone());
        }
    }
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
    let mut result1: Vec<i8> = Vec::new();
    let size3 = buffer.readInt();
    if size3 > 0 {
        for index2 in 0 .. size3 {
            let result4 = buffer.readByte();
            result1.push(result4);
        }
    }
    packet.aaa = result1;
    let result5 = buffer.readShort();
    packet.b = result5;
    let result6 = buffer.readInt();
    packet.c = result6;
    let result7 = buffer.readLong();
    packet.d = result7;
    let result8 = buffer.readFloat();
    packet.e = result8;
    let result9 = buffer.readDouble();
    packet.f = result9;
    let result10 = buffer.readBool(); 
    packet.g = result10;
    let result11 = buffer.readString();
    packet.jj = result11;
    let result12 = buffer.readPacket(102);
    let result13 = result12.downcast_ref::<ObjectA>().unwrap().clone();
    packet.kk = result13;
    let mut result14: Vec<i32> = Vec::new();
    let size15 = buffer.readInt();
    if size15 > 0 {
        for index16 in 0 .. size15 {
            let result17 = buffer.readInt();
            result14.push(result17);
        }
    }
    packet.l = result14;
    let mut result18: Vec<i64> = Vec::new();
    let size19 = buffer.readInt();
    if size19 > 0 {
        for index20 in 0 .. size19 {
            let result21 = buffer.readLong();
            result18.push(result21);
        }
    }
    packet.ll = result18;
    let mut result22: Vec<ObjectA> = Vec::new();
    let size23 = buffer.readInt();
    if size23 > 0 {
        for index24 in 0 .. size23 {
            let result25 = buffer.readPacket(102);
            let result26 = result25.downcast_ref::<ObjectA>().unwrap().clone();
            result22.push(result26);
        }
    }
    packet.lll = result22;
    let mut result27: Vec<String> = Vec::new();
    let size28 = buffer.readInt();
    if size28 > 0 {
        for index29 in 0 .. size28 {
            let result30 = buffer.readString();
            result27.push(result30);
        }
    }
    packet.llll = result27;
    let mut result31: HashMap<i32, String> = HashMap::new();
    let size32 = buffer.readInt();
    if size32 > 0 {
        for index33 in 0 .. size32 {
            let result34 = buffer.readInt();
            let result35 = buffer.readString();
            result31.insert(result34, result35);
        }
    }
    packet.m = result31;
    let mut result36: HashMap<i32, ObjectA> = HashMap::new();
    let size37 = buffer.readInt();
    if size37 > 0 {
        for index38 in 0 .. size37 {
            let result39 = buffer.readInt();
            let result40 = buffer.readPacket(102);
            let result41 = result40.downcast_ref::<ObjectA>().unwrap().clone();
            result36.insert(result39, result41);
        }
    }
    packet.mm = result36;
    let mut result42: HashSet<i32> = HashSet::new();
    let size43 = buffer.readInt();
    if size43 > 0 {
        for index44 in 0 .. size43 {
            let result45 = buffer.readInt();
            result42.insert(result45);
        }
    }
    packet.s = result42;
    let mut result46: HashSet<String> = HashSet::new();
    let size47 = buffer.readInt();
    if size47 > 0 {
        for index48 in 0 .. size47 {
            let result49 = buffer.readString();
            result46.insert(result49);
        }
    }
    packet.ssss = result46;
    if buffer.compatibleRead(beforeReadIndex, length) {
        let result50 = buffer.readInt();
        packet.outCompatibleValue = result50;
    }
    if buffer.compatibleRead(beforeReadIndex, length) {
        let result51 = buffer.readInt();
        packet.outCompatibleValue2 = result51;
    }
    if length > 0 {
        buffer.setReadOffset(beforeReadIndex + length);
    }
    return Box::new(packet);
}