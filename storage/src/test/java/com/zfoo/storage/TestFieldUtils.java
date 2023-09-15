package com.zfoo.storage;

import com.zfoo.protocol.util.FieldUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author veione
 * @version 1.0.0
 */
public class TestFieldUtils {

    @Test
    public void testPropertyName() {
        //前三种主要用于普通的POJO,第四种用于是record类型,方法名就是属性名
        String getName = FieldUtils.methodToProperty("getName");
        assertEquals(getName, "name");
        String setName = FieldUtils.methodToProperty("setName");
        assertEquals(setName, "name");
        String isName = FieldUtils.methodToProperty("isName");
        assertEquals(isName, "name");
        String name = FieldUtils.methodToProperty("name");
        assertEquals(name, "name");
        name = FieldUtils.methodToProperty("Name");
        assertEquals(name, "name");
    }
}
