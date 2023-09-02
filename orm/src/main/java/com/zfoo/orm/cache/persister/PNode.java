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

package com.zfoo.orm.cache.persister;

import com.zfoo.orm.model.IEntity;
import com.zfoo.scheduler.util.TimeUtils;

/**
 * Persister Node
 * <p>
 * 需要持久化的一个节点
 *
 * @author godotg
 * @version 3.0
 */
public class PNode<E extends IEntity<?>> {

    // 写入数据库的时间
    private volatile long writeToDbTime;
    // 修改数据的时间
    private volatile long modifiedTime;

    private volatile E entity;

    // 记录最初访问时的线程信息
    private long threadId;

    public PNode(E entity) {
        this.entity = entity;

        var currentTime = TimeUtils.now();
        this.writeToDbTime = currentTime;
        this.modifiedTime = currentTime;
    }

    public E getEntity() {
        return entity;
    }

    public void setEntity(E entity) {
        this.entity = entity;
    }

    public long getWriteToDbTime() {
        return writeToDbTime;
    }

    public void setWriteToDbTime(long writeToDbTime) {
        this.writeToDbTime = writeToDbTime;
    }

    public long getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(long modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public long getThreadId() {
        return threadId;
    }

    public void setThreadId(long threadId) {
        this.threadId = threadId;
    }

}
