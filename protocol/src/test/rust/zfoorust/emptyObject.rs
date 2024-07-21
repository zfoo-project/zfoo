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
pub struct EmptyObject {
    
}

impl IPacket for EmptyObject {
    fn protocolId(&self) -> i16 {
        return 0;
    }
}

impl EmptyObject {
    pub fn new() -> EmptyObject {
        let packet = EmptyObject {
            
        };
        return packet;
    }
}

pub fn writeEmptyObject(buffer: &mut dyn IByteBuffer, packet: &dyn Any) {
    let message = packet.downcast_ref::<EmptyObject>().unwrap();
    buffer.writeInt(-1);
}

pub fn readEmptyObject(buffer: &mut dyn IByteBuffer) -> Box<dyn Any> {
    let length = buffer.readInt();
    let mut packet = EmptyObject::new();
    if length == 0 {
        return Box::new(packet);
    }
    let beforeReadIndex = buffer.getReadOffset();
    
    if length > 0 {
        buffer.setReadOffset(beforeReadIndex + length);
    }
    return Box::new(packet);
}