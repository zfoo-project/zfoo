package com.zfoo.protocol.serializer.anno;

import java.lang.annotation.*;

/**
 * @author meiw
 * @version 3.0
 * 
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface ProtocolField {

    String value() default "";

}
