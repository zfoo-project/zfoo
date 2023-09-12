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

package com.zfoo.net.config.model;

import com.zfoo.protocol.util.StringUtils;

import java.util.Map;
import java.util.Objects;

/**
 * @author godotg
 */
public class RegistryConfig {

    private String center;
    private String user;
    private String password;
    private Map<String, String> address;

    public static RegistryConfig valueOf(String center, String user, String password, Map<String, String> addressMap) {
        RegistryConfig config = new RegistryConfig();
        config.center = center;
        config.user = user;
        config.password = password;
        config.address = addressMap;
        return config;
    }

    public boolean hasZookeeperAuthor() {
        return !(StringUtils.isBlank(user) || StringUtils.isBlank(password));
    }

    public String toZookeeperAuthor() {
        return user + StringUtils.COLON + password;
    }

    public String getCenter() {
        return center;
    }

    public void setCenter(String center) {
        this.center = center;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Map<String, String> getAddress() {
        return address;
    }

    public void setAddress(Map<String, String> address) {
        this.address = address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RegistryConfig that = (RegistryConfig) o;
        return Objects.equals(center, that.center) &&
                Objects.equals(user, that.user) &&
                Objects.equals(password, that.password) &&
                Objects.equals(address, that.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(center, user, password, address);
    }
}
