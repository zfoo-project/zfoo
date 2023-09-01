package com.zfoo.util;


import java.util.List;

/**
 * @author godotg
 */
public abstract class GraalVmUtils {

    private static final boolean GRAALVM;

    static {
        var properties = List.of(System.getProperty("java.vm.name", "")
                , System.getProperty("java.vm.version", "")
                , System.getProperty("java.vendor.version", "")
                , System.getProperty("java.vendor.url", "")
                , System.getProperty("org.graalvm.nativeimage.imagecode", "")
        );
        GRAALVM = properties.stream().map(it -> it.toLowerCase()).anyMatch(it -> it.contains("graalvm"));
    }

    public static boolean isGraalVM() {
        return GRAALVM;
    }

}
