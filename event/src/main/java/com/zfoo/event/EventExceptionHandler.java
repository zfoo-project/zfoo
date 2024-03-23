package com.zfoo.event;

import com.zfoo.event.manager.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * 处理事件订阅者抛出异常
 *
 * @author veione
 */
@FunctionalInterface
public interface EventExceptionHandler {
    /**
     * 处理订阅者抛出的异常
     *
     * @param exception 异常对象
     * @param context   异常上下文
     */
    void handleException(Throwable exception, EventExceptionContext context);

    final class LoggingEventExceptionHandler implements EventExceptionHandler {
        public static final LoggingEventExceptionHandler INSTANCE = new LoggingEventExceptionHandler();
        private final Logger logger =  LoggerFactory.getLogger(EventBus.class.getName());

        @Override
        public void handleException(Throwable exception, EventExceptionContext context) {
            if (logger.isErrorEnabled()) {
                logger.error(message(context), exception);
            }
        }

        private static String message(EventExceptionContext context) {
            Method method = context.subscriberMethod();
            return "Exception thrown by subscriber method "
                    + method.getName()
                    + '('
                    + method.getParameterTypes()[0].getName()
                    + ')'
                    + " on subscriber "
                    + context.subscriber()
                    + " when dispatching event: "
                    + context.event();
        }
    }
}
