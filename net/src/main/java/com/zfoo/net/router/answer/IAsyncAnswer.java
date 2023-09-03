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

package com.zfoo.net.router.answer;

import com.zfoo.net.packet.IPacket;

import java.util.function.Consumer;

/**
 * @author godotg
 * @version 3.0
 */
public interface IAsyncAnswer<T extends IPacket> {

    IAsyncAnswer<T> thenAccept(Consumer<T> consumer);

    /**
     * EN:To receive the message returned asynchronously and process the message, the asynchronous request must call this method
     * CN:接收到异步返回的消息，并处理这个消息，异步请求必须要调用这个方法
     */
    void whenComplete(Consumer<T> consumer);

    /**
     * EN:If the asynchronous request does not return successfully, the method is called
     * CN:如果异步请求没有成功返回，那么就会回调该方法
     */
    IAsyncAnswer<T> notComplete(Runnable notCompleteCallback);

}
