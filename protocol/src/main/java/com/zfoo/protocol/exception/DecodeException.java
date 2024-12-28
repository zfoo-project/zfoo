package com.zfoo.protocol.exception;

import com.zfoo.protocol.util.StringUtils;

public class DecodeException extends RuntimeException {
    /**
     * msg id
     */
    private final short protocolId;

    public DecodeException(Throwable e, short protocolId) {
        super(e);
        this.protocolId = protocolId;
    }

    public short getProtocolId() {
        return protocolId;
    }

    @Override
    public String toString() {
        String sss = super.toString();
        return StringUtils.format("{},  protocolId={} ", sss, protocolId);
    }
}
