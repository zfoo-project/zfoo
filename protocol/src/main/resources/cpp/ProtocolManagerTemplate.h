#ifndef ZFOO_PROTOCOLMANAGER_H
#define ZFOO_PROTOCOLMANAGER_H

#include "ByteBuffer.h"
${protocol_imports}
namespace zfoo {

    const int16_t MAX_PROTOCOL_NUM = 32767;
    const IProtocolRegistration *protocols[MAX_PROTOCOL_NUM];

    void initProtocol() {
        ${protocol_manager_registrations}
    }

    inline IProtocolRegistration *getProtocol(int16_t protocolId) {
        return const_cast<IProtocolRegistration *>(protocols[protocolId]);
    }

    void write(ByteBuffer &buffer, IProtocol *packet) {
        auto protocolId = packet->protocolId();
        // write protocol id to buffer
        buffer.writeShort(protocolId);
        // write packet
        getProtocol(protocolId)->write(buffer, packet);
    }

    IProtocol *read(ByteBuffer &buffer) {
        auto protocolId = buffer.readShort();
        return getProtocol(protocolId)->read(buffer);
    }

}
#endif
