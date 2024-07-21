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


#[derive(Clone)]
pub struct SimpleObject {
    pub c: i32,
    pub g: bool,
}

impl IPacket for SimpleObject {
    fn protocolId(&self) -> i16 {
        return 104;
    }
}

impl SimpleObject {
    pub fn new() -> SimpleObject {
        let packet = SimpleObject {
            c: 0,
            g: false,
        };
        return packet;
    }
}

pub fn writeSimpleObject(buffer: &mut dyn IByteBuffer, packet: &dyn Any) {
    let message = packet.downcast_ref::<SimpleObject>().unwrap();
    buffer.writeInt(-1);
    buffer.writeInt(message.c);
    buffer.writeBool(message.g);
}

pub fn readSimpleObject(buffer: &mut dyn IByteBuffer) -> Box<dyn Any> {
    let length = buffer.readInt();
    let mut packet = SimpleObject::new();
    if length == 0 {
        return Box::new(packet);
    }
    let beforeReadIndex = buffer.getReadOffset();
    let result0 = buffer.readInt();
    packet.c = result0;
    let result1 = buffer.readBool(); 
    packet.g = result1;
    if length > 0 {
        buffer.setReadOffset(beforeReadIndex + length);
    }
    return Box::new(packet);
}