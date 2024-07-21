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
pub struct ObjectB {
    pub flag: bool,
    pub innerCompatibleValue: i32,
}

impl IPacket for ObjectB {
    fn protocolId(&self) -> i16 {
        return 103;
    }
}

impl ObjectB {
    pub fn new() -> ObjectB {
        let packet = ObjectB {
            flag: false,
            innerCompatibleValue: 0,
        };
        return packet;
    }
}

pub fn writeObjectB(buffer: &mut dyn IByteBuffer, packet: &dyn Any) {
    let message = packet.downcast_ref::<ObjectB>().unwrap();
    let beforeWriteIndex = buffer.getWriteOffset();
    buffer.writeInt(4);
    buffer.writeBool(message.flag);
    buffer.writeInt(message.innerCompatibleValue);
    buffer.adjustPadding(4, beforeWriteIndex);
}

pub fn readObjectB(buffer: &mut dyn IByteBuffer) -> Box<dyn Any> {
    let length = buffer.readInt();
    let mut packet = ObjectB::new();
    if length == 0 {
        return Box::new(packet);
    }
    let beforeReadIndex = buffer.getReadOffset();
    let result0 = buffer.readBool(); 
    packet.flag = result0;
    if buffer.compatibleRead(beforeReadIndex, length) {
        let result1 = buffer.readInt();
        packet.innerCompatibleValue = result1;
    }
    if length > 0 {
        buffer.setReadOffset(beforeReadIndex + length);
    }
    return Box::new(packet);
}