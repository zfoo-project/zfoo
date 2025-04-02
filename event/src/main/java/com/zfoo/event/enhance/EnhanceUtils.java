package com.zfoo.event.enhance;

// 注意：需要JDK 21+ 并启用预览功能

import com.zfoo.event.anno.Bus;
import com.zfoo.event.schema.NamespaceHandler;
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.protocol.util.UuidUtils;

import java.io.FileOutputStream;
import java.lang.classfile.ClassFile;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static java.lang.classfile.ClassFile.*;
import static java.lang.constant.ConstantDescs.CD_void;

public abstract class EnhanceUtils {
    private static final ClassDesc I_EVENT_RECEIVER = ClassDesc.of("com.zfoo.event.enhance.IEventReceiver");
    private static final ClassDesc I_EVENT = ClassDesc.of("com.zfoo.event.model.IEvent");
    private static final ClassDesc OBJECT = ClassDesc.of("java.lang.Object");

    public static IEventReceiver createEventReceiver(EventReceiverDefinition definition) throws Throwable {
        Class<?> beanClass = definition.getBean().getClass();
        Method method = definition.getMethod();
        Class<?> eventClass = definition.getEventClazz();
        Bus bus = definition.getBus();

        String className = EnhanceUtils.class.getName()
                + StringUtils.capitalize(NamespaceHandler.EVENT)
                + UuidUtils.getLocalIntId();

        ClassDesc targetClass = ClassDesc.of(className);
        ClassDesc beanType = ClassDesc.of(beanClass.getName());

        // 构建类文件
        byte[] bytecode = ClassFile.of().build(targetClass, classBuilder -> {
            classBuilder.withSuperclass(ClassDesc.of("java.lang.Object"))
                    .withInterfaceSymbols(I_EVENT_RECEIVER)

                    // 添加 final 字段 bean
                    .withField("bean", beanType, ACC_PRIVATE | ACC_FINAL)

                    // 添加构造器
                    .withMethod("<init>", MethodTypeDesc.of(CD_void, beanType), ACC_PUBLIC, codeBuilder -> {
                        codeBuilder.withCode(code -> {
                                code.aload(0)
                                    .invokespecial(ClassDesc.of("java.lang.Object"), "<init>", MethodTypeDesc.of(CD_void))
                                    .aload(0)
                                    .aload(1)
                                    .putfield(targetClass, "bean", beanType)
                                    .return_();
                        });
                    })

                    // 添加 invoke 方法
                    .withMethod("invoke", MethodTypeDesc.of(CD_void, I_EVENT), ACC_PUBLIC | ACC_FINAL, codeBuilder -> {
                        codeBuilder.withCode(code -> {
                            code.aload(0)
                                    .getfield(targetClass, "bean", beanType)
                                    .aload(1)
                                    .checkcast(ClassDesc.of(eventClass.getName()))
                                    .invokevirtual(beanType, method.getName(), MethodTypeDesc.of(CD_void, ClassDesc.of(eventClass.getName())))
                                    .return_();
                        });
                    })

                    // 添加 bus 方法
                    .withMethod("bus",
                            MethodTypeDesc.of(ClassDesc.of("com.zfoo.event.anno.Bus"))
                            ,
                            ACC_PUBLIC | ACC_FINAL,
                            methodBuilder -> methodBuilder.withCode(codeBuilder -> {
                                codeBuilder
                                        .getstatic(
                                                ClassDesc.of("com.zfoo.event.anno.Bus"),
                                                bus.name(),
                                                ClassDesc.of("com.zfoo.event.anno.Bus")
                                        )
                                        .areturn();
                            }))


                    // 添加 getBean 方法
                    .withMethod("getBean", MethodTypeDesc.of(OBJECT), ACC_PUBLIC | ACC_FINAL, codeBuilder -> {
                        codeBuilder.withCode(code -> {
                            code.aload(0)
                                    .getfield(targetClass, "bean", beanType)
                                    .areturn();
                        });
                    });
        });

        // 定义类
        MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(IEventReceiver.class, MethodHandles.lookup());
        Class<?> clazz = lookup.defineClass(bytecode);


        try {

            FileOutputStream fos = new FileOutputStream("d://" +className+ ".class");
            fos.write(bytecode);
            fos.close();
        } catch (Throwable throwable) {

        }

        // 创建实例
        Constructor<?> constructor = clazz.getConstructor(beanClass);
        return (IEventReceiver) constructor.newInstance(definition.getBean());
    }
}
