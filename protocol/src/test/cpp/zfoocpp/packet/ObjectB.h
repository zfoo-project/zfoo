#ifndef ZFOO_OBJECTB_H
#define ZFOO_OBJECTB_H

#include "zfoocpp/ByteBuffer.h"

namespace zfoo {

    
    class ObjectB : public IProtocol {
    public:
        bool flag;
        int32_t innerCompatibleValue;

        ~ObjectB() override = default;

        static ObjectB valueOf(bool flag, int32_t innerCompatibleValue) {
            auto packet = ObjectB();
            packet.flag = flag;
            packet.innerCompatibleValue = innerCompatibleValue;
            return packet;
        }

        int16_t protocolId() override {
            return 103;
        }

        bool operator<(const ObjectB &_) const {
            if (flag < _.flag) { return true; }
            if (_.flag < flag) { return false; }
            if (innerCompatibleValue < _.innerCompatibleValue) { return true; }
            if (_.innerCompatibleValue < innerCompatibleValue) { return false; }
            return false;
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
