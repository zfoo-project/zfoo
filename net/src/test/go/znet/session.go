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
package znet

import "sync/atomic"

// Session struct
type Session struct {
	sid      uint64
	uid      uint64
	conn     *Conn
}

var uuid uint64

// NewSession create a new session
func NewSession(conn *Conn) *Session {
	var suuid = atomic.AddUint64(&uuid, 1)

	session := &Session{
		sid:      suuid,
		uid:      0,// 可以为用户的id
		conn:     conn,
	}

	return session
}
