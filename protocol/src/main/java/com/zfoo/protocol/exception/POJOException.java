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

/**
 * 不是一个POJO对象，POJO对象不应该继承别的类
 *
 * @author jaysunxiao
 * @version 3.0
 */
public class POJOException extends RuntimeException {

    private static final String MESSAGE = "not a POJO object, can't extend other object";

    private static final String HYPHEN = "-";//连接号，连接号与破折号的区别是，连接号的两头不用空格

    private static final String LEFT_SQUARE_BRACKET = "[";//左方括号

    private static final String RIGHT_SQUARE_BRACKET = "]";//右方括号

    public POJOException() {
        super(POJOException.MESSAGE);
    }

    public POJOException(String message) {
        super(POJOException.MESSAGE + POJOException.HYPHEN + POJOException.LEFT_SQUARE_BRACKET + message + POJOException.RIGHT_SQUARE_BRACKET);
    }

}
