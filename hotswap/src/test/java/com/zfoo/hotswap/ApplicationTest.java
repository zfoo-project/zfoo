package com.zfoo.hotswap;

import com.zfoo.hotswap.util.HotSwapUtils;
import com.zfoo.protocol.util.ClassUtils;
import com.zfoo.protocol.util.IOUtils;
import org.junit.Test;

import java.io.IOException;

/**
 * @author godotg
 */
public class ApplicationTest {

    // Hot-swap tutorial: add JVM arg -Djdk.attach.allowAttachSelf=true; without it, Byte Buddy hot-swap is used instead of Javassist
    // Javassist hot-swap is lighter; if it fails, Byte Buddy is used automatically
    @Test
    public void startHotSwapTest() throws IOException {
        // Hot-swap limitation: cannot add or remove methods/fields; only internal logic may be changed
        var test = new HotswapClass();
        // Output before hot-swap
        test.print();
        // Modify the print method, then compile it to a .class file to be hot-swapped
        HotSwapUtils.hotswapClass(IOUtils.toByteArray(ClassUtils.getFileFromClassPath("HotswapClass.class")));
        // Output after hot-swap
        test.print();
    }

}

