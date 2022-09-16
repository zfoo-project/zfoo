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
package datetime

import (
	"testing"
	"time"
)

func TestTime(t *testing.T) {
	t.Logf("Time: %d", Time())
}

func TestMilliTime(t *testing.T) {
	t.Logf("MilliTime: %d", MilliTime())
}

func TestMicroTime(t *testing.T) {
	t.Logf("MicroTime: %d", MicroTime())
}

func TestDateTime(t *testing.T) {
	t.Logf("Datetime: %s", Datetime())
}

func TestTimestamp(t *testing.T) {
	datetime := "2021-06-06 11:11:11"
	t.Logf("Date: %d", Timestamp(datetime, DefaultLayout))
}

func TestToday(t *testing.T) {
	t.Logf("Today: %s", Today())
}

func TestTodayStartTime(t *testing.T) {
	t.Logf("TodayStartTime: %v", TodayStartTime())
}

func TestTodayEndTime(t *testing.T) {
	t.Logf("TodayEndTime: %v", TodayEndTime())
}

func TestDayStartTime(t *testing.T) {
	t.Logf("DayStartTime: %v", DayStartTime(time.Now()))
}

func TestDayEndTime(t *testing.T) {
	t.Logf("DayEndTime: %v", DayEndTime(time.Now()))
}

func TestWeekStartTime(t *testing.T) {
	t.Logf("WeekStartTime: %v", WeekStartTime(time.Now()))
}

func TestWeekEndTime(t *testing.T) {
	t.Logf("WeekEndTime: %v", WeekEndTime(time.Now()))
}

func TestMonthStartTime(t *testing.T) {
	t.Logf("MonthStartTime: %v", MonthStartTime(time.Now()))
}

func TestMonthEndTime(t *testing.T) {
	t.Logf("MonthEndTime: %v", MonthEndTime(time.Now()))
}
func TestYearStartTime(t *testing.T) {
	t.Logf("YearStartTime: %v", YearStartTime(time.Now()))
}

func TestYearEndTime(t *testing.T) {
	t.Logf("YearEndTime: %v", YearEndTime(time.Now()))
}

