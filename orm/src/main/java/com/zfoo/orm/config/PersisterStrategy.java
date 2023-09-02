/*
 * Copyright (C) 2020 The zfoo Authors
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.zfoo.orm.config;

/**
 * @author godotg
 * @version 3.0
 */
public class PersisterStrategy {

    private String strategy;

    private PersisterTypeEnum type;

    private String config;

    public PersisterStrategy() {
    }

    public PersisterStrategy(String strategy, String type, String config) {
        this.strategy = strategy;
        this.config = config;
        this.type = PersisterTypeEnum.getPersisterType(type);
    }


    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public PersisterTypeEnum getType() {
        return type;
    }

    public void setType(PersisterTypeEnum type) {
        this.type = type;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }
}
