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

package com.zfoo.event.model;

import com.zfoo.protocol.util.RandomUtils;

/**
 * @author godotg
 */
public interface IEvent {

    /**
     * 这个返回的是一个用于确定事件在EventBus中的哪个线程池的执行的一个参数，只有异步事件才会有作用
     * <p>
     * 比如cpu是四核，那么EventBus中的executors线程池的数量为8个，通过取余可以确定异步事件在哪个线程池中执行。
     * 如果参数是0，0 % 8 = 0，那么异步事件最终会在executors[0]表示的线程池中执行
     * 如果参数是1，1 % 8 = 1，那么异步事件最终会在executors[1]表示的线程池中执行
     * 如果参数是8，8 % 8 = 0，那么异步事件最终会在executors[0]表示的线程池中执行
     * 如果参数是9，9 % 8 = 1，那么异步事件最终会在executors[1]表示的线程池中执行
     * <p>
     * 通过返回的参数，可以轻松控制异步事件在哪个线程池中去执行。
     * 因为EventBus中的线程池都是单线程线程池，如果将一些异步事件放在同一个线程池中执行，可以不用加锁，提高程序运行的效率。
     * <p>
     * 默认返回一个随机数，这就导致如果不重写这个方法，那么异步事件有可能会在EventBus中的任何一条线程池中去执行。
     *
     * @return 线程池的执行的一个参数
     */
    default int executorHash() {
        return RandomUtils.randomInt();
    }

}
