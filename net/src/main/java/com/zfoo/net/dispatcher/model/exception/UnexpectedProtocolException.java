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

package com.zfoo.net.dispatcher.model.exception;

import com.zfoo.protocol.exception.RunException;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public class UnexpectedProtocolException extends RunException {

    public UnexpectedProtocolException(Throwable cause) {
        super(cause);
    }

    public UnexpectedProtocolException(String message) {
        super(message);
    }

    public UnexpectedProtocolException(String template, Object... args) {
        super(template, args);
    }

    public UnexpectedProtocolException(Throwable cause, String message) {
        super(cause, message);
    }

    public UnexpectedProtocolException(Throwable cause, String template, Object... args) {
        super(cause, template, args);
    }
}
