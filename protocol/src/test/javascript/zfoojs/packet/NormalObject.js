// @author godotg
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

NormalObject.write = function(buffer, packet) {
    if (buffer.writePacketFlag(packet)) {
        return;
    }
    buffer.writeByte(packet.a);
    buffer.writeByteArray(packet.aaa);
    buffer.writeShort(packet.b);
    buffer.writeInt(packet.c);
    buffer.writeLong(packet.d);
    buffer.writeFloat(packet.e);
    buffer.writeDouble(packet.f);
    buffer.writeBoolean(packet.g);
    buffer.writeString(packet.jj);
    buffer.writePacket(packet.kk, 102);
    buffer.writeIntList(packet.l);
    buffer.writeLongList(packet.ll);
    buffer.writePacketList(packet.lll, 102);
    buffer.writeStringList(packet.llll);
    buffer.writeIntStringMap(packet.m);
    buffer.writeIntPacketMap(packet.mm, 102);
    buffer.writeIntSet(packet.s);
    buffer.writeStringSet(packet.ssss);
};

NormalObject.read = function(buffer) {
    if (!buffer.readBoolean()) {
        return null;
    }
    const packet = new NormalObject();
    const result0 = buffer.readByte();
    packet.a = result0;
    const array1 = buffer.readByteArray();
    packet.aaa = array1;
    const result2 = buffer.readShort();
    packet.b = result2;
    const result3 = buffer.readInt();
    packet.c = result3;
    const result4 = buffer.readLong();
    packet.d = result4;
    const result5 = buffer.readFloat();
    packet.e = result5;
    const result6 = buffer.readDouble();
    packet.f = result6;
    const result7 = buffer.readBoolean(); 
    packet.g = result7;
    const result8 = buffer.readString();
    packet.jj = result8;
    const result9 = buffer.readPacket(102);
    packet.kk = result9;
    const list10 = buffer.readIntList();
    packet.l = list10;
    const list11 = buffer.readLongList();
    packet.ll = list11;
    const list12 = buffer.readPacketList(102);
    packet.lll = list12;
    const list13 = buffer.readStringList();
    packet.llll = list13;
    const map14 = buffer.readIntStringMap();
    packet.m = map14;
    const map15 = buffer.readIntPacketMap(102);
    packet.mm = map15;
    const set16 = buffer.readIntSet();
    packet.s = set16;
    const set17 = buffer.readStringSet();
    packet.ssss = set17;
    return packet;
};

export default NormalObject;
