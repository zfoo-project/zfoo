// 复杂的对象，包括了各种复杂的结构，数组，List，Set，Map
class ComplexObject {
    // byte类型，最简单的整形
    a = 0; // number
    // byte的包装类型，优先使用基础类型，包装类型会有装箱拆箱
    aa = 0; // number
    // 数组类型
    aaa = []; // Array<number>
    aaaa = []; // Array<number>
    b = 0; // number
    bb = 0; // number
    bbb = []; // Array<number>
    bbbb = []; // Array<number>
    c = 0; // number
    cc = 0; // number
    ccc = []; // Array<number>
    cccc = []; // Array<number>
    d = 0; // number
    dd = 0; // number
    ddd = []; // Array<number>
    dddd = []; // Array<number>
    e = 0; // number
    ee = 0; // number
    eee = []; // Array<number>
    eeee = []; // Array<number>
    f = 0; // number
    ff = 0; // number
    fff = []; // Array<number>
    ffff = []; // Array<number>
    g = false; // boolean
    gg = false; // boolean
    ggg = []; // Array<boolean>
    gggg = []; // Array<boolean>
    jj = ""; // string
    jjj = []; // Array<string>
    kk = null; // ObjectA | null
    kkk = []; // Array<ObjectA>
    l = []; // Array<number>
    ll = []; // Array<Array<Array<number>>>
    lll = []; // Array<Array<ObjectA>>
    llll = []; // Array<string>
    lllll = []; // Array<Map<number, string>>
    m = new Map(); // Map<number, string>
    mm = new Map(); // Map<number, ObjectA>
    mmm = new Map(); // Map<ObjectA, Array<number>>
    mmmm = new Map(); // Map<Array<Array<ObjectA>>, Array<Array<Array<number>>>>
    mmmmm = new Map(); // Map<Array<Map<number, string>>, Set<Map<number, string>>>
    s = new Set(); // Set<number>
    ss = new Set(); // Set<Set<Array<number>>>
    sss = new Set(); // Set<Set<ObjectA>>
    ssss = new Set(); // Set<string>
    sssss = new Set(); // Set<Map<number, string>>
    // 如果要修改协议并且兼容老协议，需要加上Compatible注解，保持Compatible注解的value自增
    myCompatible = 0; // number
    myObject = null; // ObjectA | null

    static PROTOCOL_ID = 100;

    protocolId() {
        return ComplexObject.PROTOCOL_ID;
    }

    static write(buffer, packet) {
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
    }

    static read(buffer) {
        const length = buffer.readInt();
        if (length === 0) {
            return null;
        }
        const beforeReadIndex = buffer.getReadOffset();
        const packet = new ComplexObject();
        const result0 = buffer.readByte();
        packet.a = result0;
        const result1 = buffer.readByte();
        packet.aa = result1;
        const array2 = buffer.readByteArray();
        packet.aaa = array2;
        const array3 = buffer.readByteArray();
        packet.aaaa = array3;
        const result4 = buffer.readShort();
        packet.b = result4;
        const result5 = buffer.readShort();
        packet.bb = result5;
        const array6 = buffer.readShortArray();
        packet.bbb = array6;
        const array7 = buffer.readShortArray();
        packet.bbbb = array7;
        const result8 = buffer.readInt();
        packet.c = result8;
        const result9 = buffer.readInt();
        packet.cc = result9;
        const array10 = buffer.readIntArray();
        packet.ccc = array10;
        const array11 = buffer.readIntArray();
        packet.cccc = array11;
        const result12 = buffer.readLong();
        packet.d = result12;
        const result13 = buffer.readLong();
        packet.dd = result13;
        const array14 = buffer.readLongArray();
        packet.ddd = array14;
        const array15 = buffer.readLongArray();
        packet.dddd = array15;
        const result16 = buffer.readFloat();
        packet.e = result16;
        const result17 = buffer.readFloat();
        packet.ee = result17;
        const array18 = buffer.readFloatArray();
        packet.eee = array18;
        const array19 = buffer.readFloatArray();
        packet.eeee = array19;
        const result20 = buffer.readDouble();
        packet.f = result20;
        const result21 = buffer.readDouble();
        packet.ff = result21;
        const array22 = buffer.readDoubleArray();
        packet.fff = array22;
        const array23 = buffer.readDoubleArray();
        packet.ffff = array23;
        const result24 = buffer.readBoolean(); 
        packet.g = result24;
        const result25 = buffer.readBoolean(); 
        packet.gg = result25;
        const array26 = buffer.readBooleanArray();
        packet.ggg = array26;
        const array27 = buffer.readBooleanArray();
        packet.gggg = array27;
        const result28 = buffer.readString();
        packet.jj = result28;
        const array29 = buffer.readStringArray();
        packet.jjj = array29;
        const result30 = buffer.readPacket(102);
        packet.kk = result30;
        const array31 = buffer.readPacketArray(102);
        packet.kkk = array31;
        const list32 = buffer.readIntList();
        packet.l = list32;
        const result33 = [];
        const size34 = buffer.readInt();
        if (size34 > 0) {
            for (let index35 = 0; index35 < size34; index35++) {
                const result36 = [];
                const size37 = buffer.readInt();
                if (size37 > 0) {
                    for (let index38 = 0; index38 < size37; index38++) {
                        const list39 = buffer.readIntList();
                        result36.push(list39);
                    }
                }
                result33.push(result36);
            }
        }
        packet.ll = result33;
        const result40 = [];
        const size41 = buffer.readInt();
        if (size41 > 0) {
            for (let index42 = 0; index42 < size41; index42++) {
                const list43 = buffer.readPacketList(102);
                result40.push(list43);
            }
        }
        packet.lll = result40;
        const list44 = buffer.readStringList();
        packet.llll = list44;
        const result45 = [];
        const size46 = buffer.readInt();
        if (size46 > 0) {
            for (let index47 = 0; index47 < size46; index47++) {
                const map48 = buffer.readIntStringMap();
                result45.push(map48);
            }
        }
        packet.lllll = result45;
        const map49 = buffer.readIntStringMap();
        packet.m = map49;
        const map50 = buffer.readIntPacketMap(102);
        packet.mm = map50;
        const result51 = new Map();
        const size52 = buffer.readInt();
        if (size52 > 0) {
            for (let index53 = 0; index53 < size52; index53++) {
                const result54 = buffer.readPacket(102);
                const list55 = buffer.readIntList();
                result51.set(result54, list55);
            }
        }
        packet.mmm = result51;
        const result56 = new Map();
        const size57 = buffer.readInt();
        if (size57 > 0) {
            for (let index58 = 0; index58 < size57; index58++) {
                const result59 = [];
                const size60 = buffer.readInt();
                if (size60 > 0) {
                    for (let index61 = 0; index61 < size60; index61++) {
                        const list62 = buffer.readPacketList(102);
                        result59.push(list62);
                    }
                }
                const result63 = [];
                const size64 = buffer.readInt();
                if (size64 > 0) {
                    for (let index65 = 0; index65 < size64; index65++) {
                        const result66 = [];
                        const size67 = buffer.readInt();
                        if (size67 > 0) {
                            for (let index68 = 0; index68 < size67; index68++) {
                                const list69 = buffer.readIntList();
                                result66.push(list69);
                            }
                        }
                        result63.push(result66);
                    }
                }
                result56.set(result59, result63);
            }
        }
        packet.mmmm = result56;
        const result70 = new Map();
        const size71 = buffer.readInt();
        if (size71 > 0) {
            for (let index72 = 0; index72 < size71; index72++) {
                const result73 = [];
                const size74 = buffer.readInt();
                if (size74 > 0) {
                    for (let index75 = 0; index75 < size74; index75++) {
                        const map76 = buffer.readIntStringMap();
                        result73.push(map76);
                    }
                }
                const result77 = new Set();
                const size78 = buffer.readInt();
                if (size78 > 0) {
                    for (let index79 = 0; index79 < size78; index79++) {
                        const map80 = buffer.readIntStringMap();
                        result77.add(map80);
                    }
                }
                result70.set(result73, result77);
            }
        }
        packet.mmmmm = result70;
        const set81 = buffer.readIntSet();
        packet.s = set81;
        const result82 = new Set();
        const size83 = buffer.readInt();
        if (size83 > 0) {
            for (let index84 = 0; index84 < size83; index84++) {
                const result85 = new Set();
                const size86 = buffer.readInt();
                if (size86 > 0) {
                    for (let index87 = 0; index87 < size86; index87++) {
                        const list88 = buffer.readIntList();
                        result85.add(list88);
                    }
                }
                result82.add(result85);
            }
        }
        packet.ss = result82;
        const result89 = new Set();
        const size90 = buffer.readInt();
        if (size90 > 0) {
            for (let index91 = 0; index91 < size90; index91++) {
                const set92 = buffer.readPacketSet(102);
                result89.add(set92);
            }
        }
        packet.sss = result89;
        const set93 = buffer.readStringSet();
        packet.ssss = set93;
        const result94 = new Set();
        const size95 = buffer.readInt();
        if (size95 > 0) {
            for (let index96 = 0; index96 < size95; index96++) {
                const map97 = buffer.readIntStringMap();
                result94.add(map97);
            }
        }
        packet.sssss = result94;
        if (buffer.compatibleRead(beforeReadIndex, length)) {
            const result98 = buffer.readInt();
            packet.myCompatible = result98;
        }
        if (buffer.compatibleRead(beforeReadIndex, length)) {
            const result99 = buffer.readPacket(102);
            packet.myObject = result99;
        }
        if (length > 0) {
            buffer.setReadOffset(beforeReadIndex + length);
        }
        return packet;
    }
}
export default ComplexObject;