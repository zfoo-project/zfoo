#ifndef ZFOO_COMPLEXOBJECT_H
#define ZFOO_COMPLEXOBJECT_H

#include "zfoocpp/ByteBuffer.h"
#include "zfoocpp/Packet/ObjectA.h"
#include "zfoocpp/Packet/ObjectB.h"

namespace zfoo {

    // 复杂的对象，包括了各种复杂的结构，数组，List，Set，Map
    class ComplexObject : public IProtocol {
    public:
        // byte类型，最简单的整形
        int8_t a;
        // byte的包装类型，优先使用基础类型，包装类型会有装箱拆箱
        int8_t aa;
        // 数组类型
        vector<int8_t> aaa;
        vector<int8_t> aaaa;
        int16_t b;
        int16_t bb;
        vector<int16_t> bbb;
        vector<int16_t> bbbb;
        int32_t c;
        int32_t cc;
        vector<int32_t> ccc;
        vector<int32_t> cccc;
        int64_t d;
        int64_t dd;
        vector<int64_t> ddd;
        vector<int64_t> dddd;
        float e;
        float ee;
        vector<float> eee;
        vector<float> eeee;
        double f;
        double ff;
        vector<double> fff;
        vector<double> ffff;
        bool g;
        bool gg;
        vector<bool> ggg;
        vector<bool> gggg;
        string jj;
        vector<string> jjj;
        ObjectA kk;
        vector<ObjectA> kkk;
        list<int32_t> l;
        list<list<list<int32_t>>> ll;
        list<list<ObjectA>> lll;
        list<string> llll;
        list<map<int32_t, string>> lllll;
        map<int32_t, string> m;
        map<int32_t, ObjectA> mm;
        map<ObjectA, list<int32_t>> mmm;
        map<list<list<ObjectA>>, list<list<list<int32_t>>>> mmmm;
        map<list<map<int32_t, string>>, set<map<int32_t, string>>> mmmmm;
        set<int32_t> s;
        set<set<list<int32_t>>> ss;
        set<set<ObjectA>> sss;
        set<string> ssss;
        set<map<int32_t, string>> sssss;
        // 如果要修改协议并且兼容老协议，需要加上Compatible注解，按照增加的顺序添加order
        int32_t myCompatible;
        ObjectA myObject;

        ~ComplexObject() override = default;

        static ComplexObject valueOf(int8_t a, int8_t aa, vector<int8_t> aaa, vector<int8_t> aaaa, int16_t b, int16_t bb, vector<int16_t> bbb, vector<int16_t> bbbb, int32_t c, int32_t cc, vector<int32_t> ccc, vector<int32_t> cccc, int64_t d, int64_t dd, vector<int64_t> ddd, vector<int64_t> dddd, float e, float ee, vector<float> eee, vector<float> eeee, double f, double ff, vector<double> fff, vector<double> ffff, bool g, bool gg, vector<bool> ggg, vector<bool> gggg, string jj, vector<string> jjj, ObjectA kk, vector<ObjectA> kkk, list<int32_t> l, list<list<list<int32_t>>> ll, list<list<ObjectA>> lll, list<string> llll, list<map<int32_t, string>> lllll, map<int32_t, string> m, map<int32_t, ObjectA> mm, map<ObjectA, list<int32_t>> mmm, map<list<list<ObjectA>>, list<list<list<int32_t>>>> mmmm, map<list<map<int32_t, string>>, set<map<int32_t, string>>> mmmmm, set<int32_t> s, set<set<list<int32_t>>> ss, set<set<ObjectA>> sss, set<string> ssss, set<map<int32_t, string>> sssss, int32_t myCompatible, ObjectA myObject) {
            auto packet = ComplexObject();
            packet.a = a;
            packet.aa = aa;
            packet.aaa = aaa;
            packet.aaaa = aaaa;
            packet.b = b;
            packet.bb = bb;
            packet.bbb = bbb;
            packet.bbbb = bbbb;
            packet.c = c;
            packet.cc = cc;
            packet.ccc = ccc;
            packet.cccc = cccc;
            packet.d = d;
            packet.dd = dd;
            packet.ddd = ddd;
            packet.dddd = dddd;
            packet.e = e;
            packet.ee = ee;
            packet.eee = eee;
            packet.eeee = eeee;
            packet.f = f;
            packet.ff = ff;
            packet.fff = fff;
            packet.ffff = ffff;
            packet.g = g;
            packet.gg = gg;
            packet.ggg = ggg;
            packet.gggg = gggg;
            packet.jj = jj;
            packet.jjj = jjj;
            packet.kk = kk;
            packet.kkk = kkk;
            packet.l = l;
            packet.ll = ll;
            packet.lll = lll;
            packet.llll = llll;
            packet.lllll = lllll;
            packet.m = m;
            packet.mm = mm;
            packet.mmm = mmm;
            packet.mmmm = mmmm;
            packet.mmmmm = mmmmm;
            packet.s = s;
            packet.ss = ss;
            packet.sss = sss;
            packet.ssss = ssss;
            packet.sssss = sssss;
            packet.myCompatible = myCompatible;
            packet.myObject = myObject;
            return packet;
        }

        int16_t protocolId() override {
            return 100;
        }

        bool operator<(const ComplexObject &_) const {
            if (a < _.a) { return true; }
            if (_.a < a) { return false; }
            if (aa < _.aa) { return true; }
            if (_.aa < aa) { return false; }
            if (aaa < _.aaa) { return true; }
            if (_.aaa < aaa) { return false; }
            if (aaaa < _.aaaa) { return true; }
            if (_.aaaa < aaaa) { return false; }
            if (b < _.b) { return true; }
            if (_.b < b) { return false; }
            if (bb < _.bb) { return true; }
            if (_.bb < bb) { return false; }
            if (bbb < _.bbb) { return true; }
            if (_.bbb < bbb) { return false; }
            if (bbbb < _.bbbb) { return true; }
            if (_.bbbb < bbbb) { return false; }
            if (c < _.c) { return true; }
            if (_.c < c) { return false; }
            if (cc < _.cc) { return true; }
            if (_.cc < cc) { return false; }
            if (ccc < _.ccc) { return true; }
            if (_.ccc < ccc) { return false; }
            if (cccc < _.cccc) { return true; }
            if (_.cccc < cccc) { return false; }
            if (d < _.d) { return true; }
            if (_.d < d) { return false; }
            if (dd < _.dd) { return true; }
            if (_.dd < dd) { return false; }
            if (ddd < _.ddd) { return true; }
            if (_.ddd < ddd) { return false; }
            if (dddd < _.dddd) { return true; }
            if (_.dddd < dddd) { return false; }
            if (e < _.e) { return true; }
            if (_.e < e) { return false; }
            if (ee < _.ee) { return true; }
            if (_.ee < ee) { return false; }
            if (eee < _.eee) { return true; }
            if (_.eee < eee) { return false; }
            if (eeee < _.eeee) { return true; }
            if (_.eeee < eeee) { return false; }
            if (f < _.f) { return true; }
            if (_.f < f) { return false; }
            if (ff < _.ff) { return true; }
            if (_.ff < ff) { return false; }
            if (fff < _.fff) { return true; }
            if (_.fff < fff) { return false; }
            if (ffff < _.ffff) { return true; }
            if (_.ffff < ffff) { return false; }
            if (g < _.g) { return true; }
            if (_.g < g) { return false; }
            if (gg < _.gg) { return true; }
            if (_.gg < gg) { return false; }
            if (ggg < _.ggg) { return true; }
            if (_.ggg < ggg) { return false; }
            if (gggg < _.gggg) { return true; }
            if (_.gggg < gggg) { return false; }
            if (jj < _.jj) { return true; }
            if (_.jj < jj) { return false; }
            if (jjj < _.jjj) { return true; }
            if (_.jjj < jjj) { return false; }
            if (kk < _.kk) { return true; }
            if (_.kk < kk) { return false; }
            if (kkk < _.kkk) { return true; }
            if (_.kkk < kkk) { return false; }
            if (l < _.l) { return true; }
            if (_.l < l) { return false; }
            if (ll < _.ll) { return true; }
            if (_.ll < ll) { return false; }
            if (lll < _.lll) { return true; }
            if (_.lll < lll) { return false; }
            if (llll < _.llll) { return true; }
            if (_.llll < llll) { return false; }
            if (lllll < _.lllll) { return true; }
            if (_.lllll < lllll) { return false; }
            if (m < _.m) { return true; }
            if (_.m < m) { return false; }
            if (mm < _.mm) { return true; }
            if (_.mm < mm) { return false; }
            if (mmm < _.mmm) { return true; }
            if (_.mmm < mmm) { return false; }
            if (mmmm < _.mmmm) { return true; }
            if (_.mmmm < mmmm) { return false; }
            if (mmmmm < _.mmmmm) { return true; }
            if (_.mmmmm < mmmmm) { return false; }
            if (s < _.s) { return true; }
            if (_.s < s) { return false; }
            if (ss < _.ss) { return true; }
            if (_.ss < ss) { return false; }
            if (sss < _.sss) { return true; }
            if (_.sss < sss) { return false; }
            if (ssss < _.ssss) { return true; }
            if (_.ssss < ssss) { return false; }
            if (sssss < _.sssss) { return true; }
            if (_.sssss < sssss) { return false; }
            if (myCompatible < _.myCompatible) { return true; }
            if (_.myCompatible < myCompatible) { return false; }
            if (myObject < _.myObject) { return true; }
            if (_.myObject < myObject) { return false; }
            return false;
        }
    };


    class ComplexObjectRegistration : public IProtocolRegistration {
    public:
        int16_t protocolId() override {
            return 100;
        }

        void write(ByteBuffer &buffer, IProtocol *packet) override {
            if (packet == nullptr) {
                buffer.writeInt(0);
                return;
            }
            auto *message = (ComplexObject *) packet;
            auto beforeWriteIndex = buffer.writerIndex();
            buffer.writeInt(36962);
            buffer.writeByte(message->a);
            buffer.writeByte(message->aa);
            buffer.writeByteArray(message->aaa);
            buffer.writeByteArray(message->aaaa);
            buffer.writeShort(message->b);
            buffer.writeShort(message->bb);
            buffer.writeShortArray(message->bbb);
            buffer.writeShortArray(message->bbbb);
            buffer.writeInt(message->c);
            buffer.writeInt(message->cc);
            buffer.writeIntArray(message->ccc);
            buffer.writeIntArray(message->cccc);
            buffer.writeLong(message->d);
            buffer.writeLong(message->dd);
            buffer.writeLongArray(message->ddd);
            buffer.writeLongArray(message->dddd);
            buffer.writeFloat(message->e);
            buffer.writeFloat(message->ee);
            buffer.writeFloatArray(message->eee);
            buffer.writeFloatArray(message->eeee);
            buffer.writeDouble(message->f);
            buffer.writeDouble(message->ff);
            buffer.writeDoubleArray(message->fff);
            buffer.writeDoubleArray(message->ffff);
            buffer.writeBool(message->g);
            buffer.writeBool(message->gg);
            buffer.writeBooleanArray(message->ggg);
            buffer.writeBooleanArray(message->gggg);
            buffer.writeString(message->jj);
            buffer.writeStringArray(message->jjj);
            buffer.writePacket(&message->kk, 102);
            buffer.writePacketArray<ObjectA>(message->kkk, 102);
            buffer.writeIntList(message->l);
            buffer.writeInt(message->ll.size());
            for (auto i0 : message->ll) {
                buffer.writeInt(i0.size());
                for (auto i1 : i0) {
                    buffer.writeIntList(i1);
                }
            }
            buffer.writeInt(message->lll.size());
            for (auto i2 : message->lll) {
                buffer.writePacketList(i2, 102);
            }
            buffer.writeStringList(message->llll);
            buffer.writeInt(message->lllll.size());
            for (auto i3 : message->lllll) {
                buffer.writeIntStringMap(i3);
            }
            buffer.writeIntStringMap(message->m);
            buffer.writeIntPacketMap(message->mm, 102);
            buffer.writeInt(message->mmm.size());
            for (auto&[keyElement4, valueElement5] : message->mmm) {
                buffer.writePacket((IProtocol *) &keyElement4, 102);
                buffer.writeIntList(valueElement5);
            }
            buffer.writeInt(message->mmmm.size());
            for (auto&[keyElement6, valueElement7] : message->mmmm) {
                buffer.writeInt(keyElement6.size());
                for (auto i8 : keyElement6) {
                    buffer.writePacketList(i8, 102);
                }
                buffer.writeInt(valueElement7.size());
                for (auto i9 : valueElement7) {
                    buffer.writeInt(i9.size());
                    for (auto i10 : i9) {
                        buffer.writeIntList(i10);
                    }
                }
            }
            buffer.writeInt(message->mmmmm.size());
            for (auto&[keyElement11, valueElement12] : message->mmmmm) {
                buffer.writeInt(keyElement11.size());
                for (auto i13 : keyElement11) {
                    buffer.writeIntStringMap(i13);
                }
                buffer.writeInt(valueElement12.size());
                for (auto i14 : valueElement12) {
                    buffer.writeIntStringMap(i14);
                }
            }
            buffer.writeIntSet(message->s);
            buffer.writeInt(message->ss.size());
            for (auto i15 : message->ss) {
                buffer.writeInt(i15.size());
                for (auto i16 : i15) {
                    buffer.writeIntList(i16);
                }
            }
            buffer.writeInt(message->sss.size());
            for (auto i17 : message->sss) {
                buffer.writePacketSet(i17, 102);
            }
            buffer.writeStringSet(message->ssss);
            buffer.writeInt(message->sssss.size());
            for (auto i18 : message->sssss) {
                buffer.writeIntStringMap(i18);
            }
            buffer.writeInt(message->myCompatible);
            buffer.writePacket(&message->myObject, 102);
            buffer.adjustPadding(36962, beforeWriteIndex);
        }

        IProtocol *read(ByteBuffer &buffer) override {
            auto *packet = new ComplexObject();
            auto length = buffer.readInt();
            if (length == 0) {
                return packet;
            }
            auto beforeReadIndex = buffer.readerIndex();
            int8_t result19 = buffer.readByte();
            packet->a = result19;
            int8_t result20 = buffer.readByte();
            packet->aa = result20;
            auto array21 = buffer.readByteArray();
            packet->aaa = array21;
            auto array22 = buffer.readByteArray();
            packet->aaaa = array22;
            auto result23 = buffer.readShort();
            packet->b = result23;
            auto result24 = buffer.readShort();
            packet->bb = result24;
            auto array25 = buffer.readShortArray();
            packet->bbb = array25;
            auto array26 = buffer.readShortArray();
            packet->bbbb = array26;
            int32_t result27 = buffer.readInt();
            packet->c = result27;
            int32_t result28 = buffer.readInt();
            packet->cc = result28;
            auto array29 = buffer.readIntArray();
            packet->ccc = array29;
            auto array30 = buffer.readIntArray();
            packet->cccc = array30;
            auto result31 = buffer.readLong();
            packet->d = result31;
            auto result32 = buffer.readLong();
            packet->dd = result32;
            auto array33 = buffer.readLongArray();
            packet->ddd = array33;
            auto array34 = buffer.readLongArray();
            packet->dddd = array34;
            float result35 = buffer.readFloat();
            packet->e = result35;
            float result36 = buffer.readFloat();
            packet->ee = result36;
            auto array37 = buffer.readFloatArray();
            packet->eee = array37;
            auto array38 = buffer.readFloatArray();
            packet->eeee = array38;
            double result39 = buffer.readDouble();
            packet->f = result39;
            double result40 = buffer.readDouble();
            packet->ff = result40;
            auto array41 = buffer.readDoubleArray();
            packet->fff = array41;
            auto array42 = buffer.readDoubleArray();
            packet->ffff = array42;
            bool result43 = buffer.readBool();
            packet->g = result43;
            bool result44 = buffer.readBool();
            packet->gg = result44;
            auto array45 = buffer.readBooleanArray();
            packet->ggg = array45;
            auto array46 = buffer.readBooleanArray();
            packet->gggg = array46;
            auto result47 = buffer.readString();
            packet->jj = result47;
            auto array48 = buffer.readStringArray();
            packet->jjj = array48;
            auto result49 = buffer.readPacket(102);
            auto *result50 = (ObjectA *) result49.get();
            packet->kk = *result50;
            auto array51 = buffer.readPacketArray<ObjectA>(102);
            packet->kkk = array51;
            auto list52 = buffer.readIntList();
            packet->l = list52;
            int32_t size55 = buffer.readInt();
            list<list<list<int32_t>>> result53;
            for (int index54 = 0; index54 < size55; index54++) {
                int32_t size58 = buffer.readInt();
                list<list<int32_t>> result56;
                for (int index57 = 0; index57 < size58; index57++) {
                    auto list59 = buffer.readIntList();
                    result56.emplace_back(list59);
                }
                result53.emplace_back(result56);
            }
            packet->ll = result53;
            int32_t size62 = buffer.readInt();
            list<list<ObjectA>> result60;
            for (int index61 = 0; index61 < size62; index61++) {
                auto list63 = buffer.readPacketList<ObjectA>(102);
                result60.emplace_back(list63);
            }
            packet->lll = result60;
            auto list64 = buffer.readStringList();
            packet->llll = list64;
            int32_t size67 = buffer.readInt();
            list<map<int32_t, string>> result65;
            for (int index66 = 0; index66 < size67; index66++) {
                auto map68 = buffer.readIntStringMap();
                result65.emplace_back(map68);
            }
            packet->lllll = result65;
            auto map69 = buffer.readIntStringMap();
            packet->m = map69;
            auto map70 = buffer.readIntPacketMap<ObjectA>(102);
            packet->mm = map70;
            int32_t size72 = buffer.readInt();
            map<ObjectA, list<int32_t>> result71;
            for (auto index73 = 0; index73 < size72; index73++) {
                auto result74 = buffer.readPacket(102);
                auto *result75 = (ObjectA *) result74.get();
                auto list76 = buffer.readIntList();
                result71.insert(make_pair(*result75, list76));
            }
            packet->mmm = result71;
            int32_t size78 = buffer.readInt();
            map<list<list<ObjectA>>, list<list<list<int32_t>>>> result77;
            for (auto index79 = 0; index79 < size78; index79++) {
                int32_t size82 = buffer.readInt();
                list<list<ObjectA>> result80;
                for (int index81 = 0; index81 < size82; index81++) {
                    auto list83 = buffer.readPacketList<ObjectA>(102);
                    result80.emplace_back(list83);
                }
                int32_t size86 = buffer.readInt();
                list<list<list<int32_t>>> result84;
                for (int index85 = 0; index85 < size86; index85++) {
                    int32_t size89 = buffer.readInt();
                    list<list<int32_t>> result87;
                    for (int index88 = 0; index88 < size89; index88++) {
                        auto list90 = buffer.readIntList();
                        result87.emplace_back(list90);
                    }
                    result84.emplace_back(result87);
                }
                result77.insert(make_pair(result80, result84));
            }
            packet->mmmm = result77;
            int32_t size92 = buffer.readInt();
            map<list<map<int32_t, string>>, set<map<int32_t, string>>> result91;
            for (auto index93 = 0; index93 < size92; index93++) {
                int32_t size96 = buffer.readInt();
                list<map<int32_t, string>> result94;
                for (int index95 = 0; index95 < size96; index95++) {
                    auto map97 = buffer.readIntStringMap();
                    result94.emplace_back(map97);
                }
                int32_t size100 = buffer.readInt();
                set<map<int32_t, string>> result98;
                for (int index99 = 0; index99 < size100; index99++) {
                    auto map101 = buffer.readIntStringMap();
                    result98.emplace(map101);
                }
                result91.insert(make_pair(result94, result98));
            }
            packet->mmmmm = result91;
            auto set102 = buffer.readIntSet();
            packet->s = set102;
            int32_t size105 = buffer.readInt();
            set<set<list<int32_t>>> result103;
            for (int index104 = 0; index104 < size105; index104++) {
                int32_t size108 = buffer.readInt();
                set<list<int32_t>> result106;
                for (int index107 = 0; index107 < size108; index107++) {
                    auto list109 = buffer.readIntList();
                    result106.emplace(list109);
                }
                result103.emplace(result106);
            }
            packet->ss = result103;
            int32_t size112 = buffer.readInt();
            set<set<ObjectA>> result110;
            for (int index111 = 0; index111 < size112; index111++) {
                auto set113 = buffer.readPacketSet<ObjectA>(102);
                result110.emplace(set113);
            }
            packet->sss = result110;
            auto set114 = buffer.readStringSet();
            packet->ssss = set114;
            int32_t size117 = buffer.readInt();
            set<map<int32_t, string>> result115;
            for (int index116 = 0; index116 < size117; index116++) {
                auto map118 = buffer.readIntStringMap();
                result115.emplace(map118);
            }
            packet->sssss = result115;
            if (buffer.compatibleRead(beforeReadIndex, length)) {
                int32_t result119 = buffer.readInt();
                packet->myCompatible = result119;
            }
            if (buffer.compatibleRead(beforeReadIndex, length)) {
                auto result120 = buffer.readPacket(102);
                auto *result121 = (ObjectA *) result120.get();
                packet->myObject = *result121;
            }
            if (length > 0) {
                buffer.readerIndex(beforeReadIndex + length);
            }
            return packet;
        }
    };
}

#endif
