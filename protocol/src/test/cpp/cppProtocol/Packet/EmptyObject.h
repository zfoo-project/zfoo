#ifndef ZFOO_EMPTYOBJECT_H
#define ZFOO_EMPTYOBJECT_H

#include "cppProtocol/ByteBuffer.h"

namespace zfoo {

    
    class EmptyObject : public IProtocol {
    public:
        

        ~EmptyObject() override = default;

        static EmptyObject valueOf() {
            auto packet = EmptyObject();
            
            return packet;
        }

        int16_t protocolId() override {
            return 0;
        }

        bool operator<(const EmptyObject &_) const {
            
            return false;
        }
    };


    class EmptyObjectRegistration : public IProtocolRegistration {
    public:
        int16_t protocolId() override {
            return 0;
        }

        void write(ByteBuffer &buffer, IProtocol *packet) override {
            if (buffer.writePacketFlag(packet)) {
                return;
            }
            auto *message = (EmptyObject *) packet;
            
        }

        IProtocol *read(ByteBuffer &buffer) override {
            auto *packet = new EmptyObject();
            if (!buffer.readBool()) {
                return packet;
            }
            
            return packet;
        }
    };
}

#endif
