/*
 * Copyright (C) 2020 The zfoo Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.zfoo.protocol.javassist;

import javassist.*;
import org.junit.Ignore;
import org.junit.Test;

import java.lang.reflect.Modifier;
import java.lang.reflect.*;

/*
 测试javassist生成的类和普通用new关键字创建出来的类之间的区别<hr/>
 测试发现两者从访问方法和访问变量的速度几乎没有什么区别，可以用javassist代理其它的类，不会影响速度<hr/>
 */
@Ignore
public class JavassistTesting {


    public static final int INTERVAL_TIME = 1000;

    public static final int A_CONSTANT = 99999;
    public static final int B_CONSTANT = 888888888;
    public static final int C_CONSTANT = 7777777;



    /**
     * 直接访问public的访问速度
     */
    public void testA() {
        A a = new A();

        long start = System.currentTimeMillis();
        long count = 0;
        while (System.currentTimeMillis() - start <= INTERVAL_TIME) {
            a.a = A_CONSTANT;
            a.b = B_CONSTANT;
            a.c = C_CONSTANT;
            count++;
        }

        System.out.println(count);
    }

    /**
     * 用get和set方法访问成员变量的速度
     */
    public void testB() {
        B b = new B();

        long start = System.currentTimeMillis();
        long count = 0;
        while (System.currentTimeMillis() - start <= INTERVAL_TIME) {
            b.setA(A_CONSTANT);
            b.setB(B_CONSTANT);
            b.setC(C_CONSTANT);
            count++;
        }

        System.out.println(count);
    }

    /*
     通过反射直接操作属性访问变量的速度
     */
    public void testC() throws NoSuchFieldException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        Field aField = C.class.getDeclaredField("a");
        aField.setAccessible(true);
        Field bField = C.class.getDeclaredField("b");
        bField.setAccessible(true);
        Field cField = C.class.getDeclaredField("c");
        cField.setAccessible(true);

        Method aSetMethod = C.class.getDeclaredMethod("setA", int.class);
        aSetMethod.setAccessible(true);
        Method bSetMethod = C.class.getDeclaredMethod("setB", int.class);
        bSetMethod.setAccessible(true);
        Method cSetMethod = C.class.getDeclaredMethod("setC", int.class);
        cSetMethod.setAccessible(true);

        Constructor<?> constructor = C.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        C c = (C) constructor.newInstance();

        long start = System.currentTimeMillis();
        long count = 0;
        while (System.currentTimeMillis() - start <= INTERVAL_TIME) {
            //C c = C.class.newInstance();
            aField.set(c, A_CONSTANT);
            bField.set(c, B_CONSTANT);
            cField.set(c, C_CONSTANT);
            // aSetMethod.invoke(c, A_CONSTANT);
            // bSetMethod.invoke(c, B_CONSTANT);
            // cSetMethod.invoke(c, C_CONSTANT);
            count++;
        }

        System.out.println(count);
    }

    /*
     通过javassist访问成员变量的速度
     */
    @Test
    public void testD() throws NotFoundException, CannotCompileException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        ClassPool classPool = ClassPool.getDefault();

        CtClass ctClass = classPool.makeClass("com.zfoo.protocol.javassist.DGetClass");

        ctClass.addInterface(classPool.get(IDGet.class.getCanonicalName()));

        CtField a = new CtField(classPool.get(int.class.getCanonicalName()), "a", ctClass);
        a.setModifiers(Modifier.PRIVATE);
        ctClass.addField(a);

        CtField b = new CtField(classPool.get(int.class.getCanonicalName()), "b", ctClass);
        b.setModifiers(Modifier.PRIVATE);
        ctClass.addField(b);

        CtField c = new CtField(classPool.get(int.class.getCanonicalName()), "c", ctClass);
        c.setModifiers(Modifier.PRIVATE);
        ctClass.addField(c);

        CtConstructor ctConstructor = new CtConstructor(null, ctClass);
        ctConstructor.setBody("{}");
        ctClass.addConstructor(ctConstructor);

        CtMethod aGetMethod = new CtMethod(classPool.get(int.class.getCanonicalName()), "getA", null, ctClass);
        aGetMethod.setModifiers(Modifier.PUBLIC);
        StringBuilder aGetBuilder = new StringBuilder();
        aGetBuilder.append("{return this.a;}");
        aGetMethod.setBody(aGetBuilder.toString());
        ctClass.addMethod(aGetMethod);

        CtMethod aSetMethod = new CtMethod(classPool.get(void.class.getCanonicalName()), "setA", classPool.get(new String[]{int.class.getCanonicalName()}), ctClass);
        aGetMethod.setModifiers(Modifier.PUBLIC);
        StringBuilder aSetBuilder = new StringBuilder();
        aSetBuilder.append("{this.a=$1;}");
        aSetMethod.setBody(aSetBuilder.toString());
        ctClass.addMethod(aSetMethod);

        CtMethod bGetMethod = new CtMethod(classPool.get(int.class.getCanonicalName()), "getB", null, ctClass);
        bGetMethod.setModifiers(Modifier.PUBLIC);
        StringBuilder bGetBuilder = new StringBuilder();
        bGetBuilder.append("{return this.b;}");
        bGetMethod.setBody(bGetBuilder.toString());
        ctClass.addMethod(bGetMethod);

        CtMethod bSetMethod = new CtMethod(classPool.get(void.class.getCanonicalName()), "setB", classPool.get(new String[]{int.class.getCanonicalName()}), ctClass);
        aGetMethod.setModifiers(Modifier.PUBLIC);
        StringBuilder bSetBuilder = new StringBuilder();
        bSetBuilder.append("{this.b=$1;}");
        bSetMethod.setBody(bSetBuilder.toString());
        ctClass.addMethod(bSetMethod);

        CtMethod cGetMethod = new CtMethod(classPool.get(int.class.getCanonicalName()), "getC", null, ctClass);
        cGetMethod.setModifiers(Modifier.PUBLIC);
        StringBuilder cGetBuilder = new StringBuilder();
        cGetBuilder.append("{return this.c;}");
        cGetMethod.setBody(cGetBuilder.toString());
        ctClass.addMethod(cGetMethod);

        CtMethod cSetMethod = new CtMethod(classPool.get(void.class.getCanonicalName()), "setC", classPool.get(new String[]{int.class.getCanonicalName()}), ctClass);
        aGetMethod.setModifiers(Modifier.PUBLIC);
        StringBuilder cSetBuilder = new StringBuilder();
        cSetBuilder.append("{this.c=$1;}");
        cSetMethod.setBody(cSetBuilder.toString());
        ctClass.addMethod(cSetMethod);

        Class<?> clazz = ctClass.toClass(IDGet.class);
        Constructor<?> constructor = clazz.getConstructor();
        IDGet d = (IDGet) constructor.newInstance();

        long start = System.currentTimeMillis();
        long count = 0;
        while (System.currentTimeMillis() - start <= INTERVAL_TIME) {
            d.setA(A_CONSTANT);
            d.setB(B_CONSTANT);
            d.setC(C_CONSTANT);
            count++;
        }

        System.out.println(count);

        // d.setA(A_CONSTANT);
        // System.out.println(d.getA());
        // d.setB(B_CONSTANT);
        // System.out.println(d.getB());
        // d.setC(C_CONSTANT);
        // System.out.println(d.getC());
    }


    @Test
    public void testAll() throws NoSuchMethodException, IllegalAccessException, InstantiationException, CannotCompileException, NotFoundException, InvocationTargetException, NoSuchFieldException {
        testA();
        testB();
        testC();
        testD();
    }

}

