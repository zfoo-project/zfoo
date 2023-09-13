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

package com.zfoo.net.packet.common;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.zfoo.protocol.anno.Protocol;

/**
 * @author godotg
 */
@Protocol(id = 117)
public class TripleLLS {

    @JsonSerialize(using = ToStringSerializer.class)
    private long left;
    @JsonSerialize(using = ToStringSerializer.class)
    private long middle;
    private String right;

    public static TripleLLS valueOf(long left, long middle, String right) {
        var triple = new TripleLLS();
        triple.left = left;
        triple.middle = middle;
        triple.right = right;
        return triple;
    }

    public long getLeft() {
        return left;
    }

    public void setLeft(long left) {
        this.left = left;
    }

    public long getMiddle() {
        return middle;
    }

    public void setMiddle(long middle) {
        this.middle = middle;
    }

    public String getRight() {
        return right;
    }

    public void setRight(String right) {
        this.right = right;
    }
}
