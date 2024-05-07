package com.zfoo.storage.utils.json;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.NullNode;

import java.io.Serializable;

/**
 * 空值.用于值为空
 */
@JsonSerialize(using = ValueSerializer.class)
@JsonDeserialize(using = ValueDeserializer.class)
public sealed interface NullValue extends Serializable permits NullImpl {
    static NullValue nullValue() {
        return NullImpl.NULL;
    }

    Value asValue();
}

record NullImpl() implements EmptyValue, NullValue {
    static final NullImpl NULL = new NullImpl();

    @Override
    public Value asValue() {
        return this;
    }

    @Override
    public String toString() {
        return NullNode.getInstance().toString();   // "null"
    }
}