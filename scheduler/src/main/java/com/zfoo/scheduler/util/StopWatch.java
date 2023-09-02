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

package com.zfoo.scheduler.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author godotg
 * @version 3.0
 */
public class StopWatch {

    private final long startTime = TimeUtils.currentTimeMillis();

    public long cost() {
        return TimeUtils.currentTimeMillis() - startTime;
    }

    /**
     * 从StopWatch被创建，到调用这个方法消耗的时间
     *
     * @return 返回消耗的时间，保留两位小数，格式xx.xx
     */
    public String costSeconds() {
        var cost = cost() / (float) TimeUtils.MILLIS_PER_SECOND;
        var decimal = new BigDecimal(cost);
        return decimal.setScale(2, RoundingMode.HALF_UP).toString();
    }

    public String costMinutes() {
        var cost = cost() / (float) TimeUtils.MILLIS_PER_MINUTE;
        var decimal = new BigDecimal(cost);
        return decimal.setScale(2, RoundingMode.HALF_UP).toString();
    }

    public long getStartTime() {
        return startTime;
    }
}
