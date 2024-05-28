#ifndef ZFOO_ObjectB
#define ZFOO_ObjectB

#include "zfoocpp/ByteBuffer.h"

namespace zfoo {
    
    class ObjectB : public IProtocol {
    public:
        bool flag;
        int32_t innerCompatibleValue;
    
        ~ObjectB() override = default;
    
        int16_t protocolId() override {
            return 103;
        }
    };

    class ObjectBRegistration : public IProtocolRegistration {
    public:
        int16_t protocolId() override {
            return 103;
        }
    
        void write(ByteBuffer &buffer, IProtocol *packet) override {
            if (packet == nullptr) {
                buffer.writeInt(0);
                return;
            }
            auto *message = (ObjectB *) packet;
            auto beforeWriteIndex = buffer.writerIndex();
            buffer.writeInt(4);
            buffer.writeBool(message->flag);
            buffer.writeInt(message->innerCompatibleValue);
            buffer.adjustPadding(4, beforeWriteIndex);
        }
    
        IProtocol *read(ByteBuffer &buffer) override {
            auto *packet = new ObjectB();
            auto length = buffer.readInt();
            if (length == 0) {
                return packet;
            }
            auto beforeReadIndex = buffer.readerIndex();
            bool result0 = buffer.readBool();
            packet->flag = result0;
            if (buffer.compatibleRead(beforeReadIndex, length)) {
                int32_t result1 = buffer.readInt();
                packet->innerCompatibleValue = result1;
            }
            if (length > 0) {
                buffer.readerIndex(beforeReadIndex + length);
            }
            return packet;
        }
    };
}

#endif