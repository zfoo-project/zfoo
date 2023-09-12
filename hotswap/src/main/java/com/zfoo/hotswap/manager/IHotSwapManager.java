package com.zfoo.hotswap.manager;

import com.zfoo.hotswap.model.ClassFileDef;

import java.util.Map;

/**
 * @author godotg
 */
public interface IHotSwapManager {

    Map<String, ClassFileDef> getClassFileDefMap();

}
