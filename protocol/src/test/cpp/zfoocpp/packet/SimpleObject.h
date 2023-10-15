#ifndef ZFOO_SIMPLEOBJECT_H
#define ZFOO_SIMPLEOBJECT_H

#include "zfoocpp/ByteBuffer.h"

namespace zfoo {

    
    class SimpleObject : public IProtocol {
    public:
        int32_t c;
        bool g;

        ~SimpleObject() override = default;

        static SimpleObject valueOf(int32_t c, bool g) {
            auto packet = SimpleObject();
            packet.c = c;
            packet.g = g;
            return packet;
        }

        int16_t protocolId() override {
            return 104;
        }

        bool operator<(const SimpleObject &_) const {
            if (c < _.c) { return true; }
            if (_.c < c) { return false; }
            if (g < _.g) { return true; }
            if (_.g < g) { return false; }
            return false;
        }
    };


    class SimpleObjectRegistration : public IProtocolRegistration {
    public:
        int16_t protocolId() override {
            return 104;
        }

        void write(ByteBuffer &buffer, IProtocol *packet) override {
            if (packet == nullptr) {
                buffer.writeInt(0);
                return;
            }
            auto *message = (SimpleObject *) packet;
            buffer.writeInt(-1);
            buffer.writeInt(message->c);
            buffer.writeBool(message->g);
        }

        IProtocol *read(ByteBuffer &buffer) override {
            auto *packet = new SimpleObject();
            auto length = buffer.readInt();
            if (length == 0) {
                return packet;
            }
            auto beforeReadIndex = buffer.readerIndex();
            int32_t result0 = buffer.readInt();
            packet->c = result0;
            bool result1 = buffer.readBool();
            packet->g = result1;
            if (length > 0) {
                buffer.readerIndex(beforeReadIndex + length);
            }
            return packet;
        }
    };
}

#endif
