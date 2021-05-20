package com.zfoo.hotswap.manager;

import com.zfoo.hotswap.model.ClassFileDef;

import java.util.Map;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public interface IHotSwapManager {

    Map<String, ClassFileDef> getClassFileDefMap();

}
