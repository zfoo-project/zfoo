#ifndef ZFOO_{}_H
#define ZFOO_{}_H

#include "{}/ByteBuffer.h"
{}
namespace zfoo {

    {}
    class {} : public IPacket {
    public:
        {}

        ~{}() override = default;

        static {} valueOf({}) {
            auto packet = {}();
            {}
            return packet;
        }

        int16_t protocolId() override {
            return {};
        }

        bool operator<(const {} &_) const {
            {}
            return false;
        }
    };


    class {}Registration : public IProtocolRegistration {
    public:
        int16_t protocolId() override {
            return {};
        }

        void write(ByteBuffer &buffer, IPacket *packet) override {
            if (buffer.writePacketFlag(packet)) {
                return;
            }
            auto *message = ({} *) packet;
            {}
        }

        IPacket *read(ByteBuffer &buffer) override {
            auto *packet = new {}();
            if (!buffer.readBool()) {
                return packet;
            }
            {}
            return packet;
        }
    };
}

#endif
