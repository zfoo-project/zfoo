package com.zfoo.storage;

import com.zfoo.storage.util.PropertyNamer;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author veione
 * @version 1.0.0
 */
public class TestPropertyNamer {

    @Test
    public void testPropertyName() {
        //前三种主要用于普通的POJO,第四种用于是record类型,方法名就是属性名
        String getName = PropertyNamer.methodToProperty("getName");
        assertEquals(getName, "name");
        String setName = PropertyNamer.methodToProperty("setName");
        assertEquals(setName, "name");
        String isName = PropertyNamer.methodToProperty("isName");
        assertEquals(isName, "name");
        String name = PropertyNamer.methodToProperty("name");
        assertEquals(name, "name");
        name = PropertyNamer.methodToProperty("Name");
        assertEquals(name, "name");
    }
}
