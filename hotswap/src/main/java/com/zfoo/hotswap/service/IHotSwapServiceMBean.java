package com.zfoo.hotswap.service;

import javax.management.MXBean;

/**
 * @author godotg
 */
@MXBean
public interface IHotSwapServiceMBean {

    /**
     * Hot-swap a file specified as a path relative to the project root
     *
     * @param relativePath relative path
     */
    void hotSwapByRelativePath(String relativePath);

    /**
     * Hot-swap a file specified by absolute path
     *
     * @param absolutePath absolute path of the file to hot-swap
     */
    void hotSwapByAbsolutePath(String absolutePath);

    void logAllUpdateClassFileInfo();

}
