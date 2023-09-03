#ifndef ZFOO_OBJECTA_H
#define ZFOO_OBJECTA_H

#include "cppProtocol/ByteBuffer.h"
#include "cppProtocol/Packet/ObjectB.h"

namespace zfoo {

    
    class ObjectA : public IProtocol {
    public:
        int32_t a;
        map<int32_t, string> m;
        ObjectB objectB;

        ~ObjectA() override = default;

        static ObjectA valueOf(int32_t a, map<int32_t, string> m, ObjectB objectB) {
            auto packet = ObjectA();
            packet.a = a;
            packet.m = m;
            packet.objectB = objectB;
            return packet;
        }

        int16_t protocolId() override {
            return 102;
        }

        bool operator<(const ObjectA &_) const {
            if (a < _.a) { return true; }
            if (_.a < a) { return false; }
            if (m < _.m) { return true; }
            if (_.m < m) { return false; }
            if (objectB < _.objectB) { return true; }
            if (_.objectB < objectB) { return false; }
            return false;
        }
    };


    class ObjectARegistration : public IProtocolRegistration {
    public:
        int16_t protocolId() override {
            return 102;
        }

        void write(ByteBuffer &buffer, IProtocol *packet) override {
            if (buffer.writePacketFlag(packet)) {
                return;
            }
            auto *message = (ObjectA *) packet;
            buffer.writeInt(message->a);
            buffer.writeIntStringMap(message->m);
            buffer.writePacket(&message->objectB, 103);
        }

        IProtocol *read(ByteBuffer &buffer) override {
            auto *packet = new ObjectA();
            if (!buffer.readBool()) {
                return packet;
            }
            int32_t result0 = buffer.readInt();
            packet->a = result0;
            auto map1 = buffer.readIntStringMap();
            packet->m = map1;
            auto result2 = buffer.readPacket(103);
            auto *result3 = (ObjectB *) result2.get();
            packet->objectB = *result3;
            return packet;
        }
    };
}

#endif
