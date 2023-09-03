#ifndef ZFOO_NORMALOBJECT_H
#define ZFOO_NORMALOBJECT_H

#include "cppProtocol/ByteBuffer.h"
#include "cppProtocol/Packet/ObjectA.h"
#include "cppProtocol/Packet/ObjectB.h"

namespace zfoo {

    
    class NormalObject : public IProtocol {
    public:
        int8_t a;
        vector<int8_t> aaa;
        int16_t b;
        int32_t c;
        int64_t d;
        float e;
        double f;
        bool g;
        string jj;
        ObjectA kk;
        list<int32_t> l;
        list<int64_t> ll;
        list<ObjectA> lll;
        list<string> llll;
        map<int32_t, string> m;
        map<int32_t, ObjectA> mm;
        set<int32_t> s;
        set<string> ssss;

        ~NormalObject() override = default;

        static NormalObject valueOf(int8_t a, vector<int8_t> aaa, int16_t b, int32_t c, int64_t d, float e, double f, bool g, string jj, ObjectA kk, list<int32_t> l, list<int64_t> ll, list<ObjectA> lll, list<string> llll, map<int32_t, string> m, map<int32_t, ObjectA> mm, set<int32_t> s, set<string> ssss) {
            auto packet = NormalObject();
            packet.a = a;
            packet.aaa = aaa;
            packet.b = b;
            packet.c = c;
            packet.d = d;
            packet.e = e;
            packet.f = f;
            packet.g = g;
            packet.jj = jj;
            packet.kk = kk;
            packet.l = l;
            packet.ll = ll;
            packet.lll = lll;
            packet.llll = llll;
            packet.m = m;
            packet.mm = mm;
            packet.s = s;
            packet.ssss = ssss;
            return packet;
        }

        int16_t protocolId() override {
            return 101;
        }

        bool operator<(const NormalObject &_) const {
            if (a < _.a) { return true; }
            if (_.a < a) { return false; }
            if (aaa < _.aaa) { return true; }
            if (_.aaa < aaa) { return false; }
            if (b < _.b) { return true; }
            if (_.b < b) { return false; }
            if (c < _.c) { return true; }
            if (_.c < c) { return false; }
            if (d < _.d) { return true; }
            if (_.d < d) { return false; }
            if (e < _.e) { return true; }
            if (_.e < e) { return false; }
            if (f < _.f) { return true; }
            if (_.f < f) { return false; }
            if (g < _.g) { return true; }
            if (_.g < g) { return false; }
            if (jj < _.jj) { return true; }
            if (_.jj < jj) { return false; }
            if (kk < _.kk) { return true; }
            if (_.kk < kk) { return false; }
            if (l < _.l) { return true; }
            if (_.l < l) { return false; }
            if (ll < _.ll) { return true; }
            if (_.ll < ll) { return false; }
            if (lll < _.lll) { return true; }
            if (_.lll < lll) { return false; }
            if (llll < _.llll) { return true; }
            if (_.llll < llll) { return false; }
            if (m < _.m) { return true; }
            if (_.m < m) { return false; }
            if (mm < _.mm) { return true; }
            if (_.mm < mm) { return false; }
            if (s < _.s) { return true; }
            if (_.s < s) { return false; }
            if (ssss < _.ssss) { return true; }
            if (_.ssss < ssss) { return false; }
            return false;
        }
    };


    class NormalObjectRegistration : public IProtocolRegistration {
    public:
        int16_t protocolId() override {
            return 101;
        }

        void write(ByteBuffer &buffer, IProtocol *packet) override {
            if (buffer.writePacketFlag(packet)) {
                return;
            }
            auto *message = (NormalObject *) packet;
            buffer.writeByte(message->a);
            buffer.writeByteArray(message->aaa);
            buffer.writeShort(message->b);
            buffer.writeInt(message->c);
            buffer.writeLong(message->d);
            buffer.writeFloat(message->e);
            buffer.writeDouble(message->f);
            buffer.writeBool(message->g);
            buffer.writeString(message->jj);
            buffer.writePacket(&message->kk, 102);
            buffer.writeIntList(message->l);
            buffer.writeLongList(message->ll);
            buffer.writePacketList(message->lll, 102);
            buffer.writeStringList(message->llll);
            buffer.writeIntStringMap(message->m);
            buffer.writeIntPacketMap(message->mm, 102);
            buffer.writeIntSet(message->s);
            buffer.writeStringSet(message->ssss);
        }

        IProtocol *read(ByteBuffer &buffer) override {
            auto *packet = new NormalObject();
            if (!buffer.readBool()) {
                return packet;
            }
            int8_t result0 = buffer.readByte();
            packet->a = result0;
            auto array1 = buffer.readByteArray();
            packet->aaa = array1;
            auto result2 = buffer.readShort();
            packet->b = result2;
            int32_t result3 = buffer.readInt();
            packet->c = result3;
            auto result4 = buffer.readLong();
            packet->d = result4;
            float result5 = buffer.readFloat();
            packet->e = result5;
            double result6 = buffer.readDouble();
            packet->f = result6;
            bool result7 = buffer.readBool();
            packet->g = result7;
            auto result8 = buffer.readString();
            packet->jj = result8;
            auto result9 = buffer.readPacket(102);
            auto *result10 = (ObjectA *) result9.get();
            packet->kk = *result10;
            auto list11 = buffer.readIntList();
            packet->l = list11;
            auto list12 = buffer.readLongList();
            packet->ll = list12;
            auto list13 = buffer.readPacketList<ObjectA>(102);
            packet->lll = list13;
            auto list14 = buffer.readStringList();
            packet->llll = list14;
            auto map15 = buffer.readIntStringMap();
            packet->m = map15;
            auto map16 = buffer.readIntPacketMap<ObjectA>(102);
            packet->mm = map16;
            auto set17 = buffer.readIntSet();
            packet->s = set17;
            auto set18 = buffer.readStringSet();
            packet->ssss = set18;
            return packet;
        }
    };
}

#endif
