package com.zfoo.event.anno;

import org.springframework.aot.hint.annotation.Reflective;
import org.springframework.stereotype.Service;

import java.lang.annotation.*;

/**
 * 用于标记处理事件接口
 *
 * @author veione
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Reflective
@Service
public @interface EventService {
}
