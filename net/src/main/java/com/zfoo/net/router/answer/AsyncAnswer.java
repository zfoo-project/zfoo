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

import com.zfoo.net.router.attachment.SignalAttachment;
import com.zfoo.protocol.util.ThreadUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author godotg
 */
public class AsyncAnswer<T> implements IAsyncAnswer<T> {

    private T futurePacket;

    private final List<Consumer<T>> consumerList = new ArrayList<>(2);
    private SignalAttachment signalAttachment;

    private Runnable askCallback;

    private Runnable notCompleteCallback;


    @Override
    public IAsyncAnswer<T> thenAccept(Consumer<T> consumer) {
        consumerList.add(consumer);
        return this;
    }

    @Override
    public void whenComplete(Consumer<T> consumer) {
        thenAccept(consumer);

        // 这里其实会触发发送消息
        askCallback.run();
    }

    @Override
    public IAsyncAnswer<T> notComplete(Runnable notCompleteCallback) {
        this.notCompleteCallback = ThreadUtils.safeRunnable(notCompleteCallback);
        return this;
    }

    public void consume() {
        consumerList.forEach(it -> it.accept(futurePacket));
    }

    public T getFuturePacket() {
        return futurePacket;
    }

    public void setFuturePacket(T futurePacket) {
        this.futurePacket = futurePacket;
    }

    public SignalAttachment getSignalAttachment() {
        return signalAttachment;
    }

    public void setSignalAttachment(SignalAttachment signalAttachment) {
        this.signalAttachment = signalAttachment;
    }

    public Runnable getAskCallback() {
        return askCallback;
    }

    public void setAskCallback(Runnable askCallback) {
        this.askCallback = askCallback;
    }

    public Runnable getNotCompleteCallback() {
        return notCompleteCallback;
    }
}
