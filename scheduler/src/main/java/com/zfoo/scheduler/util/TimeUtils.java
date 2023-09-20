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

    // 一秒钟对应的纳秒数
    public static final long NANO_PER_SECOND = 1_000_000_000;
    // 一秒钟对应的毫秒数
    public static final long MILLIS_PER_SECOND = 1 * 1000;
    // 一分钟对应的毫秒数
    public static final long MILLIS_PER_MINUTE = 1 * 60 * MILLIS_PER_SECOND;
    // 一个小时对应的毫秒数
    public static final long MILLIS_PER_HOUR = 1 * 60 * MILLIS_PER_MINUTE;
    // 一天对应的毫秒数
    public static final long MILLIS_PER_DAY = 1 * 24 * MILLIS_PER_HOUR;
    // 一周对应的毫秒数
    public static final long MILLIS_PER_WEEK = 1 * 7 * MILLIS_PER_DAY;
    // 默认的时区
    public static final TimeZone DEFAULT_TIME_ZONE = TimeZone.getDefault();
    // 默认的时区Id
    public static final ZoneId DEFAULT_ZONE_ID = TimeZone.getDefault().toZoneId();


    // 统一的时间格式模板
    public static final String DATE_FORMAT_TEMPLATE = "yyyy-MM-dd HH:mm:ss";
    // 简单的时间格式模板
    public static final String SIMPLE_DATE_FORMAT_TEMPLATE = "yyyyMMddHHmmss";
    // 统一的时间格式模板
    public static final String DATE_FORMAT_TEMPLATE_FOR_DAY = "yyyy-MM-dd";

    private static final FastThreadLocalAdapter<SimpleDateFormat> DATE_FORMAT = new FastThreadLocalAdapter<>(() -> new SimpleDateFormat(DATE_FORMAT_TEMPLATE));

    private static final FastThreadLocalAdapter<SimpleDateFormat> SIMPLE_DATE_FORMAT = new FastThreadLocalAdapter<>(() -> new SimpleDateFormat(SIMPLE_DATE_FORMAT_TEMPLATE));

    private static final FastThreadLocalAdapter<SimpleDateFormat> DATE_FORMAT_FOR_DAY = new FastThreadLocalAdapter<>(() -> new SimpleDateFormat(DATE_FORMAT_TEMPLATE_FOR_DAY));

    static {
        currentTimeMillis();
        // 调用一下静态方法，使SchedulerBus静态代码块初始化
        SchedulerBus.refreshMinTriggerTimestamp();
    }

    // volatile reduces the cache hit ratio of the CPU
    private static long timestamp = System.currentTimeMillis();

    /**
     * 获取精确的时间戳
     */
    public static long currentTimeMillis() {
        timestamp = System.currentTimeMillis();
        return timestamp;
    }

    /**
     * CN：获取最多只有一秒延迟的粗略时间戳，适用于对时间精度要求不高的场景，比System.currentTimeMillis()的性能高10倍
     * <p>
     * EN：Obtain a coarse timestamp with a delay of up to one second, which is suitable for scenarios that do not require high time accuracy
     */
    public static long now() {
        return timestamp;
    }

    // --------------------------------------日期格式--------------------------------------

    /**
     * 把日期字符串转换成Date对象，统一的日期模板要遵循DATE_FORMAT_TEMPLATE="yyyy-MM-dd HH:mm:ss"
     *
     * @param dateString 日期字符串，如：2018-02-12 10:12:50
     * @return <code>Date</code>
     * @throws ParseException 解析异常
     */
    public static Date stringToDate(String dateString) throws ParseException {
        return DATE_FORMAT.get().parse(dateString);
    }

    /**
     * 把long类型的时间戳转换成字符串格式的时间
     *
     * @param time 时间戳
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

    public static Date dayStringToDate(String dateString) throws ParseException {
        return DATE_FORMAT_FOR_DAY.get().parse(dateString);
    }
    // --------------------------------------日期判断--------------------------------------

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
     * 判断两个日期是否是同一周，设置周一为一周的第一天
     * <p>
     * 2004-12-25”是星期六，也就是说它是2004年中第52周的星期六，那么“2004-12-26”到底是2004年的第几周哪，java中经测试取得的它的Week值是1，
     * 那么也就是说它被看作2005年的第一周了，这个处理是比较好的。可以用来判断“2004-12-26”和“2005-1-1”是同一周。
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
            // yearDiff==0,说明是同一年
            return cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR);
        } else if (yearDiff == 1 && cal2.get(Calendar.MONTH) == 11) {
            //yearDiff==1,说明cal比cal2大一年;java的一月用"0"标识，那么12月用"11"
            return cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR);
        } else if (yearDiff == -1 && cal1.get(Calendar.MONTH) == 11) {
            //yearDiff==-1,说明cal比cal2小一年
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

    // --------------------------------------获取相关时间戳--------------------------------------


    /**
     * 获取给定时间戳对应的日期的0点时间戳
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
     * 获取给定时间戳对应的日期的最后时刻时间戳
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

    // 获取给定时间戳对应的日期的昨天这个时刻的时间戳
    public static long getYesterdayTimeOfDay(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        cal.add(Calendar.DATE, -1);
        return cal.getTimeInMillis();
    }

    // 获取给定时间戳对应小时的起始时间戳
    public static long getStartTimeOfHour(long time) {
        var localDateTime = TimeUtils.timestampToLocalDateTime(time);
        var monday = localDateTime.withMinute(0).withSecond(0).withNano(0);
        return TimeUtils.localDateTimeToTimestamp(monday);
    }

    // 获取给定时间戳对应周的第一天的起始时间戳
    public static long getStartTimeOfWeek(long time) {
        var localDateTime = TimeUtils.timestampToLocalDateTime(time);
        var monday = localDateTime.with(TemporalAdjusters.previous(DayOfWeek.SUNDAY)).plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        return TimeUtils.localDateTimeToTimestamp(monday);
    }

    // 获取给定时间戳对应月的第一天的起始时间戳
    public static long getStartTimeOfMonth(long time) {
        var localDateTime = TimeUtils.timestampToLocalDateTime(time);
        var oneOfMonth = localDateTime.with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0).withNano(0);
        return TimeUtils.localDateTimeToTimestamp(oneOfMonth);
    }

    // 获取给定时间戳对应月的最后一天的结束时间戳
    public static long getEndTimeOfMonth(long time) {
        var localDateTime = TimeUtils.timestampToLocalDateTime(time);
        var oneOfMonth = localDateTime.with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59).withNano(0);
        return TimeUtils.localDateTimeToTimestamp(oneOfMonth);
    }

    //获取上一个月月初的时间
    public static long getLastMonthStart(long time) {
        var localDateTime = TimeUtils.timestampToLocalDateTime(time);
        var lastMonth = localDateTime.minusMonths(1);
        LocalDateTime with = lastMonth.with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0).withNano(0);
        return TimeUtils.localDateTimeToTimestamp(with);
    }

    //获取上一个月月末的时间
    public static long getLastMonthEnd(long time) {
        var localDateTime = TimeUtils.timestampToLocalDateTime(time);
        var lastMonth = localDateTime.minusMonths(1);
        LocalDateTime with = lastMonth.with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59).withNano(0);
        return TimeUtils.localDateTimeToTimestamp(with);
    }

    //获取下一个月月初的时间
    public static long getNextMonthStart(long time) {
        var localDateTime = TimeUtils.timestampToLocalDateTime(time);
        var lastMonth = localDateTime.plusMonths(1);
        LocalDateTime with = lastMonth.with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0).withNano(0);
        return TimeUtils.localDateTimeToTimestamp(with);
    }

    // 获取下一个月月末的时间
    public static long getNextMonthEnd(long time) {
        var localDateTime = TimeUtils.timestampToLocalDateTime(time);
        var lastMonth = localDateTime.plusMonths(1);
        LocalDateTime with = lastMonth.with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59).withNano(0);
        return TimeUtils.localDateTimeToTimestamp(with);
    }

    /**
     * LocalDateTime转毫秒时间戳
     *
     * @param localDateTime LocalDateTime
     * @return 时间戳
     */
    public static Long localDateTimeToTimestamp(LocalDateTime localDateTime) {
        var zoneId = ZoneId.systemDefault();
        var instant = localDateTime.atZone(zoneId).toInstant();
        return instant.toEpochMilli();
    }

    /**
     * 时间戳转LocalDateTime
     *
     * @param timestamp 时间戳
     * @return LocalDateTime
     */
    public static LocalDateTime timestampToLocalDateTime(long timestamp) {
        var instant = Instant.ofEpochMilli(timestamp);
        var zone = ZoneId.systemDefault();
        return LocalDateTime.ofInstant(instant, zone);
    }

    // --------------------------------------时间大小判断--------------------------------------
    public static boolean timeBetween(long time, long from, long end) {
        return from <= time && time <= end;
    }


    // --------------------------------------cron表达式--------------------------------------
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
