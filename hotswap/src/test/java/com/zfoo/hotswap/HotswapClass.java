package com.zfoo.hotswap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author godotg
 */
public class HotswapClass {

    private static final Logger logger = LoggerFactory.getLogger(HotswapClass.class);

    public void print() {
        logger.info("没有热更新的输出内容");
    }

}
