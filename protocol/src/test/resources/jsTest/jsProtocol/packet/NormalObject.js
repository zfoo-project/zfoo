// @author jaysunxiao
// @version 3.0
const NormalObject = function(a, aaa, b, c, d, e, f, g, jj, kk, l, ll, lll, llll, m, mm, s, ssss) {
    this.a = a; // byte
    this.aaa = aaa; // byte[]
    this.b = b; // short
    this.c = c; // int
    this.d = d; // long
    this.e = e; // float
    this.f = f; // double
    this.g = g; // boolean
    this.jj = jj; // java.lang.String
    this.kk = kk; // com.zfoo.protocol.packet.ObjectA
    this.l = l; // java.util.List<java.lang.Integer>
    this.ll = ll; // java.util.List<java.lang.Long>
    this.lll = lll; // java.util.List<com.zfoo.protocol.packet.ObjectA>
    this.llll = llll; // java.util.List<java.lang.String>
    this.m = m; // java.util.Map<java.lang.Integer, java.lang.String>
    this.mm = mm; // java.util.Map<java.lang.Integer, com.zfoo.protocol.packet.ObjectA>
    this.s = s; // java.util.Set<java.lang.Integer>
    this.ssss = ssss; // java.util.Set<java.lang.String>
};

NormalObject.prototype.protocolId = function() {
    return 101;
};

NormalObject.write = function(byteBuffer, packet) {
    if (byteBuffer.writePacketFlag(packet)) {
        return;
    }
    byteBuffer.writeByte(packet.a);
    byteBuffer.writeByteArray(packet.aaa);
    byteBuffer.writeShort(packet.b);
    byteBuffer.writeInt(packet.c);
    byteBuffer.writeLong(packet.d);
    byteBuffer.writeFloat(packet.e);
    byteBuffer.writeDouble(packet.f);
    byteBuffer.writeBoolean(packet.g);
    byteBuffer.writeString(packet.jj);
    byteBuffer.writePacket(packet.kk, 102);
    byteBuffer.writeIntArray(packet.l);
    byteBuffer.writeLongArray(packet.ll);
    byteBuffer.writePacketArray(packet.lll, 102);
    byteBuffer.writeStringArray(packet.llll);
    byteBuffer.writeIntStringMap(packet.m);
    byteBuffer.writeIntPacketMap(packet.mm, 102);
    byteBuffer.writeIntArray(packet.s);
    byteBuffer.writeStringArray(packet.ssss);
};

NormalObject.read = function(byteBuffer) {
    if (!byteBuffer.readBoolean()) {
        return null;
    }
    const packet = new NormalObject();
    const result0 = byteBuffer.readByte();
    packet.a = result0;
    const array1 = byteBuffer.readByteArray();
    packet.aaa = array1;
    const result2 = byteBuffer.readShort();
    packet.b = result2;
    const result3 = byteBuffer.readInt();
    packet.c = result3;
    const result4 = byteBuffer.readLong();
    packet.d = result4;
    const result5 = byteBuffer.readFloat();
    packet.e = result5;
    const result6 = byteBuffer.readDouble();
    packet.f = result6;
    const result7 = byteBuffer.readBoolean(); 
    packet.g = result7;
    const result8 = byteBuffer.readString();
    packet.jj = result8;
    const result9 = byteBuffer.readPacket(102);
    packet.kk = result9;
    const list10 = byteBuffer.readIntArray();
    packet.l = list10;
    const list11 = byteBuffer.readLongArray();
    packet.ll = list11;
    const list12 = byteBuffer.readPacketArray(102);
    packet.lll = list12;
    const list13 = byteBuffer.readStringArray();
    packet.llll = list13;
    const map14 = byteBuffer.readIntStringMap();
    packet.m = map14;
    const map15 = byteBuffer.readIntPacketMap(102);
    packet.mm = map15;
    const set16 = byteBuffer.readIntArray();
    packet.s = set16;
    const set17 = byteBuffer.readStringArray();
    packet.ssss = set17;
    return packet;
};

export default NormalObject;
