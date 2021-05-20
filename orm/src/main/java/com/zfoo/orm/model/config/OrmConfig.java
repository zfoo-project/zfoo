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

package com.zfoo.orm.model.config;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public class OrmConfig {

    private String id;

    private String entityPackage;

    private HostConfig hostConfig;

    private CachesConfig cachesConfig;

    private PersistersConfig persistersConfig;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEntityPackage() {
        return entityPackage;
    }

    public void setEntityPackage(String entityPackage) {
        this.entityPackage = entityPackage;
    }

    public HostConfig getHostConfig() {
        return hostConfig;
    }

    public void setHostConfig(HostConfig hostConfig) {
        this.hostConfig = hostConfig;
    }

    public CachesConfig getCachesConfig() {
        return cachesConfig;
    }

    public void setCachesConfig(CachesConfig cachesConfig) {
        this.cachesConfig = cachesConfig;
    }

    public PersistersConfig getPersistersConfig() {
        return persistersConfig;
    }

    public void setPersistersConfig(PersistersConfig persistersConfig) {
        this.persistersConfig = persistersConfig;
    }
}
