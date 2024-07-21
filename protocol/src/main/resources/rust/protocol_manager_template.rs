use std::any::Any;
use std::collections::HashMap;
use crate::${protocol_root_path}::i_byte_buffer::{IByteBuffer, IPacket};
${protocol_imports}

pub fn write(buffer: &mut dyn IByteBuffer, packet: &dyn Any, protocolId: i16) {
    match protocolId {
        ${protocol_write_serialization}
        _ => println!("protocolId:[{}] not found", protocolId)
    }
}

pub fn read(buffer: &mut dyn IByteBuffer) -> Box<dyn Any> {
    let protocolId = buffer.readShort();
    return readByProtocolId(buffer, protocolId);
}

pub fn readByProtocolId(buffer: &mut dyn IByteBuffer, protocolId: i16) -> Box<dyn Any> {
    let packet = match protocolId {
        ${protocol_read_deserialization}
        _ => Box::new(String::from("protocolId not found"))
    };
    return packet;
}