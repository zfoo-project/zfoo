package com.zfoo.protocol.serializer;

import com.zfoo.protocol.generate.GenerateOperation;
import com.zfoo.protocol.registration.ProtocolRegistration;

import java.io.IOException;
import java.util.List;

/**
 * 协议修改过后的测试流程：
 * 1.测试Java的老协议和新协议的兼容性，NormalObject/ObjectA/ObjectB有几个兼容字段
 * 2.测试Java在字节码增强，CutDown，反射三种情况下的代码下的协议解析的准确性
 * 3.测试resource/compatible中有5种协议兼容情况需要测试
 * 4.1/2/3总共有2*3*5=30种情况
 * <p>
 * 协议生成修改后的测试流程：
 * 1.测试老协议和新协议的兼容性，NormalObject/ObjectA/ObjectB有几个兼容字段
 * 2.测试普通生成，CutDown两种种情况下的代码下的协议解析的准确性
 * 3.测试merge，fold，default三种种生成方式
 * 4.测试resource/compatible中有5种协议兼容情况需要测试
 * 5.1/2/3/4总共2*2*3*5=60种情况
 * 6.简单的测试流程，NormalObject/ObjectA打开兼容字段，因为生成都是走的同一个方法直接测试非CutDown的fold
 *
 * @author godotg
 */
public interface ICodeGenerate {

    void init(GenerateOperation generateOperation);

    /**
     * 将一个包下的协议文件生成在一个协议文件里
     */
    void mergerProtocol(List<ProtocolRegistration> registrations) throws IOException;

    /**
     * 折叠协议，生成协议文件会和Java源文件保持相同的目录结构
     */
    void foldProtocol(List<ProtocolRegistration> registrations) throws IOException;

    /**
     * 默认把所有的协议文件放在同一个更目录下，不做任何操作
     */
    void defaultProtocol(List<ProtocolRegistration> registrations) throws IOException;

}
