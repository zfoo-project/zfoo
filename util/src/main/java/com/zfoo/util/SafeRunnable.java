package com.zfoo.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author godotg
 * @version 3.0
 */
public class SafeRunnable implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(SafeRunnable.class);

    private Runnable runnable;

    private SafeRunnable() {
    }

    public static SafeRunnable valueOf(Runnable runnable) {
        var run = new SafeRunnable();
        run.runnable = runnable;
        return run;
    }

    @Override
    public void run() {
        try {
            runnable.run();
        } catch (Exception e) {
            logger.error("unknown exception", e);
        } catch (Throwable t) {
            logger.error("unknown error", t);
        }
    }

}
