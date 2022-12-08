package com.zfoo.storage.model.anno;

import java.lang.annotation.*;

/**
 * 在资源类字段中标注，映射excel中的列
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface ExcelColumn {
    /**
     * 指定excel文件中的列名
     * @return
     */
    String value() default "";

    /**
     * 指定excel文件中第几列
     * @return
     */
    int index() default -1;
    /**
     * 若二者都不指定，则自动匹配excel中与资源类字段名称一致的列
     */
}
