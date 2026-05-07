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

package com.zfoo.net.consumer.registry;

import org.apache.zookeeper.CreateMode;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author godotg
 */
public interface IRegistry {

    void start();

    void checkConsumer();

    /**
     * Set the data for path
     */
    void addData(String path, byte[] bytes, CreateMode mode);

    void removeData(String path);

    byte[] queryData(String path);

    boolean haveNode(String path);

    List<String> children(String path);

    String rootPath();

    /**
     * Get registration information for all remote service providers.
     */
    List<Register> remoteProviderRegisters();

    /**
     * Listen for changes under the given path.
     *
     * @param listenerPath   the path to watch
     * @param updateCallback callback invoked on data creation/update; first parameter is the path, second is the changed content
     * @param removeCallback callback invoked on data removal; first parameter is the path
     */
    void addListener(String listenerPath, @Nullable BiConsumer<String, byte[]> updateCallback, @Nullable Consumer<String> removeCallback);

    void shutdown();

}
