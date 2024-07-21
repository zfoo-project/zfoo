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
use crate::zfoorust::objectB::ObjectB;

#[derive(Clone)]
pub struct ObjectA {
    pub a: i32,
    pub m: HashMap<i32, String>,
    pub objectB: ObjectB,
    pub innerCompatibleValue: i32,
}

impl IPacket for ObjectA {
    fn protocolId(&self) -> i16 {
        return 102;
    }
}

impl ObjectA {
    pub fn new() -> ObjectA {
        let packet = ObjectA {
            a: 0,
            m: HashMap::new(),
            objectB: ObjectB::new(),
            innerCompatibleValue: 0,
        };
        return packet;
    }
}

pub fn writeObjectA(buffer: &mut dyn IByteBuffer, packet: &dyn Any) {
    let message = packet.downcast_ref::<ObjectA>().unwrap();
    let beforeWriteIndex = buffer.getWriteOffset();
    buffer.writeInt(201);
    buffer.writeInt(message.a);
    if message.m.is_empty() {
        buffer.writeInt(0);
    } else {
        buffer.writeInt(message.m.len() as i32);
        for (key0, value1) in message.m.clone() {
            buffer.writeInt(key0);
            buffer.writeString(value1.clone());
        }
    }
    buffer.writePacket(&message.objectB, 103);
    buffer.writeInt(message.innerCompatibleValue);
    buffer.adjustPadding(201, beforeWriteIndex);
}

pub fn readObjectA(buffer: &mut dyn IByteBuffer) -> Box<dyn Any> {
    let length = buffer.readInt();
    let mut packet = ObjectA::new();
    if length == 0 {
        return Box::new(packet);
    }
    let beforeReadIndex = buffer.getReadOffset();
    let result0 = buffer.readInt();
    packet.a = result0;
    let mut result1: HashMap<i32, String> = HashMap::new();
    let size2 = buffer.readInt();
    if size2 > 0 {
        for index3 in 0 .. size2 {
            let result4 = buffer.readInt();
            let result5 = buffer.readString();
            result1.insert(result4, result5);
        }
    }
    packet.m = result1;
    let result6 = buffer.readPacket(103);
    let result7 = result6.downcast_ref::<ObjectB>().unwrap().clone();
    packet.objectB = result7;
    if buffer.compatibleRead(beforeReadIndex, length) {
        let result8 = buffer.readInt();
        packet.innerCompatibleValue = result8;
    }
    if length > 0 {
        buffer.setReadOffset(beforeReadIndex + length);
    }
    return Box::new(packet);
}