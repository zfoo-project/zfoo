package com.zfoo.storage.model.anno;

import java.lang.annotation.*;

/**
 * 指定文件列名，不指定则默认列名与字段名一致
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface ExcelFieldName {
    String value();
}
