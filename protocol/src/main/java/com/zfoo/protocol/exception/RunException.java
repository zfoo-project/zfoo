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

package com.zfoo.protocol.exception;

import com.zfoo.protocol.util.StringUtils;

/**
 * @author godotg
 * @version 3.0
 */
public class RunException extends RuntimeException {

    public RunException(Throwable cause) {
        super(cause);
    }

    public RunException(String message) {
        super(message);
    }

    public RunException(String template, Object... args) {
        super(StringUtils.format(template, args));
    }

    public RunException(Throwable cause, String message) {
        super(message, cause);
    }

    public RunException(Throwable cause, String template, Object... args) {
        super(StringUtils.format(template, args), cause);
    }

}
