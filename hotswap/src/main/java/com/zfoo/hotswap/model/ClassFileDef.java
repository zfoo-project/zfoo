package com.zfoo.hotswap.model;

import com.zfoo.util.security.MD5Utils;

/**
 * @author godotg
 * @version 3.0
 */
public class ClassFileDef {

    private String path;
    private String className;
    private byte[] data;
    private long lastModifyTime;
    private String md5;

    public ClassFileDef(String className, String path, long lastModifyTime, byte[] data) {
        this.className = className;
        this.path = path;
        this.lastModifyTime = lastModifyTime;
        this.data = data;
        this.md5 = MD5Utils.bytesToMD5(data);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public long getLastModifyTime() {
        return lastModifyTime;
    }

    public void setLastModifyTime(long lastModifyTime) {
        this.lastModifyTime = lastModifyTime;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }
}
