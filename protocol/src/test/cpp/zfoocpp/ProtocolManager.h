#ifndef ZFOO_PROTOCOLMANAGER_H
#define ZFOO_PROTOCOLMANAGER_H

#include "ByteBuffer.h"
#include "zfoocpp/EmptyObject.h"
#include "zfoocpp/NormalObject.h"
#include "zfoocpp/ObjectA.h"
#include "zfoocpp/ObjectB.h"
#include "zfoocpp/SimpleObject.h"
namespace zfoo {

    const int16_t MAX_PROTOCOL_NUM = 32767;
    const IProtocolRegistration *protocols[MAX_PROTOCOL_NUM];

    void initProtocol() {
        protocols[0] = new EmptyObjectRegistration();
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