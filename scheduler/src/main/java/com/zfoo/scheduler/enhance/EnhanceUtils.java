package com.zfoo.scheduler.enhance;

import com.zfoo.protocol.util.StringUtils;
import com.zfoo.protocol.util.UuidUtils;
import com.zfoo.scheduler.schema.NamespaceHandler;

import java.io.FileOutputStream;
import java.lang.classfile.*;
import java.lang.constant.*;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static java.lang.classfile.ClassFile.*;
import static java.lang.constant.ConstantDescs.CD_void;

public abstract class EnhanceUtils {

    private static final ClassDesc I_SCHEDULER_RECEIVER = ClassDesc.of("com.zfoo.scheduler.enhance.IScheduler");
    private static final MethodTypeDesc VOID_METHOD = MethodTypeDesc.ofDescriptor("()V");
    private static final ClassDesc OBJECT = ClassDesc.of("java.lang.Object");

    public static IScheduler createScheduler(ReflectScheduler reflectScheduler) throws Throwable {
        Object bean = reflectScheduler.getBean();
        Method method = reflectScheduler.getMethod();
        Class<?> beanClass = bean.getClass();

        // 1. 定义类名和接口
        String className = EnhanceUtils.class.getName()
                + StringUtils.capitalize(NamespaceHandler.SCHEDULER)
                + UuidUtils.getLocalIntId();

        // 2. 构建类文件模型
        ClassDesc targetClass = ClassDesc.of(className);
        ClassDesc beanType = ClassDesc.of(beanClass.getName());

        // 构建类文件
        byte[] bytecode = ClassFile.of().build(targetClass, classBuilder -> {
            classBuilder.withSuperclass(OBJECT)
                    .withInterfaceSymbols(I_SCHEDULER_RECEIVER)

                    // 添加 final 字段 bean
                    .withField("bean", beanType, ACC_PRIVATE | ACC_FINAL)

                    // 添加构造器
                    .withMethod("<init>", MethodTypeDesc.of(CD_void, beanType), ACC_PUBLIC, codeBuilder -> {
                        codeBuilder.withCode(code -> {
                            code.aload(0)
                                    .invokespecial(OBJECT, "<init>", MethodTypeDesc.of(CD_void))
                                    .aload(0)
                                    .aload(1)
                                    .putfield(targetClass, "bean", beanType)
                                    .return_();
                        });
                    })

                    // 添加 invoke 方法
                    .withMethod("invoke", MethodTypeDesc.of(CD_void), ACC_PUBLIC | ACC_FINAL, codeBuilder -> {
                        codeBuilder.withCode(code -> {
                            code.aload(0)
                                    .getfield(targetClass, "bean", beanType)
                                    .invokevirtual(beanType, method.getName(), MethodTypeDesc.of(CD_void))
                                    .return_();
                        });
                    });

        });



        // 定义类
        MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(IScheduler.class, MethodHandles.lookup());
        Class<?> clazz = lookup.defineClass(bytecode);

        // 8. 实例化对象
        Constructor<?> constructor = clazz.getConstructor(beanClass);
        return (IScheduler) constructor.newInstance(bean);
    }
}