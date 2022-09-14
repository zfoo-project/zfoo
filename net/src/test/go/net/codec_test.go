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

package net

import "testing"

func TestCodec(t *testing.T) {
	// test encode
	msg1 := NewMessage(1, []byte("message codec test..."))

	data, err := Encode(msg1)
	if err != nil {
		t.Fatal(err)
	}

	t.Log(msg1)

	// test decode
	// The first four bytes is size for socket read
	msg2, err := Decode(data[4:])
	if err != nil {
		t.Fatal(err)
	}

	t.Logf("ID=%d, Data=%s", msg2.GetID(), string(msg2.GetData()))
}
