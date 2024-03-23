package com.zfoo.event;

import java.lang.reflect.Method;

/**
 * 订阅者抛出异常上下文
 *
 * @param event            事件对象
 * @param subscriber       订阅者
 * @param subscriberMethod 订阅方法
 */
public record EventExceptionContext(Object event, Object subscriber, Method subscriberMethod) {
}
