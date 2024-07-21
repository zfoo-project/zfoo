mod zfoorust;

use std::fs::File;
use std::io::Read;
use std::path::Path;
use crate::zfoorust::byte_buffer::{ByteBuffer};
use crate::zfoorust::i_byte_buffer::{IByteBuffer, IPacket};
use crate::zfoorust::protocol_manager::{write, read};
use crate::zfoorust::normalObject::NormalObject;
fn main() {
    byte_buffer_test();

    // let path = Path::new("C:\\github\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-no-compatible.bytes");
    // let path = Path::new("C:\\github\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-out-compatible.bytes");
    // let path = Path::new("C:\\github\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-inner-compatible.bytes");
    // let path = Path::new("C:\\github\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-out-inner-compatible.bytes");
    let path = Path::new("C:\\github\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-out-inner-inner-compatible.bytes");
    let mut file = File::open(path).expect("Failed to open file");
    let mut content = Vec::new();
    file.read_to_end(&mut content).expect("Failed to read file");
    println!("File content: {:?}", content.len());

    let mut byte_buffer = ByteBuffer::new();
    byte_buffer.writeUBytes(&content);
    println!("buffer length:[{}]", byte_buffer.getWriteOffset());

    let message = read(&mut byte_buffer);
    let packet = message.downcast_ref::<NormalObject>().unwrap();

    // read
    let mut new_byte_buffer = ByteBuffer::new();
    write(&mut new_byte_buffer, packet, packet.protocolId());
    println!("new_byte_buffer length:[{}]", new_byte_buffer.getWriteOffset());
    let new_message = read(&mut new_byte_buffer);
    let new_packet = new_message.downcast_ref::<NormalObject>().unwrap();
    println!("new packet g:{}", new_packet.g);
}

fn byte_buffer_test() {
    let mut buffer = ByteBuffer::new();
    buffer.ensureCapacity(200);
    assert_eq!(buffer.getCapacity(), 128 * 2);

    buffer.writeBool(true);
    assert_eq!(buffer.readBool(), true);
    buffer.writeBool(false);
    assert_eq!(buffer.readBool(), false);

    let bytes: [i8; 6] = [1, 2, 3, 4, 5, 6];
    buffer.writeBytes(&bytes);
    let toBytes = buffer.toBytes();
    let expectBytes: [i8; 8] = [1, 0, 1, 2, 3, 4, 5, 6];
    assert_eq!(toBytes, expectBytes);
    let bytes1 = buffer.readBytes(6);

    buffer.writeShort(9999);
    assert_eq!(buffer.readShort(), 9999);
    buffer.writeShort(-9999);
    assert_eq!(buffer.readShort(), -9999);

    buffer.writeInt(-2147483648);
    assert_eq!(buffer.readInt(), -2147483648);
    buffer.writeInt(2147483647);
    assert_eq!(buffer.readInt(), 2147483647);

    buffer.writeLong(-9223372036854775808);
    assert_eq!(buffer.readLong(), -9223372036854775808);
    buffer.writeLong(9223372036854775807);
    assert_eq!(buffer.readLong(), 9223372036854775807);

    buffer.writeFloat(3.1415926);
    assert_eq!(buffer.readFloat(), 3.1415926);
    buffer.writeFloat(-3.1415926);
    assert_eq!(buffer.readFloat(), -3.1415926);
    buffer.writeDouble(3.1415926);
    assert_eq!(buffer.readDouble(), 3.1415926);
    buffer.writeDouble(-3.1415926);
    assert_eq!(buffer.readDouble(), -3.1415926);

    buffer.writeString(String::from("hello"));
    assert_eq!(buffer.readString(), String::from("hello"));
}