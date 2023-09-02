package com.zfoo.storage.model.anno;

import org.springframework.aot.hint.annotation.Reflective;

import java.lang.annotation.*;

/**
 * EN: Specify the file column name. If not specified, the default column name will be consistent with the field name.
 * CN: 指定文件列名，不指定则默认列名与字段名一致
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Reflective
public @interface ExcelFieldName {
    String value();
}
