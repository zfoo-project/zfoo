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

    /**
     * 将一个包下的协议文件生成在一个协议文件里
     */
    void mergerProtocol(List<IProtocolRegistration> registrations) throws IOException;

    /**
     * 折叠协议，生成协议文件会和Java源文件保持相同的目录结构
     */
    void foldProtocol(List<IProtocolRegistration> registrations) throws IOException;

    /**
     * 默认把所有的协议文件放在同一个更目录下，不做任何操作
     */
    void defaultProtocol(List<IProtocolRegistration> registrations) throws IOException;

}
