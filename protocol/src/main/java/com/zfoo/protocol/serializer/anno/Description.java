package com.zfoo.protocol.serializer.anno;

import java.lang.annotation.*;

/**
 * @author meiw
 * @version 3.0
 * 
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface Description {

    String value() default "";

}
