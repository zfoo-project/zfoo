package com.zfoo.storage.graalvm.feature;

import com.zfoo.protocol.util.ClassUtils;
import com.zfoo.storage.anno.GraalvmNative;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeSerialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用于注册graalvm lambda capture的类
 *
 * @author veione
 */
public class RuntimeRegistrationFeature implements Feature {
    private static final Logger log = LoggerFactory.getLogger(RuntimeRegistrationFeature.class);

    private static String lambdaCapturePackage;

    @Override
    public void duringSetup(DuringSetupAccess access) {
        log.info("Runtime registration feature on duringSetup");
        var filterClasses = ClassUtils.getClasses(lambdaCapturePackage, c -> c.isAnnotationPresent(GraalvmNative.class));
        filterClasses.forEach(cls -> {
            log.info("Starting register lambda capture class: {}", cls);
            RuntimeSerialization.registerLambdaCapturingClass(cls);
        });
    }

    public static void setLambdaCapturePackage(String lambdaCapturePackage) {
        RuntimeRegistrationFeature.lambdaCapturePackage = lambdaCapturePackage;
    }
}
