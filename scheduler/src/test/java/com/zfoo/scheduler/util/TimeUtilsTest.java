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

package com.zfoo.scheduler.util;

import org.junit.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;

/**
 * @author godotg
 */
public class TimeUtilsTest {

    @Test
    public void testLocalDate() {
        // 取当前日期：
        LocalDate today = LocalDate.now(); // -> 2014-12-24
        // 根据年月日取日期：
        LocalDate crischristmas = LocalDate.of(2014, 12, 25); // -> 2014-12-25
        // 根据字符串取：
        LocalDate endOfFeb = LocalDate.parse("2014-02-28"); // 严格按照ISO yyyy-MM-dd验证，02写成2都不行，当然也有一个重载方法允许自己定义格式
        LocalDate.parse("2014-02-26"); // 无效日期无法通过：DateTimeParseException: Invalid date
        // 取本月第1天：
        LocalDate firstDayOfThisMonth = today.with(TemporalAdjusters.firstDayOfMonth()); // 2017-03-01
        // 取本月第2天：
        LocalDate secondDayOfThisMonth = today.withDayOfMonth(2); // 2017-03-02
        // 取本月最后一天，再也不用计算是28，29，30还是31：
        LocalDate lastDayOfThisMonth = today.with(TemporalAdjusters.lastDayOfMonth()); // 2017-12-31
        // 取下一天：
        LocalDate firstDayOf2015 = lastDayOfThisMonth.plusDays(1); // 变成了2018-01-01
        // 取2017年1月第一个周一，用Calendar要死掉很多脑细胞：
        LocalDate firstMondayOf2015 = LocalDate.parse("2017-01-01").with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY)); // 2017-01-02
    }

    @Test
    public void testLocalTime() {
        System.out.println(Long.MAX_VALUE);
    }

    @Test
    public void testLocalDateTime() {
        LocalDate date = LocalDate.of(2018, 12, 4);
        LocalTime time = LocalTime.of(0, 0, 0);
        LocalDateTime localDateTime = LocalDateTime.of(date, time);
        System.out.println(localDateTime);
        //将localDateTime转换成时间戳
        System.out.println("localDateTime:" + java.sql.Timestamp.valueOf(localDateTime).getTime());
    }


    /**
     * 计算周五的活动下次开启的时间
     */
    @Test
    public void test() {
        LocalDate date = LocalDate.of(2018, 12, 6);
        LocalTime time = LocalTime.of(0, 0, 0);

        int days = DayOfWeek.FRIDAY.getValue() - date.getDayOfWeek().getValue();
        LocalDate localDate = (days <= 0) ? date.plusDays(DayOfWeek.SUNDAY.getValue() + days) : date.plusDays(days);
        System.out.println(localDate);

        LocalDateTime localDateTime = LocalDateTime.of(date, time);
        System.out.println(localDateTime);
        System.out.println("date:" + new Date(java.sql.Timestamp.valueOf(localDateTime).getTime()));
    }

}
