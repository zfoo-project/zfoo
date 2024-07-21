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
    buffer.writeIntStringMap(&message.m);
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
    let map1 = buffer.readIntStringMap();
    packet.m = map1;
    let result2 = buffer.readPacket(103);
    let result3 = result2.downcast_ref::<ObjectB>().unwrap().clone();
    packet.objectB = result3;
    if buffer.compatibleRead(beforeReadIndex, length) {
        let result4 = buffer.readInt();
        packet.innerCompatibleValue = result4;
    }
    if length > 0 {
        buffer.setReadOffset(beforeReadIndex + length);
    }
    return Box::new(packet);
}