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

package com.zfoo.monitor;

/**
 * @author godotg
 */
public class DiskStorage implements Comparable<DiskStorage> {

	private String device;

	// 每秒实际读取需求的数量
	private long rs;

	// 每秒实际读取的大小，单位为KB
	private long rKBs;

	// 每秒实际写入需求的数量
	private long ws;

	// 每秒实际写入的大小，单位为KB
	private long wKBs;

	public static DiskStorage valueOf(String device, long rs, long rKBs, long ws, long wKBs)
	{
		DiskStorage ds = new DiskStorage();
		ds.device = device;
		ds.rs     = rs;
		ds.rKBs   = rKBs;
		ds.ws     = ws;
		ds.wKBs   = wKBs;
		return ds;
	}

	@Override
	public int compareTo(DiskStorage target) {
		if (target == null) {
			return 1;
		}
		var a = this.rs + this.ws;
		var b = target.getRs() + target.getWs();
		return Double.compare(a, b);
	}

	public String getDevice() {
		return device;
	}

	public long getRs() {
		return rs;
	}

	public long getrKBs() {
		return rKBs;
	}

	public long getWs() {
		return ws;
	}

	public long getwKBs() {
		return wKBs;
	}

}
