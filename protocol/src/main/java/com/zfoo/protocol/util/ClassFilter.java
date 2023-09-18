package com.zfoo.protocol.util;

/**
 * 类扫描过滤器
 *
 * @author veione
 */
@FunctionalInterface
public interface ClassFilter {

    /**
     * 是否满足条件
     */
    boolean accept(Class<?> clazz);
}
