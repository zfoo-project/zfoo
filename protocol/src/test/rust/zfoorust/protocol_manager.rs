#![allow(unused_imports)]
#![allow(dead_code)]
#![allow(non_snake_case)]
#![allow(non_camel_case_types)]
use std::any::Any;
use crate::zfoorust::i_byte_buffer::IByteBuffer;
use crate::zfoorust::emptyObject::{writeEmptyObject, readEmptyObject};
use crate::zfoorust::normalObject::{writeNormalObject, readNormalObject};
use crate::zfoorust::objectA::{writeObjectA, readObjectA};
use crate::zfoorust::objectB::{writeObjectB, readObjectB};
use crate::zfoorust::simpleObject::{writeSimpleObject, readSimpleObject};


pub fn write(buffer: &mut dyn IByteBuffer, packet: &dyn Any, protocolId: i16) {
    buffer.writeShort(protocolId);
    writeNoProtocolId(buffer, packet, protocolId);
}

pub fn writeNoProtocolId(buffer: &mut dyn IByteBuffer, packet: &dyn Any, protocolId: i16) {
    match protocolId {
        0 => writeEmptyObject(buffer, packet),
        101 => writeNormalObject(buffer, packet),
        102 => writeObjectA(buffer, packet),
        103 => writeObjectB(buffer, packet),
        104 => writeSimpleObject(buffer, packet),
        _ => println!("protocolId:[{}] not found", protocolId)
    }
}

pub fn read(buffer: &mut dyn IByteBuffer) -> Box<dyn Any> {
    let protocolId = buffer.readShort();
    return readNoProtocolId(buffer, protocolId);
}

pub fn readNoProtocolId(buffer: &mut dyn IByteBuffer, protocolId: i16) -> Box<dyn Any> {
    let packet = match protocolId {
        0 => readEmptyObject(buffer),
        101 => readNormalObject(buffer),
        102 => readObjectA(buffer),
        103 => readObjectB(buffer),
        104 => readSimpleObject(buffer),
        _ => Box::new(String::from("protocolId not found"))
    };
    return packet;
}