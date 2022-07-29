package com.zfoo.protocol.serializer.anno;

import java.lang.annotation.*;

/**
 * @author meiw
 * @version 3.0
 * 
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ProtocolClass {

    short protocolId() default 0;

    String description() default "";

}
