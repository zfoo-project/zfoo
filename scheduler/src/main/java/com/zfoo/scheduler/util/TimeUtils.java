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

import com.zfoo.protocol.util.FastThreadLocalAdapter;
import com.zfoo.scheduler.manager.SchedulerBus;
import org.springframework.scheduling.support.CronExpression;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author godotg
 */
public abstract class TimeUtils {

    // Nanoseconds per second
    public static final long NANO_PER_SECOND = 1_000_000_000;
    // Milliseconds per second
    public static final long MILLIS_PER_SECOND = 1 * 1000;
    // Milliseconds per minute
    public static final long MILLIS_PER_MINUTE = 1 * 60 * MILLIS_PER_SECOND;
    // Milliseconds per hour
    public static final long MILLIS_PER_HOUR = 1 * 60 * MILLIS_PER_MINUTE;
    // Milliseconds per day
    public static final long MILLIS_PER_DAY = 1 * 24 * MILLIS_PER_HOUR;
    // Milliseconds per week
    public static final long MILLIS_PER_WEEK = 1 * 7 * MILLIS_PER_DAY;
    // Default time zone
    public static final TimeZone DEFAULT_TIME_ZONE = TimeZone.getDefault();
    // Default zone ID
    public static final ZoneId DEFAULT_ZONE_ID = TimeZone.getDefault().toZoneId();


    // Standard date-time format template
    public static final String DATE_FORMAT_TEMPLATE = "yyyy-MM-dd HH:mm:ss";
    // Simple date-time format template
    public static final String SIMPLE_DATE_FORMAT_TEMPLATE = "yyyyMMddHHmmss";
    // Standard date-only format template
    public static final String DATE_FORMAT_TEMPLATE_FOR_DAY = "yyyy-MM-dd";

    private static final FastThreadLocalAdapter<SimpleDateFormat> DATE_FORMAT = new FastThreadLocalAdapter<>(() -> new SimpleDateFormat(DATE_FORMAT_TEMPLATE));

    private static final FastThreadLocalAdapter<SimpleDateFormat> SIMPLE_DATE_FORMAT = new FastThreadLocalAdapter<>(() -> new SimpleDateFormat(SIMPLE_DATE_FORMAT_TEMPLATE));

    private static final FastThreadLocalAdapter<SimpleDateFormat> DATE_FORMAT_FOR_DAY = new FastThreadLocalAdapter<>(() -> new SimpleDateFormat(DATE_FORMAT_TEMPLATE_FOR_DAY));

    static {
        currentTimeMillis();
        // Call a static method to trigger the static initializer of SchedulerBus
        SchedulerBus.refreshMinTriggerTimestamp();
    }

    // volatile reduces the cache hit ratio of the CPU
    private static long timestamp = System.currentTimeMillis();

    /**
     * Get the precise current timestamp in milliseconds
     */
    public static long currentTimeMillis() {
        timestamp = System.currentTimeMillis();
        return timestamp;
    }

    /**
     * Obtain a coarse timestamp with a delay of up to one second, which is suitable for scenarios that
     * do not require high time accuracy. Its performance is 10x faster than System.currentTimeMillis().
     */
    public static long now() {
        return timestamp;
    }

    // -------------------------------------- Date Format --------------------------------------

    /**
     * Convert a date string to a Date object. The standard date template follows DATE_FORMAT_TEMPLATE="yyyy-MM-dd HH:mm:ss"
     *
     * @param dateString date string, e.g. 2018-02-12 10:12:50
     * @return <code>Date</code>
     */
    public static Date stringToDate(String dateString) {
        try {
            return DATE_FORMAT.get().parse(dateString);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Convert a long timestamp to a formatted date-time string
     *
     * @param time timestamp in milliseconds
     * @return yyyy-MM-dd HH:mm:ss
     */
    public static String timeToString(long time) {
        return dateToString(new Date(time));
    }

    public static String dateToString(Date date) {
        return DATE_FORMAT.get().format(date);
    }

    public static String simpleDateString() {
        return SIMPLE_DATE_FORMAT.get().format(new Date(now()));
    }

    public static Date dayStringToDate(String dateString) {
        try {
            return DATE_FORMAT_FOR_DAY.get().parse(dateString);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
    // -------------------------------------- Date Comparison --------------------------------------

    /**
     * <p>Checks if two date objects are on the same day ignoring time.</p>
     *
     * <p>28 Mar 2002 13:45 and 28 Mar 2002 06:01 would return true.
     * 28 Mar 2002 13:45 and 12 Mar 2002 13:45 would return false.
     * </p>
     *
     * @param date1 the first date, not altered, not null
     * @param date2 the second date, not altered, not null
     * @return true if they represent the same day
     * @throws IllegalArgumentException if either date is <code>null</code>
     */
    public static boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        var cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        var cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return isSameDay(cal1, cal2);
    }

    public static boolean isSameDay(long time1, long time2) {
        var cal1 = Calendar.getInstance();
        cal1.setTimeInMillis(time1);
        var cal2 = Calendar.getInstance();
        cal2.setTimeInMillis(time2);
        return isSameDay(cal1, cal2);
    }

    /**
     * <p>Checks if two calendar objects are on the same day ignoring time.</p>
     *
     * <p>28 Mar 2002 13:45 and 28 Mar 2002 06:01 would return true.
     * 28 Mar 2002 13:45 and 12 Mar 2002 13:45 would return false.
     * </p>
     *
     * @param cal1 the first calendar, not altered, not null
     * @param cal2 the second calendar, not altered, not null
     * @return true if they represent the same day
     * @throws IllegalArgumentException if either calendar is <code>null</code>
     */
    public static boolean isSameDay(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        return cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    public static String dateFormatForDayString(long time) {
        return DATE_FORMAT_FOR_DAY.get().format(new Date(time));
    }

    public static String dateFormatForDayTimeString(long time) {
        return DATE_FORMAT.get().format(new Date(time));
    }

    /**
     * Check whether two timestamps fall in the same week, with Monday as the first day of the week.
     * For example, "2004-12-25" is Saturday (week 52 of 2004), while "2004-12-26" has a WEEK_OF_YEAR
     * value of 1 in Java (treated as week 1 of 2005), so "2004-12-26" and "2005-01-01" are considered the same week.
     */
    public static boolean isSameWeek(long time1, long time2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTimeInMillis(time1);
        cal2.setTimeInMillis(time2);
        cal1.setFirstDayOfWeek(Calendar.MONDAY);
        cal2.setFirstDayOfWeek(Calendar.MONDAY);

        int yearDiff = cal1.get(Calendar.YEAR) - cal2.get(Calendar.YEAR);
        if (yearDiff == 0 && cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR)) {
            // yearDiff==0 means they are in the same year
            return cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR);
        } else if (yearDiff == 1 && cal2.get(Calendar.MONTH) == 11) {
            // yearDiff==1 means cal1 is one year ahead of cal2; January is represented as "0" in Java, December as "11"
            return cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR);
        } else if (yearDiff == -1 && cal1.get(Calendar.MONTH) == 11) {
            // yearDiff==-1 means cal1 is one year behind cal2
            return cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR);
        }
        return false;
    }

    public static boolean isSameMonth(long time1, long time2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTimeInMillis(time1);
        cal2.setTimeInMillis(time2);
        return cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA)
                && cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH);
    }

    // -------------------------------------- Retrieve Related Timestamps --------------------------------------


    /**
     * Get the timestamp of midnight (00:00:00) for the given timestamp's date
     */
    public static long getZeroTimeOfDay(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }


    /**
     * Get the timestamp of the last moment (23:59:59.999) for the given timestamp's date
     */
    public static long getLastTimeOfDay(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTimeInMillis();
    }

    // Get the timestamp of yesterday at the same time as the given timestamp
    public static long getYesterdayTimeOfDay(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        cal.add(Calendar.DATE, -1);
        return cal.getTimeInMillis();
    }

    // Get the start timestamp of the hour corresponding to the given timestamp
    public static long getStartTimeOfHour(long time) {
        var localDateTime = TimeUtils.timestampToLocalDateTime(time);
        var monday = localDateTime.withMinute(0).withSecond(0).withNano(0);
        return TimeUtils.localDateTimeToTimestamp(monday);
    }

    // Get the start timestamp of the first day of the week corresponding to the given timestamp
    public static long getStartTimeOfWeek(long time) {
        var localDateTime = TimeUtils.timestampToLocalDateTime(time);
        var monday = localDateTime.with(TemporalAdjusters.previous(DayOfWeek.SUNDAY)).plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        return TimeUtils.localDateTimeToTimestamp(monday);
    }

    // Get the start timestamp of the first day of the month corresponding to the given timestamp
    public static long getStartTimeOfMonth(long time) {
        var localDateTime = TimeUtils.timestampToLocalDateTime(time);
        var oneOfMonth = localDateTime.with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0).withNano(0);
        return TimeUtils.localDateTimeToTimestamp(oneOfMonth);
    }

    // Get the end timestamp of the last day of the month corresponding to the given timestamp
    public static long getEndTimeOfMonth(long time) {
        var localDateTime = TimeUtils.timestampToLocalDateTime(time);
        var oneOfMonth = localDateTime.with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59).withNano(0);
        return TimeUtils.localDateTimeToTimestamp(oneOfMonth);
    }

    // Get the start timestamp of the previous month
    public static long getLastMonthStart(long time) {
        var localDateTime = TimeUtils.timestampToLocalDateTime(time);
        var lastMonth = localDateTime.minusMonths(1);
        LocalDateTime with = lastMonth.with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0).withNano(0);
        return TimeUtils.localDateTimeToTimestamp(with);
    }

    // Get the end timestamp of the previous month
    public static long getLastMonthEnd(long time) {
        var localDateTime = TimeUtils.timestampToLocalDateTime(time);
        var lastMonth = localDateTime.minusMonths(1);
        LocalDateTime with = lastMonth.with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59).withNano(0);
        return TimeUtils.localDateTimeToTimestamp(with);
    }

    // Get the start timestamp of the next month
    public static long getNextMonthStart(long time) {
        var localDateTime = TimeUtils.timestampToLocalDateTime(time);
        var lastMonth = localDateTime.plusMonths(1);
        LocalDateTime with = lastMonth.with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0).withNano(0);
        return TimeUtils.localDateTimeToTimestamp(with);
    }

    // Get the end timestamp of the next month
    public static long getNextMonthEnd(long time) {
        var localDateTime = TimeUtils.timestampToLocalDateTime(time);
        var lastMonth = localDateTime.plusMonths(1);
        LocalDateTime with = lastMonth.with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59).withNano(0);
        return TimeUtils.localDateTimeToTimestamp(with);
    }

    /**
     * Convert LocalDateTime to millisecond timestamp
     *
     * @param localDateTime LocalDateTime
     * @return timestamp in milliseconds
     */
    public static Long localDateTimeToTimestamp(LocalDateTime localDateTime) {
        var zoneId = ZoneId.systemDefault();
        var instant = localDateTime.atZone(zoneId).toInstant();
        return instant.toEpochMilli();
    }

    /**
     * Convert millisecond timestamp to LocalDateTime
     *
     * @param timestamp timestamp in milliseconds
     * @return LocalDateTime
     */
    public static LocalDateTime timestampToLocalDateTime(long timestamp) {
        var instant = Instant.ofEpochMilli(timestamp);
        var zone = ZoneId.systemDefault();
        return LocalDateTime.ofInstant(instant, zone);
    }

    // -------------------------------------- Time Comparison --------------------------------------
    public static boolean timeBetween(long time, long from, long end) {
        return from <= time && time <= end;
    }


    // -------------------------------------- Cron Expression --------------------------------------
    public static long nextTimestampByCronExpression(CronExpression expression, long currentTimestamp) {
        var zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(currentTimestamp), DEFAULT_ZONE_ID);
        return nextTimestampByCronExpression(expression, zonedDateTime);
    }

    public static long nextTimestampByCronExpression(CronExpression expression, ZonedDateTime zonedDateTime) {
        var next = expression.next(zonedDateTime);

        if (next == null) {
            return Long.MAX_VALUE;
        }

        return next.toInstant().toEpochMilli();
    }

}
