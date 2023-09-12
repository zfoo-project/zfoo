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
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author godotg
 */
public interface IRegistry {

    void start();

    void checkConsumer();

    void addData(String path, byte[] bytes, CreateMode mode);

    void removeData(String path);

    byte[] queryData(String path);

    boolean haveNode(String path);

    List<String> children(String path);

    Set<RegisterVO> remoteProviderRegisterSet();

    /**
     * 监听path路径下的更新
     *
     * @param listenerPath   需要监听的路径
     * @param updateCallback 回调方法，第一个参数是路径，第二个是变化的内容
     * @param removeCallback 回调方法，第一个参数是路径，第二个是变化的内容
     */
    void addListener(String listenerPath, @Nullable BiConsumer<String, byte[]> updateCallback, @Nullable Consumer<String> removeCallback);

    void shutdown();

}
