package com.zfoo.protocol.registration.anno;

import java.lang.annotation.*;

/**
 * @author meiw
 * @version 3.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Note {

    String value() default "";

}
