impl IPacket for ${protocol_name} {
    fn protocolId(&self) -> i16 {
        return ${protocol_id};
    }
}

impl ${protocol_name} {
    pub fn new() -> ${protocol_name} {
        let packet = ${protocol_name} {
            ${protocol_field_definition}
        };
        return packet;
    }
}

pub fn write${protocol_name}(buffer: &mut dyn IByteBuffer, packet: &dyn Any) {
    let message = packet.downcast_ref::<${protocol_name}>().unwrap();
    ${protocol_write_serialization}
}

pub fn read${protocol_name}(buffer: &mut dyn IByteBuffer) -> Box<dyn Any> {
    let length = buffer.readInt();
    let mut packet = ${protocol_name}::new();
    if length == 0 {
        return Box::new(packet);
    }
    let beforeReadIndex = buffer.getReadOffset();
    ${protocol_read_deserialization}
    if length > 0 {
        buffer.setReadOffset(beforeReadIndex + length);
    }
    return Box::new(packet);
}
