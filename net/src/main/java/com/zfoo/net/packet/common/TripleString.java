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

import com.zfoo.protocol.IPacket;

/**
 * @author godotg
 * @version 3.0
 */
public class TripleString implements IPacket {

    public static final short PROTOCOL_ID = 115;

    private String left;
    private String middle;
    private String right;

    public static TripleString valueOf(String left, String middle, String right) {
        var triple = new TripleString();
        triple.left = left;
        triple.middle = middle;
        triple.right = right;
        return triple;
    }

    @Override
    public short protocolId() {
        return PROTOCOL_ID;
    }

    public String getLeft() {
        return left;
    }

    public void setLeft(String left) {
        this.left = left;
    }

    public String getMiddle() {
        return middle;
    }

    public void setMiddle(String middle) {
        this.middle = middle;
    }

    public String getRight() {
        return right;
    }

    public void setRight(String right) {
        this.right = right;
    }
}
