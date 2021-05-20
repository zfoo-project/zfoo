package com.zfoo.net.task.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public abstract class SafeRunnable implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(SafeRunnable.class);

    @Override
    public void run() {
        try {
            doRun();
        } catch (Exception e) {
            logger.error("未知exception异常", e);
        } catch (Throwable t) {
            logger.error("未知throwable异常", t);
        }
    }

    public abstract void doRun();

}
