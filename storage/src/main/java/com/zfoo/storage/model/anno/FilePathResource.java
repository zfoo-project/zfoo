package com.zfoo.storage.model.anno;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 文件路径资源注解
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface FilePathResource {
    /**
     * 指定对应的资源文件路径，路径为文件路径，路径前缀必须添加"file:"，并且可以为绝对路径或者相对路径
     * 若未指定文件路径，则通过配置xml文件中reource标签扫描与类同名文件，并且只在前缀为"file:"的路径中扫描
     * @return
     */
    @AliasFor("path")
    String value() default "";
    @AliasFor("value")
    String path() default "";
}
