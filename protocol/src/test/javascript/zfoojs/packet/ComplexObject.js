// 复杂的对象，包括了各种复杂的结构，数组，List，Set，Map
const ComplexObject = function() {
    // byte类型，最简单的整形
    this.a = 0; // number
    // byte的包装类型，优先使用基础类型，包装类型会有装箱拆箱
    this.aa = 0; // number
    // 数组类型
    this.aaa = []; // Array<number>
    this.aaaa = []; // Array<number>
    this.b = 0; // number
    this.bb = 0; // number
    this.bbb = []; // Array<number>
    this.bbbb = []; // Array<number>
    this.c = 0; // number
    this.cc = 0; // number
    this.ccc = []; // Array<number>
    this.cccc = []; // Array<number>
    this.d = 0; // number
    this.dd = 0; // number
    this.ddd = []; // Array<number>
    this.dddd = []; // Array<number>
    this.e = 0; // number
    this.ee = 0; // number
    this.eee = []; // Array<number>
    this.eeee = []; // Array<number>
    this.f = 0; // number
    this.ff = 0; // number
    this.fff = []; // Array<number>
    this.ffff = []; // Array<number>
    this.g = false; // boolean
    this.gg = false; // boolean
    this.ggg = []; // Array<boolean>
    this.gggg = []; // Array<boolean>
    this.jj = ""; // string
    this.jjj = []; // Array<string>
    this.kk = null; // ObjectA | null
    this.kkk = []; // Array<ObjectA>
    this.l = []; // Array<number>
    this.ll = []; // Array<Array<Array<number>>>
    this.lll = []; // Array<Array<ObjectA>>
    this.llll = []; // Array<string>
    this.lllll = []; // Array<Map<number, string>>
    this.m = new Map(); // Map<number, string>
    this.mm = new Map(); // Map<number, ObjectA>
    this.mmm = new Map(); // Map<ObjectA, Array<number>>
    this.mmmm = new Map(); // Map<Array<Array<ObjectA>>, Array<Array<Array<number>>>>
    this.mmmmm = new Map(); // Map<Array<Map<number, string>>, Set<Map<number, string>>>
    this.s = new Set(); // Set<number>
    this.ss = new Set(); // Set<Set<Array<number>>>
    this.sss = new Set(); // Set<Set<ObjectA>>
    this.ssss = new Set(); // Set<string>
    this.sssss = new Set(); // Set<Map<number, string>>
    // 如果要修改协议并且兼容老协议，需要加上Compatible注解，按照增加的顺序添加order
    this.myCompatible = 0; // number
    this.myObject = null; // ObjectA | null
};

ComplexObject.prototype.protocolId = function() {
    return 100;
};

ComplexObject.write = function(buffer, packet) {
    if (packet === null) {
        buffer.writeInt(0);
        return;
    }
    const beforeWriteIndex = buffer.getWriteOffset();
    buffer.writeInt(36962);
    buffer.writeByte(packet.a);
    buffer.writeByte(packet.aa);
    buffer.writeByteArray(packet.aaa);
    buffer.writeByteArray(packet.aaaa);
    buffer.writeShort(packet.b);
    buffer.writeShort(packet.bb);
    buffer.writeShortArray(packet.bbb);
    buffer.writeShortArray(packet.bbbb);
    buffer.writeInt(packet.c);
    buffer.writeInt(packet.cc);
    buffer.writeIntArray(packet.ccc);
    buffer.writeIntArray(packet.cccc);
    buffer.writeLong(packet.d);
    buffer.writeLong(packet.dd);
    buffer.writeLongArray(packet.ddd);
    buffer.writeLongArray(packet.dddd);
    buffer.writeFloat(packet.e);
    buffer.writeFloat(packet.ee);
    buffer.writeFloatArray(packet.eee);
    buffer.writeFloatArray(packet.eeee);
    buffer.writeDouble(packet.f);
    buffer.writeDouble(packet.ff);
    buffer.writeDoubleArray(packet.fff);
    buffer.writeDoubleArray(packet.ffff);
    buffer.writeBoolean(packet.g);
    buffer.writeBoolean(packet.gg);
    buffer.writeBooleanArray(packet.ggg);
    buffer.writeBooleanArray(packet.gggg);
    buffer.writeString(packet.jj);
    buffer.writeStringArray(packet.jjj);
    buffer.writePacket(packet.kk, 102);
    buffer.writePacketArray(packet.kkk, 102);
    buffer.writeIntList(packet.l);
    if (packet.ll === null) {
        buffer.writeInt(0);
    } else {
        buffer.writeInt(packet.ll.length);
        packet.ll.forEach(element0 => {
            if (element0 === null) {
                buffer.writeInt(0);
            } else {
                buffer.writeInt(element0.length);
                element0.forEach(element1 => {
                    buffer.writeIntList(element1);
                });
            }
        });
    }
    if (packet.lll === null) {
        buffer.writeInt(0);
    } else {
        buffer.writeInt(packet.lll.length);
        packet.lll.forEach(element2 => {
            buffer.writePacketList(element2, 102);
        });
    }
    buffer.writeStringList(packet.llll);
    if (packet.lllll === null) {
        buffer.writeInt(0);
    } else {
        buffer.writeInt(packet.lllll.length);
        packet.lllll.forEach(element3 => {
            buffer.writeIntStringMap(element3);
        });
    }
    buffer.writeIntStringMap(packet.m);
    buffer.writeIntPacketMap(packet.mm, 102);
    if (packet.mmm === null) {
        buffer.writeInt(0);
    } else {
        buffer.writeInt(packet.mmm.size);
        packet.mmm.forEach((value5, key4) => {
            buffer.writePacket(key4, 102);
            buffer.writeIntList(value5);
        });
    }
    if (packet.mmmm === null) {
        buffer.writeInt(0);
    } else {
        buffer.writeInt(packet.mmmm.size);
        packet.mmmm.forEach((value7, key6) => {
            if (key6 === null) {
                buffer.writeInt(0);
            } else {
                buffer.writeInt(key6.length);
                key6.forEach(element8 => {
                    buffer.writePacketList(element8, 102);
                });
            }
            if (value7 === null) {
                buffer.writeInt(0);
            } else {
                buffer.writeInt(value7.length);
                value7.forEach(element9 => {
                    if (element9 === null) {
                        buffer.writeInt(0);
                    } else {
                        buffer.writeInt(element9.length);
                        element9.forEach(element10 => {
                            buffer.writeIntList(element10);
                        });
                    }
                });
            }
        });
    }
    if (packet.mmmmm === null) {
        buffer.writeInt(0);
    } else {
        buffer.writeInt(packet.mmmmm.size);
        packet.mmmmm.forEach((value12, key11) => {
            if (key11 === null) {
                buffer.writeInt(0);
            } else {
                buffer.writeInt(key11.length);
                key11.forEach(element13 => {
                    buffer.writeIntStringMap(element13);
                });
            }
            if (value12 === null) {
                buffer.writeInt(0);
            } else {
                buffer.writeInt(value12.size);
                value12.forEach(element14 => {
                    buffer.writeIntStringMap(element14);
                });
            }
        });
    }
    buffer.writeIntSet(packet.s);
    if (packet.ss === null) {
        buffer.writeInt(0);
    } else {
        buffer.writeInt(packet.ss.size);
        packet.ss.forEach(element15 => {
            if (element15 === null) {
                buffer.writeInt(0);
            } else {
                buffer.writeInt(element15.size);
                element15.forEach(element16 => {
                    buffer.writeIntList(element16);
                });
            }
        });
    }
    if (packet.sss === null) {
        buffer.writeInt(0);
    } else {
        buffer.writeInt(packet.sss.size);
        packet.sss.forEach(element17 => {
            buffer.writePacketSet(element17, 102);
        });
    }
    buffer.writeStringSet(packet.ssss);
    if (packet.sssss === null) {
        buffer.writeInt(0);
    } else {
        buffer.writeInt(packet.sssss.size);
        packet.sssss.forEach(element18 => {
            buffer.writeIntStringMap(element18);
        });
    }
    buffer.writeInt(packet.myCompatible);
    buffer.writePacket(packet.myObject, 102);
    buffer.adjustPadding(36962, beforeWriteIndex);
};

ComplexObject.read = function(buffer) {
    const length = buffer.readInt();
    if (length === 0) {
        return null;
    }
    const beforeReadIndex = buffer.getReadOffset();
    const packet = new ComplexObject();
    const result19 = buffer.readByte();
    packet.a = result19;
    const result20 = buffer.readByte();
    packet.aa = result20;
    const array21 = buffer.readByteArray();
    packet.aaa = array21;
    const array22 = buffer.readByteArray();
    packet.aaaa = array22;
    const result23 = buffer.readShort();
    packet.b = result23;
    const result24 = buffer.readShort();
    packet.bb = result24;
    const array25 = buffer.readShortArray();
    packet.bbb = array25;
    const array26 = buffer.readShortArray();
    packet.bbbb = array26;
    const result27 = buffer.readInt();
    packet.c = result27;
    const result28 = buffer.readInt();
    packet.cc = result28;
    const array29 = buffer.readIntArray();
    packet.ccc = array29;
    const array30 = buffer.readIntArray();
    packet.cccc = array30;
    const result31 = buffer.readLong();
    packet.d = result31;
    const result32 = buffer.readLong();
    packet.dd = result32;
    const array33 = buffer.readLongArray();
    packet.ddd = array33;
    const array34 = buffer.readLongArray();
    packet.dddd = array34;
    const result35 = buffer.readFloat();
    packet.e = result35;
    const result36 = buffer.readFloat();
    packet.ee = result36;
    const array37 = buffer.readFloatArray();
    packet.eee = array37;
    const array38 = buffer.readFloatArray();
    packet.eeee = array38;
    const result39 = buffer.readDouble();
    packet.f = result39;
    const result40 = buffer.readDouble();
    packet.ff = result40;
    const array41 = buffer.readDoubleArray();
    packet.fff = array41;
    const array42 = buffer.readDoubleArray();
    packet.ffff = array42;
    const result43 = buffer.readBoolean(); 
    packet.g = result43;
    const result44 = buffer.readBoolean(); 
    packet.gg = result44;
    const array45 = buffer.readBooleanArray();
    packet.ggg = array45;
    const array46 = buffer.readBooleanArray();
    packet.gggg = array46;
    const result47 = buffer.readString();
    packet.jj = result47;
    const array48 = buffer.readStringArray();
    packet.jjj = array48;
    const result49 = buffer.readPacket(102);
    packet.kk = result49;
    const array50 = buffer.readPacketArray(102);
    packet.kkk = array50;
    const list51 = buffer.readIntList();
    packet.l = list51;
    const result52 = [];
    const size53 = buffer.readInt();
    if (size53 > 0) {
        for (let index54 = 0; index54 < size53; index54++) {
            const result55 = [];
            const size56 = buffer.readInt();
            if (size56 > 0) {
                for (let index57 = 0; index57 < size56; index57++) {
                    const list58 = buffer.readIntList();
                    result55.push(list58);
                }
            }
            result52.push(result55);
        }
    }
    packet.ll = result52;
    const result59 = [];
    const size60 = buffer.readInt();
    if (size60 > 0) {
        for (let index61 = 0; index61 < size60; index61++) {
            const list62 = buffer.readPacketList(102);
            result59.push(list62);
        }
    }
    packet.lll = result59;
    const list63 = buffer.readStringList();
    packet.llll = list63;
    const result64 = [];
    const size65 = buffer.readInt();
    if (size65 > 0) {
        for (let index66 = 0; index66 < size65; index66++) {
            const map67 = buffer.readIntStringMap();
            result64.push(map67);
        }
    }
    packet.lllll = result64;
    const map68 = buffer.readIntStringMap();
    packet.m = map68;
    const map69 = buffer.readIntPacketMap(102);
    packet.mm = map69;
    const result70 = new Map();
    const size71 = buffer.readInt();
    if (size71 > 0) {
        for (let index72 = 0; index72 < size71; index72++) {
            const result73 = buffer.readPacket(102);
            const list74 = buffer.readIntList();
            result70.set(result73, list74);
        }
    }
    packet.mmm = result70;
    const result75 = new Map();
    const size76 = buffer.readInt();
    if (size76 > 0) {
        for (let index77 = 0; index77 < size76; index77++) {
            const result78 = [];
            const size79 = buffer.readInt();
            if (size79 > 0) {
                for (let index80 = 0; index80 < size79; index80++) {
                    const list81 = buffer.readPacketList(102);
                    result78.push(list81);
                }
            }
            const result82 = [];
            const size83 = buffer.readInt();
            if (size83 > 0) {
                for (let index84 = 0; index84 < size83; index84++) {
                    const result85 = [];
                    const size86 = buffer.readInt();
                    if (size86 > 0) {
                        for (let index87 = 0; index87 < size86; index87++) {
                            const list88 = buffer.readIntList();
                            result85.push(list88);
                        }
                    }
                    result82.push(result85);
                }
            }
            result75.set(result78, result82);
        }
    }
    packet.mmmm = result75;
    const result89 = new Map();
    const size90 = buffer.readInt();
    if (size90 > 0) {
        for (let index91 = 0; index91 < size90; index91++) {
            const result92 = [];
            const size93 = buffer.readInt();
            if (size93 > 0) {
                for (let index94 = 0; index94 < size93; index94++) {
                    const map95 = buffer.readIntStringMap();
                    result92.push(map95);
                }
            }
            const result96 = new Set();
            const size97 = buffer.readInt();
            if (size97 > 0) {
                for (let index98 = 0; index98 < size97; index98++) {
                    const map99 = buffer.readIntStringMap();
                    result96.add(map99);
                }
            }
            result89.set(result92, result96);
        }
    }
    packet.mmmmm = result89;
    const set100 = buffer.readIntSet();
    packet.s = set100;
    const result101 = new Set();
    const size102 = buffer.readInt();
    if (size102 > 0) {
        for (let index103 = 0; index103 < size102; index103++) {
            const result104 = new Set();
            const size105 = buffer.readInt();
            if (size105 > 0) {
                for (let index106 = 0; index106 < size105; index106++) {
                    const list107 = buffer.readIntList();
                    result104.add(list107);
                }
            }
            result101.add(result104);
        }
    }
    packet.ss = result101;
    const result108 = new Set();
    const size109 = buffer.readInt();
    if (size109 > 0) {
        for (let index110 = 0; index110 < size109; index110++) {
            const set111 = buffer.readPacketSet(102);
            result108.add(set111);
        }
    }
    packet.sss = result108;
    const set112 = buffer.readStringSet();
    packet.ssss = set112;
    const result113 = new Set();
    const size114 = buffer.readInt();
    if (size114 > 0) {
        for (let index115 = 0; index115 < size114; index115++) {
            const map116 = buffer.readIntStringMap();
            result113.add(map116);
        }
    }
    packet.sssss = result113;
    if (buffer.compatibleRead(beforeReadIndex, length)) {
        const result117 = buffer.readInt();
        packet.myCompatible = result117;
    }
    if (buffer.compatibleRead(beforeReadIndex, length)) {
        const result118 = buffer.readPacket(102);
        packet.myObject = result118;
    }
    if (length > 0) {
        buffer.setReadOffset(beforeReadIndex + length);
    }
    return packet;
};

export default ComplexObject;
