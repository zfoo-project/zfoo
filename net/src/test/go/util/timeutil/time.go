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
package dtime

import (
	"bytes"
	"context"
	"database/sql/driver"
	"math"
	"math/rand"
	"strconv"
	"sync"
	"time"
)

/**
  时间获取
  时间转换必须加入时区设置,请注意
*/

var (
	randSeek = int64(1)
	l        sync.Mutex
	zone     = "CST" //时区
)

func TimeIntToDate(time_int int) string {
	var cstZone = time.FixedZone(zone, 8*3600)
	return time.Unix(int64(time_int), 0).In(cstZone).Format("2006-01-02 15:04:05")
}

func GetNowDateTime() string {
	var cstZone = time.FixedZone(zone, 8*3600)
	return time.Now().In(cstZone).Format("2006-01-02 15:04:05")
}

func GetDate() string {
	var cstZone = time.FixedZone(zone, 8*3600)
	return time.Now().In(cstZone).Format("2006-01-02")
}

//防时间间隔
func GetIntTime() int {
	var _t = int(time.Now().Unix())
	return _t
}

//暂时独立
func _getRandomSring(num int, str ...string) string {
	s := "123456789"
	if len(str) > 0 {
		s = str[0]
	}
	l := len(s)
	r := rand.New(rand.NewSource(getRandSeek()))
	var buf bytes.Buffer
	for i := 0; i < num; i++ {
		x := r.Intn(l)
		buf.WriteString(s[x : x+1])
	}
	return buf.String()
}

func getRandSeek() int64 {
	l.Lock()
	if randSeek >= 100000000 {
		randSeek = 1
	}
	randSeek++
	l.Unlock()
	return time.Now().UnixNano() + randSeek
}

//获取今天时间戳 Today => 00:00:00
func TodayTimeUnix() int {
	t := time.Now()
	tm1 := int(time.Date(t.Year(), t.Month(), t.Day(), 0, 0, 0, 0, t.Location()).Unix())
	return tm1
}

//获取今天时间戳 Today => 23:59:59
func TodayNightUnix() int {
	tm1 := TodayTimeUnix() + 86400 - 1
	return tm1
}



// Time be used to MySql timestamp converting.
type Time int64

// Scan scan time.
func (jt *Time) Scan(src interface{}) (err error) {
	switch sc := src.(type) {
	case time.Time:
		*jt = Time(sc.Unix())
	case string:
		var i int64
		i, err = strconv.ParseInt(sc, 10, 64)
		*jt = Time(i)
	}
	return
}

// Value get time value.
func (jt Time) Value() (driver.Value, error) {
	return time.Unix(int64(jt), 0), nil
}

// Time get time.
func (jt Time) Time() time.Time {
	return time.Unix(int64(jt), 0)
}

// Duration be used toml unmarshal string time, like 1s, 500ms.
type Duration time.Duration

// UnmarshalText unmarshal text to duration.
func (d *Duration) UnmarshalText(text []byte) error {
	tmp, err := time.ParseDuration(string(text))
	if err == nil {
		*d = Duration(tmp)
	}
	return err
}

// Shrink will decrease the duration by comparing with context's timeout duration
// and return new timeout\context\CancelFunc.
func (d Duration) Shrink(c context.Context) (Duration, context.Context, context.CancelFunc) {
	if deadline, ok := c.Deadline(); ok {
		if ctimeout := time.Until(deadline); ctimeout < time.Duration(d) {
			// deliver small timeout
			return Duration(ctimeout), c, func() {}
		}
	}
	ctx, cancel := context.WithTimeout(c, time.Duration(d))
	return d, ctx, cancel
}


// ------------------------------------------------------------------------------------
const (
	YFormatNum          = "2006"
	YMFormatNum         = "200601"
	DateFormatNum       = "20060102"
	DateHFormatNum      = "2006010215"
	DateHMFormatNum     = "200601021504"
	DateTimeFormatNum   = "20060102150405"
	HFormatNum          = "15"
	HMFormatNum         = "1504"
	TimeFormatNum       = "150405"
	DateFormat          = "2006-01-02"
	TimeFormat          = "15:04:05"
	DateTimeFormat      = "2006-01-02 15:04:05"
	DateTimeFormatMilli = "2006-01-02 15:04:05.000"
	DateTimeFormatMicro = "2006-01-02 15:04:05.000000"
	DateTimeFormatNano  = "2006-01-02 15:04:05.000000000"
)

//
// Part 0: Get some useful infomation about time
//

// GetNowS gets unix timestamp in second
func GetNowS() int64 {
	return time.Now().Unix()
}

// GetNowMs gets unix timestamp in millisecond
func GetNowMs() int64 {
	return time.Now().UnixNano() / int64(time.Millisecond)
}

// GetNowUs gets unix timestamp in microsecond
func GetNowUs() int64 {
	return time.Now().UnixNano() / int64(time.Microsecond)
}

// GetNowNs gets unix timestamp in nanosecond
func GetNowNs() int64 {
	return time.Now().UnixNano()
}

// GetNowDate gets now date in YYYY-MM-DD
func GetNowDate() string {
	return time.Now().Format(DateFormat)
}

// GetNowDate gets now time in hh:mm:ss
func GetNowTime() string {
	return time.Now().Format(TimeFormat)
}

// GetNowDateTimeZ gets now datetime with zone in YYYY-MM-DD hh:mm:ss Zone
// e.g. 2020-05-11 23:18:07 +08:00
func GetNowDateTimeZ() string {
	return time.Now().Format("2006-01-02 15:04:05 Z07:00")
}

// GetDayBeginMoment gets the starting moment of one day
func GetDayBeginMoment(t time.Time) time.Time {
	y, m, d := t.Date()
	n := time.Date(y, m, d, 0, 0, 0, 0, time.Local)
	return n
}

// GetDayBeginMoment1 gets the starting moment of one day specified by UNIX time stamp
func GetDayBeginMoment1(uts int64) time.Time {
	y, m, d := time.Unix(uts, 0).Date()
	n := time.Date(y, m, d, 0, 0, 0, 0, time.Local)
	return n
}

// GetDayEndMoment gets the ending moment of one day
func GetDayEndMoment(t time.Time) time.Time {
	y, m, d := t.Date()
	n := time.Date(y, m, d, 23, 59, 59, 999999999, time.Local)
	return n
}

// GetDayEndMoment1 gets the ending moment of one day specified by UNIX time stamp
func GetDayEndMoment1(uts int64) time.Time {
	y, m, d := time.Unix(uts, 0).Date()
	n := time.Date(y, m, d, 23, 59, 59, 999999999, time.Local)
	return n
}

// GetDayElapsedS gets the elapsed seconds since the starting moment of one day
func GetDayElapsedS(t time.Time) int64 {
	return t.Unix() - GetDayBeginMoment(t).Unix()
}

// GetDayElapsedMs gets the elapsed milliseconds since the starting moment of one day
func GetDayElapsedMs(t time.Time) int64 {
	return (t.UnixNano() - GetDayBeginMoment(t).UnixNano()) / int64(time.Millisecond)
}

// GetDayElapsedUs gets the elapsed microseconds since the starting moment of one day
func GetDayElapsedUs(t time.Time) int64 {
	return (t.UnixNano() - GetDayBeginMoment(t).UnixNano()) / int64(time.Microsecond)
}

// GetDayElapsedNs gets the elapsed nanoseconds since the starting moment of one day
func GetDayElapsedNs(t time.Time) int64 {
	return t.Unix() - GetDayBeginMoment(t).Unix()
}

// GetDaysBtwTs gets the number of days between two timestamps and round down
func GetDaysBtwTs(ts0, ts1 int64) int64 {
	return int64(math.Abs(float64(ts0-ts1))) / 86400
}

// GetHoursBtwTs gets the number of hours between two timestamps and round down
func GetHoursBtwTs(ts0, ts1 int64) int64 {
	return int64(math.Abs(float64(ts0-ts1))) / 3600
}

// GetMinutesBtwTs gets the number of hours between two timestamps and round down
func GetMinutesBtwTs(ts0, ts1 int64) int64 {
	return int64(math.Abs(float64(ts0-ts1))) / 60
}

// GetWeekday gets the weekday time
func GetWeekday(t time.Time, w time.Weekday) time.Time {
	if t.Weekday() == w {
		return t
	}
	d := w - t.Weekday()
	if w == time.Sunday {
		d += 7
	} else if t.Weekday() == time.Sunday {
		d -= 7
	}
	return t.AddDate(0, 0, int(d))
}

// GetMonDate gets monday date in format 2006-01-02
func GetMonDate(t time.Time) string {
	return GetWeekday(t, time.Monday).Format(DateFormat)
}

// GetTuesDate gets tuesday date in format 2006-01-02
func GetTuesDate(t time.Time) string {
	return GetWeekday(t, time.Tuesday).Format(DateFormat)
}

// GetWedDate gets wednesday date in format 2006-01-02
func GetWedDate(t time.Time) string {
	return GetWeekday(t, time.Wednesday).Format(DateFormat)
}

// GetThursDate gets thursday date in format 2006-01-02
func GetThursDate(t time.Time) string {
	return GetWeekday(t, time.Thursday).Format(DateFormat)
}

// GetFriDate gets friday date in format 2006-01-02
func GetFriDate(t time.Time) string {
	return GetWeekday(t, time.Friday).Format(DateFormat)
}

// GetSatDate gets saturday date in format 2006-01-02
func GetSatDate(t time.Time) string {
	return GetWeekday(t, time.Saturday).Format(DateFormat)
}

// GetSunDate gets sunday date in format 2006-01-02
func GetSunDate(t time.Time) string {
	return GetWeekday(t, time.Sunday).Format(DateFormat)
}

// IsLeapYear checks the year whether is leap year
func IsLeapYear(year int) bool {
	return (year%4 == 0 && year%100 != 0) || year%400 == 0
}

// IsSameYear checks the unix timestamp whether is the same year
func IsSameYear(uts1, uts2 int64) bool {
	t1 := time.Unix(uts1, 0)
	t2 := time.Unix(uts2, 0)
	return t1.Format(YFormatNum) == t2.Format(YFormatNum)
}

// IsSameMonth checks the unix timestamp whether is the same month
func IsSameMonth(uts1, uts2 int64) bool {
	t1 := time.Unix(uts1, 0)
	t2 := time.Unix(uts2, 0)
	return t1.Format(YMFormatNum) == t2.Format(YMFormatNum)
}

// IsSameDay checks the unix timestamp whether is the same day
func IsSameDay(uts1, uts2 int64) bool {
	t1 := time.Unix(uts1, 0)
	t2 := time.Unix(uts2, 0)
	return t1.Format(DateFormatNum) == t2.Format(DateFormatNum)
}

// IsSameHour checks the unix timestamp whether is the same hour
func IsSameHour(uts1, uts2 int64) bool {
	t1 := time.Unix(uts1, 0)
	t2 := time.Unix(uts2, 0)
	return t1.Format(DateHFormatNum) == t2.Format(DateHFormatNum)
}

// IsSameMinute checks the unix timestamp whether is the same minute
func IsSameMinute(uts1, uts2 int64) bool {
	t1 := time.Unix(uts1, 0)
	t2 := time.Unix(uts2, 0)
	return t1.Format(DateHMFormatNum) == t2.Format(DateHMFormatNum)
}

// IsSameWeek checks the unix timestamp whether is the same week
func IsSameWeek(uts1, uts2 int64) bool {
	t1 := time.Unix(uts1, 0)
	t2 := time.Unix(uts2, 0)
	return GetMonDate(t1) == GetMonDate(t2)
}

//
// Part 1: Common conversion about time
//

// DateTime2UTs converts datetime in YYYY-MM-DD hh:mm:ss to unix timestamp
func DateTime2UTs(dt string) int64 {
	loc, _ := time.LoadLocation("Local")
	t, err := time.ParseInLocation(DateTimeFormat, dt, loc)
	if err != nil {
		return 0
	}
	return t.Unix()
}

// UTs2DateTime converts unix timestamp to datetime in YYYY-MM-DD hh:mm:ss
func UTs2DateTime(uts int64) string {
	return time.Unix(uts, 0).Format(DateTimeFormat)
}

//
// Part 2: A time counter to count time interval
//

// TimeCounter is used to count time interval
type TimeCounter struct {
	time.Time
	int64
}

// NewTimeCounter create a time counter
func NewTimeCounter() (t *TimeCounter) {
	t = new(TimeCounter)
	t.Set()
	return t
}

// Set start timing
func (t *TimeCounter) Set() {
	t.Time = time.Now()
	t.int64 = t.Time.UnixNano()
}

// GetD return the time interval from the beginning to now in time.Duration
func (t *TimeCounter) GetD() time.Duration {
	return time.Since(t.Time)
}

// GetS return the time interval from the beginning to now in second
func (t *TimeCounter) GetS() int64 {
	return (time.Now().UnixNano() - t.int64) / int64(time.Second)
}

// GetMs return the time interval from the beginning to now in millisecond
func (t *TimeCounter) GetMs() int64 {
	return (time.Now().UnixNano() - t.int64) / int64(time.Millisecond)
}

// GetUs return the time interval from the beginning to now in microsecond
func (t *TimeCounter) GetUs() int64 {
	return (time.Now().UnixNano() - t.int64) / int64(time.Microsecond)
}

// GetNs return the time interval from the beginning to now in nanosecond
func (t *TimeCounter) GetNs() int64 {
	return time.Now().UnixNano() - t.int64
}

// TimeCost count time cost
func TimeCost() func() time.Duration {
	start := time.Now()
	return func() time.Duration {
		return time.Since(start)
	}
}
