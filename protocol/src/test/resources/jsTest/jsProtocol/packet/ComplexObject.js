// 复杂的对象
// 包括了各种复杂的结构，数组，List，Set，Map
//
// @author jaysunxiao
// @version 3.0
const ComplexObject = function(a, aa, aaa, aaaa, b, bb, bbb, bbbb, c, cc, ccc, cccc, d, dd, ddd, dddd, e, ee, eee, eeee, f, ff, fff, ffff, g, gg, ggg, gggg, h, hh, hhh, hhhh, jj, jjj, kk, kkk, l, ll, lll, llll, lllll, m, mm, mmm, mmmm, mmmmm, s, ss, sss, ssss, sssss) {
    // byte类型，最简单的整形
    this.a = a; // byte
    // byte的包装类型
    // 优先使用基础类型，包装类型会有装箱拆箱
    this.aa = aa; // java.lang.Byte
    // 数组类型
    this.aaa = aaa; // byte[]
    this.aaaa = aaaa; // java.lang.Byte[]
    this.b = b; // short
    this.bb = bb; // java.lang.Short
    this.bbb = bbb; // short[]
    this.bbbb = bbbb; // java.lang.Short[]
    this.c = c; // int
    this.cc = cc; // java.lang.Integer
    this.ccc = ccc; // int[]
    this.cccc = cccc; // java.lang.Integer[]
    this.d = d; // long
    this.dd = dd; // java.lang.Long
    this.ddd = ddd; // long[]
    this.dddd = dddd; // java.lang.Long[]
    this.e = e; // float
    this.ee = ee; // java.lang.Float
    this.eee = eee; // float[]
    this.eeee = eeee; // java.lang.Float[]
    this.f = f; // double
    this.ff = ff; // java.lang.Double
    this.fff = fff; // double[]
    this.ffff = ffff; // java.lang.Double[]
    this.g = g; // boolean
    this.gg = gg; // java.lang.Boolean
    this.ggg = ggg; // boolean[]
    this.gggg = gggg; // java.lang.Boolean[]
    this.h = h; // char
    this.hh = hh; // java.lang.Character
    this.hhh = hhh; // char[]
    this.hhhh = hhhh; // java.lang.Character[]
    this.jj = jj; // java.lang.String
    this.jjj = jjj; // java.lang.String[]
    this.kk = kk; // com.zfoo.protocol.packet.ObjectA
    this.kkk = kkk; // com.zfoo.protocol.packet.ObjectA[]
    this.l = l; // java.util.List<java.lang.Integer>
    this.ll = ll; // java.util.List<java.util.List<java.util.List<java.lang.Integer>>>
    this.lll = lll; // java.util.List<java.util.List<com.zfoo.protocol.packet.ObjectA>>
    this.llll = llll; // java.util.List<java.lang.String>
    this.lllll = lllll; // java.util.List<java.util.Map<java.lang.Integer, java.lang.String>>
    this.m = m; // java.util.Map<java.lang.Integer, java.lang.String>
    this.mm = mm; // java.util.Map<java.lang.Integer, com.zfoo.protocol.packet.ObjectA>
    this.mmm = mmm; // java.util.Map<com.zfoo.protocol.packet.ObjectA, java.util.List<java.lang.Integer>>
    this.mmmm = mmmm; // java.util.Map<java.util.List<java.util.List<com.zfoo.protocol.packet.ObjectA>>, java.util.List<java.util.List<java.util.List<java.lang.Integer>>>>
    this.mmmmm = mmmmm; // java.util.Map<java.util.List<java.util.Map<java.lang.Integer, java.lang.String>>, java.util.Set<java.util.Map<java.lang.Integer, java.lang.String>>>
    this.s = s; // java.util.Set<java.lang.Integer>
    this.ss = ss; // java.util.Set<java.util.Set<java.util.List<java.lang.Integer>>>
    this.sss = sss; // java.util.Set<java.util.Set<com.zfoo.protocol.packet.ObjectA>>
    this.ssss = ssss; // java.util.Set<java.lang.String>
    this.sssss = sssss; // java.util.Set<java.util.Map<java.lang.Integer, java.lang.String>>
};

ComplexObject.prototype.protocolId = function() {
    return 100;
};

ComplexObject.write = function(byteBuffer, packet) {
    if (byteBuffer.writePacketFlag(packet)) {
        return;
    }
    byteBuffer.writeByte(packet.a);
    byteBuffer.writeByte(packet.aa);
    byteBuffer.writeByteArray(packet.aaa);
    byteBuffer.writeByteArray(packet.aaaa);
    byteBuffer.writeShort(packet.b);
    byteBuffer.writeShort(packet.bb);
    byteBuffer.writeShortArray(packet.bbb);
    byteBuffer.writeShortArray(packet.bbbb);
    byteBuffer.writeInt(packet.c);
    byteBuffer.writeInt(packet.cc);
    byteBuffer.writeIntArray(packet.ccc);
    byteBuffer.writeIntArray(packet.cccc);
    byteBuffer.writeLong(packet.d);
    byteBuffer.writeLong(packet.dd);
    byteBuffer.writeLongArray(packet.ddd);
    byteBuffer.writeLongArray(packet.dddd);
    byteBuffer.writeFloat(packet.e);
    byteBuffer.writeFloat(packet.ee);
    byteBuffer.writeFloatArray(packet.eee);
    byteBuffer.writeFloatArray(packet.eeee);
    byteBuffer.writeDouble(packet.f);
    byteBuffer.writeDouble(packet.ff);
    byteBuffer.writeDoubleArray(packet.fff);
    byteBuffer.writeDoubleArray(packet.ffff);
    byteBuffer.writeBoolean(packet.g);
    byteBuffer.writeBoolean(packet.gg);
    byteBuffer.writeBooleanArray(packet.ggg);
    byteBuffer.writeBooleanArray(packet.gggg);
    byteBuffer.writeChar(packet.h);
    byteBuffer.writeChar(packet.hh);
    byteBuffer.writeCharArray(packet.hhh);
    byteBuffer.writeCharArray(packet.hhhh);
    byteBuffer.writeString(packet.jj);
    byteBuffer.writeStringArray(packet.jjj);
    byteBuffer.writePacket(packet.kk, 102);
    byteBuffer.writePacketArray(packet.kkk, 102);
    byteBuffer.writeIntArray(packet.l);
    if (packet.ll === null) {
        byteBuffer.writeInt(0);
    } else {
        byteBuffer.writeInt(packet.ll.length);
        packet.ll.forEach(element0 => {
            if (element0 === null) {
                byteBuffer.writeInt(0);
            } else {
                byteBuffer.writeInt(element0.length);
                element0.forEach(element1 => {
                    byteBuffer.writeIntArray(element1);
                });
            }
        });
    }
    if (packet.lll === null) {
        byteBuffer.writeInt(0);
    } else {
        byteBuffer.writeInt(packet.lll.length);
        packet.lll.forEach(element2 => {
            byteBuffer.writePacketArray(element2, 102);
        });
    }
    byteBuffer.writeStringArray(packet.llll);
    if (packet.lllll === null) {
        byteBuffer.writeInt(0);
    } else {
        byteBuffer.writeInt(packet.lllll.length);
        packet.lllll.forEach(element3 => {
            byteBuffer.writeIntStringMap(element3);
        });
    }
    byteBuffer.writeIntStringMap(packet.m);
    byteBuffer.writeIntPacketMap(packet.mm, 102);
    if (packet.mmm === null) {
        byteBuffer.writeInt(0);
    } else {
        byteBuffer.writeInt(packet.mmm.size);
        packet.mmm.forEach((value5, key4) => {
            byteBuffer.writePacket(key4, 102);
            byteBuffer.writeIntArray(value5);
        });
    }
    if (packet.mmmm === null) {
        byteBuffer.writeInt(0);
    } else {
        byteBuffer.writeInt(packet.mmmm.size);
        packet.mmmm.forEach((value7, key6) => {
            if (key6 === null) {
                byteBuffer.writeInt(0);
            } else {
                byteBuffer.writeInt(key6.length);
                key6.forEach(element8 => {
                    byteBuffer.writePacketArray(element8, 102);
                });
            }
            if (value7 === null) {
                byteBuffer.writeInt(0);
            } else {
                byteBuffer.writeInt(value7.length);
                value7.forEach(element9 => {
                    if (element9 === null) {
                        byteBuffer.writeInt(0);
                    } else {
                        byteBuffer.writeInt(element9.length);
                        element9.forEach(element10 => {
                            byteBuffer.writeIntArray(element10);
                        });
                    }
                });
            }
        });
    }
    if (packet.mmmmm === null) {
        byteBuffer.writeInt(0);
    } else {
        byteBuffer.writeInt(packet.mmmmm.size);
        packet.mmmmm.forEach((value12, key11) => {
            if (key11 === null) {
                byteBuffer.writeInt(0);
            } else {
                byteBuffer.writeInt(key11.length);
                key11.forEach(element13 => {
                    byteBuffer.writeIntStringMap(element13);
                });
            }
            if (value12 === null) {
                byteBuffer.writeInt(0);
            } else {
                byteBuffer.writeInt(value12.size);
                value12.forEach(element14 => {
                    byteBuffer.writeIntStringMap(element14);
                });
            }
        });
    }
    byteBuffer.writeIntArray(packet.s);
    if (packet.ss === null) {
        byteBuffer.writeInt(0);
    } else {
        byteBuffer.writeInt(packet.ss.size);
        packet.ss.forEach(element15 => {
            if (element15 === null) {
                byteBuffer.writeInt(0);
            } else {
                byteBuffer.writeInt(element15.size);
                element15.forEach(element16 => {
                    byteBuffer.writeIntArray(element16);
                });
            }
        });
    }
    if (packet.sss === null) {
        byteBuffer.writeInt(0);
    } else {
        byteBuffer.writeInt(packet.sss.size);
        packet.sss.forEach(element17 => {
            byteBuffer.writePacketArray(element17, 102);
        });
    }
    byteBuffer.writeStringArray(packet.ssss);
    if (packet.sssss === null) {
        byteBuffer.writeInt(0);
    } else {
        byteBuffer.writeInt(packet.sssss.size);
        packet.sssss.forEach(element18 => {
            byteBuffer.writeIntStringMap(element18);
        });
    }
};

ComplexObject.read = function(byteBuffer) {
    if (!byteBuffer.readBoolean()) {
        return null;
    }
    const packet = new ComplexObject();
    const result19 = byteBuffer.readByte();
    packet.a = result19;
    const result20 = byteBuffer.readByte();
    packet.aa = result20;
    const array21 = byteBuffer.readByteArray();
    packet.aaa = array21;
    const array22 = byteBuffer.readByteArray();
    packet.aaaa = array22;
    const result23 = byteBuffer.readShort();
    packet.b = result23;
    const result24 = byteBuffer.readShort();
    packet.bb = result24;
    const array25 = byteBuffer.readShortArray();
    packet.bbb = array25;
    const array26 = byteBuffer.readShortArray();
    packet.bbbb = array26;
    const result27 = byteBuffer.readInt();
    packet.c = result27;
    const result28 = byteBuffer.readInt();
    packet.cc = result28;
    const array29 = byteBuffer.readIntArray();
    packet.ccc = array29;
    const array30 = byteBuffer.readIntArray();
    packet.cccc = array30;
    const result31 = byteBuffer.readLong();
    packet.d = result31;
    const result32 = byteBuffer.readLong();
    packet.dd = result32;
    const array33 = byteBuffer.readLongArray();
    packet.ddd = array33;
    const array34 = byteBuffer.readLongArray();
    packet.dddd = array34;
    const result35 = byteBuffer.readFloat();
    packet.e = result35;
    const result36 = byteBuffer.readFloat();
    packet.ee = result36;
    const array37 = byteBuffer.readFloatArray();
    packet.eee = array37;
    const array38 = byteBuffer.readFloatArray();
    packet.eeee = array38;
    const result39 = byteBuffer.readDouble();
    packet.f = result39;
    const result40 = byteBuffer.readDouble();
    packet.ff = result40;
    const array41 = byteBuffer.readDoubleArray();
    packet.fff = array41;
    const array42 = byteBuffer.readDoubleArray();
    packet.ffff = array42;
    const result43 = byteBuffer.readBoolean(); 
    packet.g = result43;
    const result44 = byteBuffer.readBoolean(); 
    packet.gg = result44;
    const array45 = byteBuffer.readBooleanArray();
    packet.ggg = array45;
    const array46 = byteBuffer.readBooleanArray();
    packet.gggg = array46;
    const result47 = byteBuffer.readChar();
    packet.h = result47;
    const result48 = byteBuffer.readChar();
    packet.hh = result48;
    const array49 = byteBuffer.readCharArray();
    packet.hhh = array49;
    const array50 = byteBuffer.readCharArray();
    packet.hhhh = array50;
    const result51 = byteBuffer.readString();
    packet.jj = result51;
    const array52 = byteBuffer.readStringArray();
    packet.jjj = array52;
    const result53 = byteBuffer.readPacket(102);
    packet.kk = result53;
    const array54 = byteBuffer.readPacketArray(102);
    packet.kkk = array54;
    const list55 = byteBuffer.readIntArray();
    packet.l = list55;
    const result56 = [];
    const size57 = byteBuffer.readInt();
    if (size57 > 0) {
        for (let index58 = 0; index58 < size57; index58++) {
            const result59 = [];
            const size60 = byteBuffer.readInt();
            if (size60 > 0) {
                for (let index61 = 0; index61 < size60; index61++) {
                    const list62 = byteBuffer.readIntArray();
                    result59.push(list62);
                }
            }
            result56.push(result59);
        }
    }
    packet.ll = result56;
    const result63 = [];
    const size64 = byteBuffer.readInt();
    if (size64 > 0) {
        for (let index65 = 0; index65 < size64; index65++) {
            const list66 = byteBuffer.readPacketArray(102);
            result63.push(list66);
        }
    }
    packet.lll = result63;
    const list67 = byteBuffer.readStringArray();
    packet.llll = list67;
    const result68 = [];
    const size69 = byteBuffer.readInt();
    if (size69 > 0) {
        for (let index70 = 0; index70 < size69; index70++) {
            const map71 = byteBuffer.readIntStringMap();
            result68.push(map71);
        }
    }
    packet.lllll = result68;
    const map72 = byteBuffer.readIntStringMap();
    packet.m = map72;
    const map73 = byteBuffer.readIntPacketMap(102);
    packet.mm = map73;
    const result74 = new Map();
    const size75 = byteBuffer.readInt();
    if (size75 > 0) {
        for (let index76 = 0; index76 < size75; index76++) {
            const result77 = byteBuffer.readPacket(102);
            const list78 = byteBuffer.readIntArray();
            result74.set(result77, list78);
        }
    }
    packet.mmm = result74;
    const result79 = new Map();
    const size80 = byteBuffer.readInt();
    if (size80 > 0) {
        for (let index81 = 0; index81 < size80; index81++) {
            const result82 = [];
            const size83 = byteBuffer.readInt();
            if (size83 > 0) {
                for (let index84 = 0; index84 < size83; index84++) {
                    const list85 = byteBuffer.readPacketArray(102);
                    result82.push(list85);
                }
            }
            const result86 = [];
            const size87 = byteBuffer.readInt();
            if (size87 > 0) {
                for (let index88 = 0; index88 < size87; index88++) {
                    const result89 = [];
                    const size90 = byteBuffer.readInt();
                    if (size90 > 0) {
                        for (let index91 = 0; index91 < size90; index91++) {
                            const list92 = byteBuffer.readIntArray();
                            result89.push(list92);
                        }
                    }
                    result86.push(result89);
                }
            }
            result79.set(result82, result86);
        }
    }
    packet.mmmm = result79;
    const result93 = new Map();
    const size94 = byteBuffer.readInt();
    if (size94 > 0) {
        for (let index95 = 0; index95 < size94; index95++) {
            const result96 = [];
            const size97 = byteBuffer.readInt();
            if (size97 > 0) {
                for (let index98 = 0; index98 < size97; index98++) {
                    const map99 = byteBuffer.readIntStringMap();
                    result96.push(map99);
                }
            }
            const result100 = new Set();
            const size101 = byteBuffer.readInt();
            if (size101 > 0) {
                for (let index102 = 0; index102 < size101; index102++) {
                    const map103 = byteBuffer.readIntStringMap();
                    result100.add(map103);
                }
            }
            result93.set(result96, result100);
        }
    }
    packet.mmmmm = result93;
    const set104 = byteBuffer.readIntArray();
    packet.s = set104;
    const result105 = new Set();
    const size106 = byteBuffer.readInt();
    if (size106 > 0) {
        for (let index107 = 0; index107 < size106; index107++) {
            const result108 = new Set();
            const size109 = byteBuffer.readInt();
            if (size109 > 0) {
                for (let index110 = 0; index110 < size109; index110++) {
                    const list111 = byteBuffer.readIntArray();
                    result108.add(list111);
                }
            }
            result105.add(result108);
        }
    }
    packet.ss = result105;
    const result112 = new Set();
    const size113 = byteBuffer.readInt();
    if (size113 > 0) {
        for (let index114 = 0; index114 < size113; index114++) {
            const set115 = byteBuffer.readPacketArray(102);
            result112.add(set115);
        }
    }
    packet.sss = result112;
    const set116 = byteBuffer.readStringArray();
    packet.ssss = set116;
    const result117 = new Set();
    const size118 = byteBuffer.readInt();
    if (size118 > 0) {
        for (let index119 = 0; index119 < size118; index119++) {
            const map120 = byteBuffer.readIntStringMap();
            result117.add(map120);
        }
    }
    packet.sssss = result117;
    return packet;
};

export default ComplexObject;
