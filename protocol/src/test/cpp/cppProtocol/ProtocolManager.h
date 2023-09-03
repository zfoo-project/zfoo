#ifndef ZFOO_PROTOCOLMANAGER_H
#define ZFOO_PROTOCOLMANAGER_H

#include "ByteBuffer.h"
#include "cppProtocol/packet/EmptyObject.h"
#include "cppProtocol/packet/VeryBigObject.h"
#include "cppProtocol/packet/ComplexObject.h"
#include "cppProtocol/packet/NormalObject.h"
#include "cppProtocol/packet/ObjectA.h"
#include "cppProtocol/packet/ObjectB.h"
#include "cppProtocol/packet/SimpleObject.h"

namespace zfoo {

    const int16_t MAX_PROTOCOL_NUM = 32767;
    const IProtocolRegistration *protocols[MAX_PROTOCOL_NUM];

    void initProtocol() {
        protocols[0] = new EmptyObjectRegistration();
        protocols[1] = new VeryBigObjectRegistration();
        protocols[100] = new ComplexObjectRegistration();
        protocols[101] = new NormalObjectRegistration();
        protocols[102] = new ObjectARegistration();
        protocols[103] = new ObjectBRegistration();
        protocols[104] = new SimpleObjectRegistration();
    }

    inline IProtocolRegistration *getProtocol(int16_t protocolId) {
        return const_cast<IProtocolRegistration *>(protocols[protocolId]);
    }

    void write(ByteBuffer &buffer, IProtocol *packet) {
        auto protocolId = packet->protocolId();
        // 写入协议号
        buffer.writeShort(protocolId);
        // 写入包体
        getProtocol(protocolId)->write(buffer, packet);
    }

    IProtocol *read(ByteBuffer &buffer) {
        auto protocolId = buffer.readShort();
        return getProtocol(protocolId)->read(buffer);
    }

}
#endif
