#ifndef ZFOO_PROTOCOLMANAGER_H
#define ZFOO_PROTOCOLMANAGER_H

#include "ByteBuffer.h"
{}
namespace zfoo {

    const int16_t MAX_PROTOCOL_NUM = 32767;
    const IProtocolRegistration *protocols[MAX_PROTOCOL_NUM];

    void initProtocol() {
        {}
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
