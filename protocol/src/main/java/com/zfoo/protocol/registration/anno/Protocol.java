package com.zfoo.protocol.registration.anno;

import java.lang.annotation.*;

/**
 * @author meiw
 * @version 3.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Protocol {

    short id() default 0;

    String note() default "";

}
