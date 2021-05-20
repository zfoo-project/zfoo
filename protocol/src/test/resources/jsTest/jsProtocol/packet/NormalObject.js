import ProtocolManager from '../ProtocolManager.js';
// @author jaysunxiao
// @version 1.0
// @since 2021-02-07 17:18
const NormalObject = function (a, aaa, b, bbb, c, ccc, d, ddd, e, eee, f, fff, g, ggg, h, hhh, jj, jjj, kk, kkk, l, llll, m, mm, s, ssss) {
    this.a = a; // byte
    this.aaa = aaa; // byte[]
    this.b = b; // short
    this.bbb = bbb; // short[]
    this.c = c; // int
    this.ccc = ccc; // int[]
    this.d = d; // long
    this.ddd = ddd; // long[]
    this.e = e; // float
    this.eee = eee; // float[]
    this.f = f; // double
    this.fff = fff; // double[]
    this.g = g; // boolean
    this.ggg = ggg; // boolean[]
    this.h = h; // char
    this.hhh = hhh; // char[]
    this.jj = jj; // java.lang.String
    this.jjj = jjj; // java.lang.String[]
    this.kk = kk; // com.zfoo.protocol.packet.ObjectA
    this.kkk = kkk; // com.zfoo.protocol.packet.ObjectA[]
    this.l = l; // java.util.List<java.lang.Integer>
    this.llll = llll; // java.util.List<java.lang.String>
    this.m = m; // java.util.Map<java.lang.Integer, java.lang.String>
    this.mm = mm; // java.util.Map<java.lang.Integer, com.zfoo.protocol.packet.ObjectA>
    this.s = s; // java.util.Set<java.lang.Integer>
    this.ssss = ssss; // java.util.Set<java.lang.String>
};

NormalObject.prototype.protocolId = function () {
    return 1161;
};

NormalObject.writeObject = function (byteBuffer, packet) {
    if (packet === null) {
        byteBuffer.writeBoolean(false);
        return;
    }
    byteBuffer.writeBoolean(true);
    byteBuffer.writeByte(packet.a);
    if (packet.aaa === null) {
        byteBuffer.writeInt(0);
    } else {
        byteBuffer.writeInt(packet.aaa.length);
        packet.aaa.forEach(element0 => {
            byteBuffer.writeByte(element0);
        });
    }
    byteBuffer.writeShort(packet.b);
    if (packet.bbb === null) {
        byteBuffer.writeInt(0);
    } else {
        byteBuffer.writeInt(packet.bbb.length);
        packet.bbb.forEach(element1 => {
            byteBuffer.writeShort(element1);
        });
    }
    byteBuffer.writeInt(packet.c);
    if (packet.ccc === null) {
        byteBuffer.writeInt(0);
    } else {
        byteBuffer.writeInt(packet.ccc.length);
        packet.ccc.forEach(element2 => {
            byteBuffer.writeInt(element2);
        });
    }
    byteBuffer.writeLong(packet.d);
    if (packet.ddd === null) {
        byteBuffer.writeInt(0);
    } else {
        byteBuffer.writeInt(packet.ddd.length);
        packet.ddd.forEach(element3 => {
            byteBuffer.writeLong(element3);
        });
    }
    byteBuffer.writeFloat(packet.e);
    if (packet.eee === null) {
        byteBuffer.writeInt(0);
    } else {
        byteBuffer.writeInt(packet.eee.length);
        packet.eee.forEach(element4 => {
            byteBuffer.writeFloat(element4);
        });
    }
    byteBuffer.writeDouble(packet.f);
    if (packet.fff === null) {
        byteBuffer.writeInt(0);
    } else {
        byteBuffer.writeInt(packet.fff.length);
        packet.fff.forEach(element5 => {
            byteBuffer.writeDouble(element5);
        });
    }
    byteBuffer.writeBoolean(packet.g);
    if (packet.ggg === null) {
        byteBuffer.writeInt(0);
    } else {
        byteBuffer.writeInt(packet.ggg.length);
        packet.ggg.forEach(element6 => {
            byteBuffer.writeBoolean(element6);
        });
    }
    byteBuffer.writeChar(packet.h);
    if (packet.hhh === null) {
        byteBuffer.writeInt(0);
    } else {
        byteBuffer.writeInt(packet.hhh.length);
        packet.hhh.forEach(element7 => {
            byteBuffer.writeChar(element7);
        });
    }
    byteBuffer.writeString(packet.jj);
    if (packet.jjj === null) {
        byteBuffer.writeInt(0);
    } else {
        byteBuffer.writeInt(packet.jjj.length);
        packet.jjj.forEach(element8 => {
            byteBuffer.writeString(element8);
        });
    }
    ProtocolManager.getProtocol(1116).writeObject(byteBuffer, packet.kk);
    if (packet.kkk === null) {
        byteBuffer.writeInt(0);
    } else {
        byteBuffer.writeInt(packet.kkk.length);
        packet.kkk.forEach(element9 => {
            ProtocolManager.getProtocol(1116).writeObject(byteBuffer, element9);
        });
    }
    if (packet.l === null) {
        byteBuffer.writeInt(0);
    } else {
        byteBuffer.writeInt(packet.l.length);
        packet.l.forEach(element10 => {
            byteBuffer.writeInt(element10);
        });
    }
    if (packet.llll === null) {
        byteBuffer.writeInt(0);
    } else {
        byteBuffer.writeInt(packet.llll.length);
        packet.llll.forEach(element11 => {
            byteBuffer.writeString(element11);
        });
    }
    if (packet.m === null) {
        byteBuffer.writeInt(0);
    } else {
        byteBuffer.writeInt(packet.m.size);
        packet.m.forEach((value13, key12) => {
            byteBuffer.writeInt(key12);
            byteBuffer.writeString(value13);
        });
    }
    if (packet.mm === null) {
        byteBuffer.writeInt(0);
    } else {
        byteBuffer.writeInt(packet.mm.size);
        packet.mm.forEach((value15, key14) => {
            byteBuffer.writeInt(key14);
            ProtocolManager.getProtocol(1116).writeObject(byteBuffer, value15);
        });
    }
    if (packet.s === null) {
        byteBuffer.writeInt(0);
    } else {
        byteBuffer.writeInt(packet.s.size);
        packet.s.forEach(element16 => {
            byteBuffer.writeInt(element16);
        });
    }
    if (packet.ssss === null) {
        byteBuffer.writeInt(0);
    } else {
        byteBuffer.writeInt(packet.ssss.size);
        packet.ssss.forEach(element17 => {
            byteBuffer.writeString(element17);
        });
    }
};

NormalObject.readObject = function (byteBuffer) {
    if (!byteBuffer.readBoolean()) {
        return null;
    }
    const packet = new NormalObject();
    const result18 = byteBuffer.readByte();
    packet.a = result18;
    const result19 = [];
    const size21 = byteBuffer.readInt();
    if (size21 > 0) {
        for (let index20 = 0; index20 < size21; index20++) {
            const result22 = byteBuffer.readByte();
            result19.push(result22);
        }
    }
    packet.aaa = result19;
    const result23 = byteBuffer.readShort();
    packet.b = result23;
    const result24 = [];
    const size26 = byteBuffer.readInt();
    if (size26 > 0) {
        for (let index25 = 0; index25 < size26; index25++) {
            const result27 = byteBuffer.readShort();
            result24.push(result27);
        }
    }
    packet.bbb = result24;
    const result28 = byteBuffer.readInt();
    packet.c = result28;
    const result29 = [];
    const size31 = byteBuffer.readInt();
    if (size31 > 0) {
        for (let index30 = 0; index30 < size31; index30++) {
            const result32 = byteBuffer.readInt();
            result29.push(result32);
        }
    }
    packet.ccc = result29;
    const result33 = byteBuffer.readLong();
    packet.d = result33;
    const result34 = [];
    const size36 = byteBuffer.readInt();
    if (size36 > 0) {
        for (let index35 = 0; index35 < size36; index35++) {
            const result37 = byteBuffer.readLong();
            result34.push(result37);
        }
    }
    packet.ddd = result34;
    const result38 = byteBuffer.readFloat();
    packet.e = result38;
    const result39 = [];
    const size41 = byteBuffer.readInt();
    if (size41 > 0) {
        for (let index40 = 0; index40 < size41; index40++) {
            const result42 = byteBuffer.readFloat();
            result39.push(result42);
        }
    }
    packet.eee = result39;
    const result43 = byteBuffer.readDouble();
    packet.f = result43;
    const result44 = [];
    const size46 = byteBuffer.readInt();
    if (size46 > 0) {
        for (let index45 = 0; index45 < size46; index45++) {
            const result47 = byteBuffer.readDouble();
            result44.push(result47);
        }
    }
    packet.fff = result44;
    const result48 = byteBuffer.readBoolean();
    packet.g = result48;
    const result49 = [];
    const size51 = byteBuffer.readInt();
    if (size51 > 0) {
        for (let index50 = 0; index50 < size51; index50++) {
            const result52 = byteBuffer.readBoolean();
            result49.push(result52);
        }
    }
    packet.ggg = result49;
    const result53 = byteBuffer.readChar();
    packet.h = result53;
    const result54 = [];
    const size56 = byteBuffer.readInt();
    if (size56 > 0) {
        for (let index55 = 0; index55 < size56; index55++) {
            const result57 = byteBuffer.readChar();
            result54.push(result57);
        }
    }
    packet.hhh = result54;
    const result58 = byteBuffer.readString();
    packet.jj = result58;
    const result59 = [];
    const size61 = byteBuffer.readInt();
    if (size61 > 0) {
        for (let index60 = 0; index60 < size61; index60++) {
            const result62 = byteBuffer.readString();
            result59.push(result62);
        }
    }
    packet.jjj = result59;
    const result63 = ProtocolManager.getProtocol(1116).readObject(byteBuffer);
    packet.kk = result63;
    const result64 = [];
    const size66 = byteBuffer.readInt();
    if (size66 > 0) {
        for (let index65 = 0; index65 < size66; index65++) {
            const result67 = ProtocolManager.getProtocol(1116).readObject(byteBuffer);
            result64.push(result67);
        }
    }
    packet.kkk = result64;
    const result68 = [];
    const size69 = byteBuffer.readInt();
    if (size69 > 0) {
        for (let index70 = 0; index70 < size69; index70++) {
            const result71 = byteBuffer.readInt();
            result68.push(result71);
        }
    }
    packet.l = result68;
    const result72 = [];
    const size73 = byteBuffer.readInt();
    if (size73 > 0) {
        for (let index74 = 0; index74 < size73; index74++) {
            const result75 = byteBuffer.readString();
            result72.push(result75);
        }
    }
    packet.llll = result72;
    const result76 = new Map();
    const size77 = byteBuffer.readInt();
    if (size77 > 0) {
        for (let index78 = 0; index78 < size77; index78++) {
            const result79 = byteBuffer.readInt();
            const result80 = byteBuffer.readString();
            result76.set(result79, result80);
        }
    }
    packet.m = result76;
    const result81 = new Map();
    const size82 = byteBuffer.readInt();
    if (size82 > 0) {
        for (let index83 = 0; index83 < size82; index83++) {
            const result84 = byteBuffer.readInt();
            const result85 = ProtocolManager.getProtocol(1116).readObject(byteBuffer);
            result81.set(result84, result85);
        }
    }
    packet.mm = result81;
    const result86 = new Set();
    const size87 = byteBuffer.readInt();
    if (size87 > 0) {
        for (let index88 = 0; index88 < size87; index88++) {
            const result89 = byteBuffer.readInt();
            result86.add(result89);
        }
    }
    packet.s = result86;
    const result90 = new Set();
    const size91 = byteBuffer.readInt();
    if (size91 > 0) {
        for (let index92 = 0; index92 < size91; index92++) {
            const result93 = byteBuffer.readString();
            result90.add(result93);
        }
    }
    packet.ssss = result90;
    return packet;
};

export default NormalObject;
