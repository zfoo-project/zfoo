import ProtocolManager from '../ProtocolManager.js';
// 复杂的对象
// 包括了各种复杂的结构，数组，List，Set，Map
//
// @author jaysunxiao
// @version 1.0
// @since 2017 10.14 11:19
const ComplexObject = function (a, aa, aaa, aaaa, b, bb, bbb, bbbb, c, cc, ccc, cccc, d, dd, ddd, dddd, e, ee, eee, eeee, f, ff, fff, ffff, g, gg, ggg, gggg, h, hh, hhh, hhhh, jj, jjj, kk, kkk, l, ll, lll, llll, lllll, m, mm, mmm, mmmm, mmmmm, s, ss, sss, ssss, sssss) {
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

ComplexObject.prototype.protocolId = function () {
    return 1160;
};

ComplexObject.writeObject = function (byteBuffer, packet) {
    if (packet === null) {
        byteBuffer.writeBoolean(false);
        return;
    }
    byteBuffer.writeBoolean(true);
    byteBuffer.writeByte(packet.a);
    byteBuffer.writeByte(packet.aa);
    if (packet.aaa === null) {
        byteBuffer.writeInt(0);
    } else {
        byteBuffer.writeInt(packet.aaa.length);
        packet.aaa.forEach(element0 => {
            byteBuffer.writeByte(element0);
        });
    }
    if (packet.aaaa === null) {
        byteBuffer.writeInt(0);
    } else {
        byteBuffer.writeInt(packet.aaaa.length);
        packet.aaaa.forEach(element1 => {
            byteBuffer.writeByte(element1);
        });
    }
    byteBuffer.writeShort(packet.b);
    byteBuffer.writeShort(packet.bb);
    if (packet.bbb === null) {
        byteBuffer.writeInt(0);
    } else {
        byteBuffer.writeInt(packet.bbb.length);
        packet.bbb.forEach(element2 => {
            byteBuffer.writeShort(element2);
        });
    }
    if (packet.bbbb === null) {
        byteBuffer.writeInt(0);
    } else {
        byteBuffer.writeInt(packet.bbbb.length);
        packet.bbbb.forEach(element3 => {
            byteBuffer.writeShort(element3);
        });
    }
    byteBuffer.writeInt(packet.c);
    byteBuffer.writeInt(packet.cc);
    if (packet.ccc === null) {
        byteBuffer.writeInt(0);
    } else {
        byteBuffer.writeInt(packet.ccc.length);
        packet.ccc.forEach(element4 => {
            byteBuffer.writeInt(element4);
        });
    }
    if (packet.cccc === null) {
        byteBuffer.writeInt(0);
    } else {
        byteBuffer.writeInt(packet.cccc.length);
        packet.cccc.forEach(element5 => {
            byteBuffer.writeInt(element5);
        });
    }
    byteBuffer.writeLong(packet.d);
    byteBuffer.writeLong(packet.dd);
    if (packet.ddd === null) {
        byteBuffer.writeInt(0);
    } else {
        byteBuffer.writeInt(packet.ddd.length);
        packet.ddd.forEach(element6 => {
            byteBuffer.writeLong(element6);
        });
    }
    if (packet.dddd === null) {
        byteBuffer.writeInt(0);
    } else {
        byteBuffer.writeInt(packet.dddd.length);
        packet.dddd.forEach(element7 => {
            byteBuffer.writeLong(element7);
        });
    }
    byteBuffer.writeFloat(packet.e);
    byteBuffer.writeFloat(packet.ee);
    if (packet.eee === null) {
        byteBuffer.writeInt(0);
    } else {
        byteBuffer.writeInt(packet.eee.length);
        packet.eee.forEach(element8 => {
            byteBuffer.writeFloat(element8);
        });
    }
    if (packet.eeee === null) {
        byteBuffer.writeInt(0);
    } else {
        byteBuffer.writeInt(packet.eeee.length);
        packet.eeee.forEach(element9 => {
            byteBuffer.writeFloat(element9);
        });
    }
    byteBuffer.writeDouble(packet.f);
    byteBuffer.writeDouble(packet.ff);
    if (packet.fff === null) {
        byteBuffer.writeInt(0);
    } else {
        byteBuffer.writeInt(packet.fff.length);
        packet.fff.forEach(element10 => {
            byteBuffer.writeDouble(element10);
        });
    }
    if (packet.ffff === null) {
        byteBuffer.writeInt(0);
    } else {
        byteBuffer.writeInt(packet.ffff.length);
        packet.ffff.forEach(element11 => {
            byteBuffer.writeDouble(element11);
        });
    }
    byteBuffer.writeBoolean(packet.g);
    byteBuffer.writeBoolean(packet.gg);
    if (packet.ggg === null) {
        byteBuffer.writeInt(0);
    } else {
        byteBuffer.writeInt(packet.ggg.length);
        packet.ggg.forEach(element12 => {
            byteBuffer.writeBoolean(element12);
        });
    }
    if (packet.gggg === null) {
        byteBuffer.writeInt(0);
    } else {
        byteBuffer.writeInt(packet.gggg.length);
        packet.gggg.forEach(element13 => {
            byteBuffer.writeBoolean(element13);
        });
    }
    byteBuffer.writeChar(packet.h);
    byteBuffer.writeChar(packet.hh);
    if (packet.hhh === null) {
        byteBuffer.writeInt(0);
    } else {
        byteBuffer.writeInt(packet.hhh.length);
        packet.hhh.forEach(element14 => {
            byteBuffer.writeChar(element14);
        });
    }
    if (packet.hhhh === null) {
        byteBuffer.writeInt(0);
    } else {
        byteBuffer.writeInt(packet.hhhh.length);
        packet.hhhh.forEach(element15 => {
            byteBuffer.writeChar(element15);
        });
    }
    byteBuffer.writeString(packet.jj);
    if (packet.jjj === null) {
        byteBuffer.writeInt(0);
    } else {
        byteBuffer.writeInt(packet.jjj.length);
        packet.jjj.forEach(element16 => {
            byteBuffer.writeString(element16);
        });
    }
    ProtocolManager.getProtocol(1116).writeObject(byteBuffer, packet.kk);
    if (packet.kkk === null) {
        byteBuffer.writeInt(0);
    } else {
        byteBuffer.writeInt(packet.kkk.length);
        packet.kkk.forEach(element17 => {
            ProtocolManager.getProtocol(1116).writeObject(byteBuffer, element17);
        });
    }
    if (packet.l === null) {
        byteBuffer.writeInt(0);
    } else {
        byteBuffer.writeInt(packet.l.length);
        packet.l.forEach(element18 => {
            byteBuffer.writeInt(element18);
        });
    }
    if (packet.ll === null) {
        byteBuffer.writeInt(0);
    } else {
        byteBuffer.writeInt(packet.ll.length);
        packet.ll.forEach(element19 => {
            if (element19 === null) {
                byteBuffer.writeInt(0);
            } else {
                byteBuffer.writeInt(element19.length);
                element19.forEach(element20 => {
                    if (element20 === null) {
                        byteBuffer.writeInt(0);
                    } else {
                        byteBuffer.writeInt(element20.length);
                        element20.forEach(element21 => {
                            byteBuffer.writeInt(element21);
                        });
                    }
                });
            }
        });
    }
    if (packet.lll === null) {
        byteBuffer.writeInt(0);
    } else {
        byteBuffer.writeInt(packet.lll.length);
        packet.lll.forEach(element22 => {
            if (element22 === null) {
                byteBuffer.writeInt(0);
            } else {
                byteBuffer.writeInt(element22.length);
                element22.forEach(element23 => {
                    ProtocolManager.getProtocol(1116).writeObject(byteBuffer, element23);
                });
            }
        });
    }
    if (packet.llll === null) {
        byteBuffer.writeInt(0);
    } else {
        byteBuffer.writeInt(packet.llll.length);
        packet.llll.forEach(element24 => {
            byteBuffer.writeString(element24);
        });
    }
    if (packet.lllll === null) {
        byteBuffer.writeInt(0);
    } else {
        byteBuffer.writeInt(packet.lllll.length);
        packet.lllll.forEach(element25 => {
            if (element25 === null) {
                byteBuffer.writeInt(0);
            } else {
                byteBuffer.writeInt(element25.size);
                element25.forEach((value27, key26) => {
                    byteBuffer.writeInt(key26);
                    byteBuffer.writeString(value27);
                });
            }
        });
    }
    if (packet.m === null) {
        byteBuffer.writeInt(0);
    } else {
        byteBuffer.writeInt(packet.m.size);
        packet.m.forEach((value29, key28) => {
            byteBuffer.writeInt(key28);
            byteBuffer.writeString(value29);
        });
    }
    if (packet.mm === null) {
        byteBuffer.writeInt(0);
    } else {
        byteBuffer.writeInt(packet.mm.size);
        packet.mm.forEach((value31, key30) => {
            byteBuffer.writeInt(key30);
            ProtocolManager.getProtocol(1116).writeObject(byteBuffer, value31);
        });
    }
    if (packet.mmm === null) {
        byteBuffer.writeInt(0);
    } else {
        byteBuffer.writeInt(packet.mmm.size);
        packet.mmm.forEach((value33, key32) => {
            ProtocolManager.getProtocol(1116).writeObject(byteBuffer, key32);
            if (value33 === null) {
                byteBuffer.writeInt(0);
            } else {
                byteBuffer.writeInt(value33.length);
                value33.forEach(element34 => {
                    byteBuffer.writeInt(element34);
                });
            }
        });
    }
    if (packet.mmmm === null) {
        byteBuffer.writeInt(0);
    } else {
        byteBuffer.writeInt(packet.mmmm.size);
        packet.mmmm.forEach((value36, key35) => {
            if (key35 === null) {
                byteBuffer.writeInt(0);
            } else {
                byteBuffer.writeInt(key35.length);
                key35.forEach(element37 => {
                    if (element37 === null) {
                        byteBuffer.writeInt(0);
                    } else {
                        byteBuffer.writeInt(element37.length);
                        element37.forEach(element38 => {
                            ProtocolManager.getProtocol(1116).writeObject(byteBuffer, element38);
                        });
                    }
                });
            }
            if (value36 === null) {
                byteBuffer.writeInt(0);
            } else {
                byteBuffer.writeInt(value36.length);
                value36.forEach(element39 => {
                    if (element39 === null) {
                        byteBuffer.writeInt(0);
                    } else {
                        byteBuffer.writeInt(element39.length);
                        element39.forEach(element40 => {
                            if (element40 === null) {
                                byteBuffer.writeInt(0);
                            } else {
                                byteBuffer.writeInt(element40.length);
                                element40.forEach(element41 => {
                                    byteBuffer.writeInt(element41);
                                });
                            }
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
        packet.mmmmm.forEach((value43, key42) => {
            if (key42 === null) {
                byteBuffer.writeInt(0);
            } else {
                byteBuffer.writeInt(key42.length);
                key42.forEach(element44 => {
                    if (element44 === null) {
                        byteBuffer.writeInt(0);
                    } else {
                        byteBuffer.writeInt(element44.size);
                        element44.forEach((value46, key45) => {
                            byteBuffer.writeInt(key45);
                            byteBuffer.writeString(value46);
                        });
                    }
                });
            }
            if (value43 === null) {
                byteBuffer.writeInt(0);
            } else {
                byteBuffer.writeInt(value43.size);
                value43.forEach(element47 => {
                    if (element47 === null) {
                        byteBuffer.writeInt(0);
                    } else {
                        byteBuffer.writeInt(element47.size);
                        element47.forEach((value49, key48) => {
                            byteBuffer.writeInt(key48);
                            byteBuffer.writeString(value49);
                        });
                    }
                });
            }
        });
    }
    if (packet.s === null) {
        byteBuffer.writeInt(0);
    } else {
        byteBuffer.writeInt(packet.s.size);
        packet.s.forEach(element50 => {
            byteBuffer.writeInt(element50);
        });
    }
    if (packet.ss === null) {
        byteBuffer.writeInt(0);
    } else {
        byteBuffer.writeInt(packet.ss.size);
        packet.ss.forEach(element51 => {
            if (element51 === null) {
                byteBuffer.writeInt(0);
            } else {
                byteBuffer.writeInt(element51.size);
                element51.forEach(element52 => {
                    if (element52 === null) {
                        byteBuffer.writeInt(0);
                    } else {
                        byteBuffer.writeInt(element52.length);
                        element52.forEach(element53 => {
                            byteBuffer.writeInt(element53);
                        });
                    }
                });
            }
        });
    }
    if (packet.sss === null) {
        byteBuffer.writeInt(0);
    } else {
        byteBuffer.writeInt(packet.sss.size);
        packet.sss.forEach(element54 => {
            if (element54 === null) {
                byteBuffer.writeInt(0);
            } else {
                byteBuffer.writeInt(element54.size);
                element54.forEach(element55 => {
                    ProtocolManager.getProtocol(1116).writeObject(byteBuffer, element55);
                });
            }
        });
    }
    if (packet.ssss === null) {
        byteBuffer.writeInt(0);
    } else {
        byteBuffer.writeInt(packet.ssss.size);
        packet.ssss.forEach(element56 => {
            byteBuffer.writeString(element56);
        });
    }
    if (packet.sssss === null) {
        byteBuffer.writeInt(0);
    } else {
        byteBuffer.writeInt(packet.sssss.size);
        packet.sssss.forEach(element57 => {
            if (element57 === null) {
                byteBuffer.writeInt(0);
            } else {
                byteBuffer.writeInt(element57.size);
                element57.forEach((value59, key58) => {
                    byteBuffer.writeInt(key58);
                    byteBuffer.writeString(value59);
                });
            }
        });
    }
};

ComplexObject.readObject = function (byteBuffer) {
    if (!byteBuffer.readBoolean()) {
        return null;
    }
    const packet = new ComplexObject();
    const result60 = byteBuffer.readByte();
    packet.a = result60;
    const result61 = byteBuffer.readByte();
    packet.aa = result61;
    const result62 = [];
    const size64 = byteBuffer.readInt();
    if (size64 > 0) {
        for (let index63 = 0; index63 < size64; index63++) {
            const result65 = byteBuffer.readByte();
            result62.push(result65);
        }
    }
    packet.aaa = result62;
    const result66 = [];
    const size68 = byteBuffer.readInt();
    if (size68 > 0) {
        for (let index67 = 0; index67 < size68; index67++) {
            const result69 = byteBuffer.readByte();
            result66.push(result69);
        }
    }
    packet.aaaa = result66;
    const result70 = byteBuffer.readShort();
    packet.b = result70;
    const result71 = byteBuffer.readShort();
    packet.bb = result71;
    const result72 = [];
    const size74 = byteBuffer.readInt();
    if (size74 > 0) {
        for (let index73 = 0; index73 < size74; index73++) {
            const result75 = byteBuffer.readShort();
            result72.push(result75);
        }
    }
    packet.bbb = result72;
    const result76 = [];
    const size78 = byteBuffer.readInt();
    if (size78 > 0) {
        for (let index77 = 0; index77 < size78; index77++) {
            const result79 = byteBuffer.readShort();
            result76.push(result79);
        }
    }
    packet.bbbb = result76;
    const result80 = byteBuffer.readInt();
    packet.c = result80;
    const result81 = byteBuffer.readInt();
    packet.cc = result81;
    const result82 = [];
    const size84 = byteBuffer.readInt();
    if (size84 > 0) {
        for (let index83 = 0; index83 < size84; index83++) {
            const result85 = byteBuffer.readInt();
            result82.push(result85);
        }
    }
    packet.ccc = result82;
    const result86 = [];
    const size88 = byteBuffer.readInt();
    if (size88 > 0) {
        for (let index87 = 0; index87 < size88; index87++) {
            const result89 = byteBuffer.readInt();
            result86.push(result89);
        }
    }
    packet.cccc = result86;
    const result90 = byteBuffer.readLong();
    packet.d = result90;
    const result91 = byteBuffer.readLong();
    packet.dd = result91;
    const result92 = [];
    const size94 = byteBuffer.readInt();
    if (size94 > 0) {
        for (let index93 = 0; index93 < size94; index93++) {
            const result95 = byteBuffer.readLong();
            result92.push(result95);
        }
    }
    packet.ddd = result92;
    const result96 = [];
    const size98 = byteBuffer.readInt();
    if (size98 > 0) {
        for (let index97 = 0; index97 < size98; index97++) {
            const result99 = byteBuffer.readLong();
            result96.push(result99);
        }
    }
    packet.dddd = result96;
    const result100 = byteBuffer.readFloat();
    packet.e = result100;
    const result101 = byteBuffer.readFloat();
    packet.ee = result101;
    const result102 = [];
    const size104 = byteBuffer.readInt();
    if (size104 > 0) {
        for (let index103 = 0; index103 < size104; index103++) {
            const result105 = byteBuffer.readFloat();
            result102.push(result105);
        }
    }
    packet.eee = result102;
    const result106 = [];
    const size108 = byteBuffer.readInt();
    if (size108 > 0) {
        for (let index107 = 0; index107 < size108; index107++) {
            const result109 = byteBuffer.readFloat();
            result106.push(result109);
        }
    }
    packet.eeee = result106;
    const result110 = byteBuffer.readDouble();
    packet.f = result110;
    const result111 = byteBuffer.readDouble();
    packet.ff = result111;
    const result112 = [];
    const size114 = byteBuffer.readInt();
    if (size114 > 0) {
        for (let index113 = 0; index113 < size114; index113++) {
            const result115 = byteBuffer.readDouble();
            result112.push(result115);
        }
    }
    packet.fff = result112;
    const result116 = [];
    const size118 = byteBuffer.readInt();
    if (size118 > 0) {
        for (let index117 = 0; index117 < size118; index117++) {
            const result119 = byteBuffer.readDouble();
            result116.push(result119);
        }
    }
    packet.ffff = result116;
    const result120 = byteBuffer.readBoolean();
    packet.g = result120;
    const result121 = byteBuffer.readBoolean();
    packet.gg = result121;
    const result122 = [];
    const size124 = byteBuffer.readInt();
    if (size124 > 0) {
        for (let index123 = 0; index123 < size124; index123++) {
            const result125 = byteBuffer.readBoolean();
            result122.push(result125);
        }
    }
    packet.ggg = result122;
    const result126 = [];
    const size128 = byteBuffer.readInt();
    if (size128 > 0) {
        for (let index127 = 0; index127 < size128; index127++) {
            const result129 = byteBuffer.readBoolean();
            result126.push(result129);
        }
    }
    packet.gggg = result126;
    const result130 = byteBuffer.readChar();
    packet.h = result130;
    const result131 = byteBuffer.readChar();
    packet.hh = result131;
    const result132 = [];
    const size134 = byteBuffer.readInt();
    if (size134 > 0) {
        for (let index133 = 0; index133 < size134; index133++) {
            const result135 = byteBuffer.readChar();
            result132.push(result135);
        }
    }
    packet.hhh = result132;
    const result136 = [];
    const size138 = byteBuffer.readInt();
    if (size138 > 0) {
        for (let index137 = 0; index137 < size138; index137++) {
            const result139 = byteBuffer.readChar();
            result136.push(result139);
        }
    }
    packet.hhhh = result136;
    const result140 = byteBuffer.readString();
    packet.jj = result140;
    const result141 = [];
    const size143 = byteBuffer.readInt();
    if (size143 > 0) {
        for (let index142 = 0; index142 < size143; index142++) {
            const result144 = byteBuffer.readString();
            result141.push(result144);
        }
    }
    packet.jjj = result141;
    const result145 = ProtocolManager.getProtocol(1116).readObject(byteBuffer);
    packet.kk = result145;
    const result146 = [];
    const size148 = byteBuffer.readInt();
    if (size148 > 0) {
        for (let index147 = 0; index147 < size148; index147++) {
            const result149 = ProtocolManager.getProtocol(1116).readObject(byteBuffer);
            result146.push(result149);
        }
    }
    packet.kkk = result146;
    const result150 = [];
    const size151 = byteBuffer.readInt();
    if (size151 > 0) {
        for (let index152 = 0; index152 < size151; index152++) {
            const result153 = byteBuffer.readInt();
            result150.push(result153);
        }
    }
    packet.l = result150;
    const result154 = [];
    const size155 = byteBuffer.readInt();
    if (size155 > 0) {
        for (let index156 = 0; index156 < size155; index156++) {
            const result157 = [];
            const size158 = byteBuffer.readInt();
            if (size158 > 0) {
                for (let index159 = 0; index159 < size158; index159++) {
                    const result160 = [];
                    const size161 = byteBuffer.readInt();
                    if (size161 > 0) {
                        for (let index162 = 0; index162 < size161; index162++) {
                            const result163 = byteBuffer.readInt();
                            result160.push(result163);
                        }
                    }
                    result157.push(result160);
                }
            }
            result154.push(result157);
        }
    }
    packet.ll = result154;
    const result164 = [];
    const size165 = byteBuffer.readInt();
    if (size165 > 0) {
        for (let index166 = 0; index166 < size165; index166++) {
            const result167 = [];
            const size168 = byteBuffer.readInt();
            if (size168 > 0) {
                for (let index169 = 0; index169 < size168; index169++) {
                    const result170 = ProtocolManager.getProtocol(1116).readObject(byteBuffer);
                    result167.push(result170);
                }
            }
            result164.push(result167);
        }
    }
    packet.lll = result164;
    const result171 = [];
    const size172 = byteBuffer.readInt();
    if (size172 > 0) {
        for (let index173 = 0; index173 < size172; index173++) {
            const result174 = byteBuffer.readString();
            result171.push(result174);
        }
    }
    packet.llll = result171;
    const result175 = [];
    const size176 = byteBuffer.readInt();
    if (size176 > 0) {
        for (let index177 = 0; index177 < size176; index177++) {
            const result178 = new Map();
            const size179 = byteBuffer.readInt();
            if (size179 > 0) {
                for (let index180 = 0; index180 < size179; index180++) {
                    const result181 = byteBuffer.readInt();
                    const result182 = byteBuffer.readString();
                    result178.set(result181, result182);
                }
            }
            result175.push(result178);
        }
    }
    packet.lllll = result175;
    const result183 = new Map();
    const size184 = byteBuffer.readInt();
    if (size184 > 0) {
        for (let index185 = 0; index185 < size184; index185++) {
            const result186 = byteBuffer.readInt();
            const result187 = byteBuffer.readString();
            result183.set(result186, result187);
        }
    }
    packet.m = result183;
    const result188 = new Map();
    const size189 = byteBuffer.readInt();
    if (size189 > 0) {
        for (let index190 = 0; index190 < size189; index190++) {
            const result191 = byteBuffer.readInt();
            const result192 = ProtocolManager.getProtocol(1116).readObject(byteBuffer);
            result188.set(result191, result192);
        }
    }
    packet.mm = result188;
    const result193 = new Map();
    const size194 = byteBuffer.readInt();
    if (size194 > 0) {
        for (let index195 = 0; index195 < size194; index195++) {
            const result196 = ProtocolManager.getProtocol(1116).readObject(byteBuffer);
            const result197 = [];
            const size198 = byteBuffer.readInt();
            if (size198 > 0) {
                for (let index199 = 0; index199 < size198; index199++) {
                    const result200 = byteBuffer.readInt();
                    result197.push(result200);
                }
            }
            result193.set(result196, result197);
        }
    }
    packet.mmm = result193;
    const result201 = new Map();
    const size202 = byteBuffer.readInt();
    if (size202 > 0) {
        for (let index203 = 0; index203 < size202; index203++) {
            const result204 = [];
            const size205 = byteBuffer.readInt();
            if (size205 > 0) {
                for (let index206 = 0; index206 < size205; index206++) {
                    const result207 = [];
                    const size208 = byteBuffer.readInt();
                    if (size208 > 0) {
                        for (let index209 = 0; index209 < size208; index209++) {
                            const result210 = ProtocolManager.getProtocol(1116).readObject(byteBuffer);
                            result207.push(result210);
                        }
                    }
                    result204.push(result207);
                }
            }
            const result211 = [];
            const size212 = byteBuffer.readInt();
            if (size212 > 0) {
                for (let index213 = 0; index213 < size212; index213++) {
                    const result214 = [];
                    const size215 = byteBuffer.readInt();
                    if (size215 > 0) {
                        for (let index216 = 0; index216 < size215; index216++) {
                            const result217 = [];
                            const size218 = byteBuffer.readInt();
                            if (size218 > 0) {
                                for (let index219 = 0; index219 < size218; index219++) {
                                    const result220 = byteBuffer.readInt();
                                    result217.push(result220);
                                }
                            }
                            result214.push(result217);
                        }
                    }
                    result211.push(result214);
                }
            }
            result201.set(result204, result211);
        }
    }
    packet.mmmm = result201;
    const result221 = new Map();
    const size222 = byteBuffer.readInt();
    if (size222 > 0) {
        for (let index223 = 0; index223 < size222; index223++) {
            const result224 = [];
            const size225 = byteBuffer.readInt();
            if (size225 > 0) {
                for (let index226 = 0; index226 < size225; index226++) {
                    const result227 = new Map();
                    const size228 = byteBuffer.readInt();
                    if (size228 > 0) {
                        for (let index229 = 0; index229 < size228; index229++) {
                            const result230 = byteBuffer.readInt();
                            const result231 = byteBuffer.readString();
                            result227.set(result230, result231);
                        }
                    }
                    result224.push(result227);
                }
            }
            const result232 = new Set();
            const size233 = byteBuffer.readInt();
            if (size233 > 0) {
                for (let index234 = 0; index234 < size233; index234++) {
                    const result235 = new Map();
                    const size236 = byteBuffer.readInt();
                    if (size236 > 0) {
                        for (let index237 = 0; index237 < size236; index237++) {
                            const result238 = byteBuffer.readInt();
                            const result239 = byteBuffer.readString();
                            result235.set(result238, result239);
                        }
                    }
                    result232.add(result235);
                }
            }
            result221.set(result224, result232);
        }
    }
    packet.mmmmm = result221;
    const result240 = new Set();
    const size241 = byteBuffer.readInt();
    if (size241 > 0) {
        for (let index242 = 0; index242 < size241; index242++) {
            const result243 = byteBuffer.readInt();
            result240.add(result243);
        }
    }
    packet.s = result240;
    const result244 = new Set();
    const size245 = byteBuffer.readInt();
    if (size245 > 0) {
        for (let index246 = 0; index246 < size245; index246++) {
            const result247 = new Set();
            const size248 = byteBuffer.readInt();
            if (size248 > 0) {
                for (let index249 = 0; index249 < size248; index249++) {
                    const result250 = [];
                    const size251 = byteBuffer.readInt();
                    if (size251 > 0) {
                        for (let index252 = 0; index252 < size251; index252++) {
                            const result253 = byteBuffer.readInt();
                            result250.push(result253);
                        }
                    }
                    result247.add(result250);
                }
            }
            result244.add(result247);
        }
    }
    packet.ss = result244;
    const result254 = new Set();
    const size255 = byteBuffer.readInt();
    if (size255 > 0) {
        for (let index256 = 0; index256 < size255; index256++) {
            const result257 = new Set();
            const size258 = byteBuffer.readInt();
            if (size258 > 0) {
                for (let index259 = 0; index259 < size258; index259++) {
                    const result260 = ProtocolManager.getProtocol(1116).readObject(byteBuffer);
                    result257.add(result260);
                }
            }
            result254.add(result257);
        }
    }
    packet.sss = result254;
    const result261 = new Set();
    const size262 = byteBuffer.readInt();
    if (size262 > 0) {
        for (let index263 = 0; index263 < size262; index263++) {
            const result264 = byteBuffer.readString();
            result261.add(result264);
        }
    }
    packet.ssss = result261;
    const result265 = new Set();
    const size266 = byteBuffer.readInt();
    if (size266 > 0) {
        for (let index267 = 0; index267 < size266; index267++) {
            const result268 = new Map();
            const size269 = byteBuffer.readInt();
            if (size269 > 0) {
                for (let index270 = 0; index270 < size269; index270++) {
                    const result271 = byteBuffer.readInt();
                    const result272 = byteBuffer.readString();
                    result268.set(result271, result272);
                }
            }
            result265.add(result268);
        }
    }
    packet.sssss = result265;
    return packet;
};

export default ComplexObject;
