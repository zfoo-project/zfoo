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
        // Get current date:
        LocalDate today = LocalDate.now(); // -> 2014-12-24
        // Construct date from year/month/day:
        LocalDate crischristmas = LocalDate.of(2014, 12, 25); // -> 2014-12-25
        // Parse from string:
        LocalDate endOfFeb = LocalDate.parse("2014-02-28"); // Strict ISO yyyy-MM-dd; overloaded methods allow custom formats
        LocalDate.parse("2014-02-26"); // Invalid date throws DateTimeParseException
        // Get first day of this month:
        LocalDate firstDayOfThisMonth = today.with(TemporalAdjusters.firstDayOfMonth()); // 2017-03-01
        // Get second day of this month:
        LocalDate secondDayOfThisMonth = today.withDayOfMonth(2); // 2017-03-02
        // Get last day of this month (no manual 28/29/30/31 needed):
        LocalDate lastDayOfThisMonth = today.with(TemporalAdjusters.lastDayOfMonth()); // 2017-12-31
        // Get next day:
        LocalDate firstDayOf2015 = lastDayOfThisMonth.plusDays(1); // results in 2018-01-01
        // Get first Monday of January 2017 (much simpler than Calendar):
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
        // Convert LocalDateTime to timestamp
        System.out.println("localDateTime:" + java.sql.Timestamp.valueOf(localDateTime).getTime());
    }


    /**
     * Calculate the next activation time for the Friday event
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
