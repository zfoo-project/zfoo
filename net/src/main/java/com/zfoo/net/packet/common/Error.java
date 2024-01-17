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

import com.zfoo.protocol.anno.Protocol;
import com.zfoo.protocol.util.StringUtils;

/**
 * internal error message
 *
 * @author godotg
 */
@Protocol(id = 101)
public class Error {

    private int code;
    private String message;

    @Override
    public String toString() {
        return StringUtils.format("code:[{}] message:[{}]", code, message);
    }

    public static Error valueOf(int code, String errorMessage) {
        Error response = new Error();
        response.code = code;
        response.message = errorMessage;
        return response;
    }

    public static Error valueOf(String errorMessage) {
        Error response = new Error();
        response.code = 0;
        response.message = errorMessage;
        return response;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
