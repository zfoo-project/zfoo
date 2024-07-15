#ifndef ZFOO_ObjectA
#define ZFOO_ObjectA

#include "zfoocpp/ByteBuffer.h"
#include "zfoocpp/Packet/ObjectB.h"
namespace zfoo {
    
    class ObjectA : public IProtocol {
    public:
        int32_t a;
        map<int32_t, string> m;
        ObjectB objectB;
        int32_t innerCompatibleValue;
    
        ~ObjectA() override = default;
    
        int16_t protocolId() override {
            return 102;
        }
    };

    class ObjectARegistration : public IProtocolRegistration {
    public:
        int16_t protocolId() override {
            return 102;
        }
    
        void write(ByteBuffer &buffer, IProtocol *packet) override {
            if (packet == nullptr) {
                buffer.writeInt(0);
                return;
            }
            auto *message = (ObjectA *) packet;
            auto beforeWriteIndex = buffer.getWriteOffset();
            buffer.writeInt(201);
            buffer.writeInt(message->a);
            buffer.writeIntStringMap(message->m);
            buffer.writePacket(&message->objectB, 103);
            buffer.writeInt(message->innerCompatibleValue);
            buffer.adjustPadding(201, beforeWriteIndex);
        }
    
        IProtocol *read(ByteBuffer &buffer) override {
            auto *packet = new ObjectA();
            auto length = buffer.readInt();
            if (length == 0) {
                return packet;
            }
            auto beforeReadIndex = buffer.getReadOffset();
            int32_t result0 = buffer.readInt();
            packet->a = result0;
            auto map1 = buffer.readIntStringMap();
            packet->m = map1;
            auto result2 = buffer.readPacket(103);
            auto *result3 = (ObjectB *) result2.get();
            packet->objectB = *result3;
            if (buffer.compatibleRead(beforeReadIndex, length)) {
                int32_t result4 = buffer.readInt();
                packet->innerCompatibleValue = result4;
            }
            if (length > 0) {
                buffer.setReadOffset(beforeReadIndex + length);
            }
            return packet;
        }
    };
}

#endif