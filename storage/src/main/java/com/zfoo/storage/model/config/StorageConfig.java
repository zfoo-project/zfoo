/*
 * Copyright (C) 2020 The zfoo Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.zfoo.storage.model.config;

/**
 * @author godotg
 * @version 4.0
 */
public class StorageConfig {

    private String id;

    private String[] scanPackages;

    private String[] resourceLocations;

    // 类的属性是否可写，如果为false则类的属性必须为private并且不能有set方法
    private boolean writeable;

    // 未被使用的Storage是否回收，默认开启节省资源
    private boolean recycle;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String[] getScanPackage() {
        return scanPackages;
    }

    public void setScanPackage(String[] scanPackages) {
        this.scanPackages = scanPackages;
    }

    public String[] getResourceLocations() {
        return resourceLocations;
    }

    public void setResourceLocations(String[] resourceLocations) {
        this.resourceLocations = resourceLocations;
    }

    public boolean isWriteable() {
        return writeable;
    }

    public void setWriteable(boolean writeable) {
        this.writeable = writeable;
    }

    public boolean isRecycle() {
        return recycle;
    }

    public void setRecycle(boolean recycle) {
        this.recycle = recycle;
    }
}
