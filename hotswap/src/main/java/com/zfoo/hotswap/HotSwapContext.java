package com.zfoo.hotswap;

import com.zfoo.hotswap.manager.HotSwapManager;
import com.zfoo.hotswap.service.HotSwapServiceMBean;

/**
 * @author godotg
 * @version 3.0
 */
public class HotSwapContext {

    private static final HotSwapContext HOT_SWAP_CONTEXT = new HotSwapContext();

    private HotSwapContext() {

    }

    public static HotSwapServiceMBean getHotSwapService() {
        return HotSwapServiceMBean.getSingleInstance();
    }

    public static HotSwapManager getHotSwapManager() {
        return HotSwapManager.getInstance();
    }

}
