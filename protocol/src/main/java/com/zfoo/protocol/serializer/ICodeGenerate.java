package com.zfoo.protocol.serializer;

import com.zfoo.protocol.generate.GenerateOperation;
import com.zfoo.protocol.registration.IProtocolRegistration;

import java.io.IOException;
import java.util.List;

/**
 * @author godotg
 */
public interface ICodeGenerate {

    void init(GenerateOperation generateOperation);

    void mergerProtocol(List<IProtocolRegistration> registrations) throws IOException;

    void foldProtocol(List<IProtocolRegistration> registrations) throws IOException;

    void defaultProtocol(List<IProtocolRegistration> registrations) throws IOException;

}
