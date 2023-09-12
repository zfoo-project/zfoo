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
 */
public class RunException extends RuntimeException {

    public RunException(Throwable cause) {
        super(cause);
    }

    public RunException(String message) {
        super(message);
    }

    public RunException(String message, Throwable cause) {
        super(message, cause);
    }

    public RunException(String template, Object arg1, Throwable cause) {
        super(StringUtils.format(template, arg1), cause);
    }

    public RunException(String template, Object arg1, Object arg2, Throwable cause) {
        super(StringUtils.format(template, arg1, arg2), cause);
    }

    public RunException(String template, Object arg1, Object arg2, Object arg3, Throwable cause) {
        super(StringUtils.format(template, arg1, arg2, arg3), cause);
    }

    public RunException(String template, Object arg1, Object arg2, Object arg3, Object arg4, Throwable cause) {
        super(StringUtils.format(template, arg1, arg2, arg3, arg4), cause);
    }

    public RunException(String template, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Throwable cause) {
        super(StringUtils.format(template, arg1, arg2, arg3, arg4, arg5), cause);
    }

    public RunException(String template, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Throwable cause) {
        super(StringUtils.format(template, arg1, arg2, arg3, arg4, arg5, arg6), cause);
    }

    public RunException(String template, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Throwable cause) {
        super(StringUtils.format(template, arg1, arg2, arg3, arg4, arg5, arg6, arg7), cause);
    }

    public RunException(String template, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8, Throwable cause) {
        super(StringUtils.format(template, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8), cause);
    }

    public RunException(String template, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8, Object arg9, Throwable cause) {
        super(StringUtils.format(template, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9), cause);
    }

    public RunException(String template, Object... args) {
        super(StringUtils.format(template, args));
    }

}
