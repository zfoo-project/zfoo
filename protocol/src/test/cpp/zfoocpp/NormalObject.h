#ifndef ZFOO_NormalObject
#define ZFOO_NormalObject

#include "zfoocpp/ByteBuffer.h"
#include "zfoocpp/ObjectA.h"
#include "zfoocpp/ObjectB.h"
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
        int32_t outCompatibleValue;
        int32_t outCompatibleValue2;
    
        ~NormalObject() override = default;
    
        int16_t protocolId() override {
            return 101;
        }
    };

    class NormalObjectRegistration : public IProtocolRegistration {
    public:
        int16_t protocolId() override {
            return 101;
        }
    
        void write(ByteBuffer &buffer, IProtocol *packet) override {
            if (packet == nullptr) {
                buffer.writeInt(0);
                return;
            }
            auto *message = (NormalObject *) packet;
            auto beforeWriteIndex = buffer.writerIndex();
            buffer.writeInt(857);
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
            buffer.writeInt(message->outCompatibleValue);
            buffer.writeInt(message->outCompatibleValue2);
            buffer.adjustPadding(857, beforeWriteIndex);
        }
    
        IProtocol *read(ByteBuffer &buffer) override {
            auto *packet = new NormalObject();
            auto length = buffer.readInt();
            if (length == 0) {
                return packet;
            }
            auto beforeReadIndex = buffer.readerIndex();
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
            if (buffer.compatibleRead(beforeReadIndex, length)) {
                int32_t result19 = buffer.readInt();
                packet->outCompatibleValue = result19;
            }
            if (buffer.compatibleRead(beforeReadIndex, length)) {
                int32_t result20 = buffer.readInt();
                packet->outCompatibleValue2 = result20;
            }
            if (length > 0) {
                buffer.readerIndex(beforeReadIndex + length);
            }
            return packet;
        }
    };
}

#endif