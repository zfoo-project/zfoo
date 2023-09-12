package com.zfoo.hotswap.manager;

import com.zfoo.hotswap.model.ClassFileDef;

import java.util.HashMap;
import java.util.Map;

/**
 * @author godotg
 */
public class HotSwapManager implements IHotSwapManager {

    private static final HotSwapManager HOT_SWAP_MANAGER = new HotSwapManager();

    private static final Map<String, ClassFileDef> classFileDefMap = new HashMap<>();

    public static volatile byte[] updateBytes;

    public static volatile Exception exception;

    private HotSwapManager() {
    }

    public static HotSwapManager getInstance() {
        return HOT_SWAP_MANAGER;
    }

    @Override
    public Map<String, ClassFileDef> getClassFileDefMap() {
        return classFileDefMap;
    }

}
