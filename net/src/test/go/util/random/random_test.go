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
package random

import (
	"testing"
)

func TestGetRandomSring(t *testing.T) {
	t.Logf("GetRandomString: %s", GetRandomString(4, "abide123456"))
	t.Logf("GetRandomString: %s", GetRandomString(6, "abcdefghijklmnopqrstuvwxyz0123456789"))
}

func TestGetRandomChars(t *testing.T) {
	t.Logf("GetRandomChars: %s", GetRandomChars(4))
	t.Logf("GetRandomChars: %s", GetRandomChars(6))
}

func TestGetRandomNumbers(t *testing.T) {
	t.Logf("GetRandomNumbers: %s", GetRandomNumbers(4))
	t.Logf("GetRandomNumbers: %s", GetRandomNumbers(6))
}

func TestGetRandomInt(t *testing.T) {
	t.Logf("GetRandomInt: %d", GetRandomInt(10))
	t.Logf("GetRandomInt: %d", GetRandomInt(10000))
}
