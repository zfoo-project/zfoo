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

package com.zfoo.storage.resource;

import com.zfoo.storage.anno.Id;
import com.zfoo.storage.anno.Storage;

import java.util.Map;

/**
 * @author godotg
 */
@Storage
public class TestResource {

	@Id
	private int Id;
	private long Type0;
	private String Type1;
	private String[] Type2;
	private Integer[] Type3;
	private Long[] Type4;
	private Item[] Type5;
	private Map<Integer, Integer> Type8;
	private Map<Integer, String> Type9;
	private Map<String, String> Type10;
	
	public int getId() {
		return Id;
	}
	public long getType0() {
		return Type0;
	}
	public String getType1() {
		return Type1;
	}
	public String[] getType2() {
		return Type2;
	}
	public Integer[] getType3() {
		return Type3;
	}
	public Long[] getType4() {
		return Type4;
	}
	public Item[] getType5() {
		return Type5;
	}
	public Map<Integer, Integer> getType8() {
		return Type8;
	}
	public Map<Integer, String> getType9() {
		return Type9;
	}
	public Map<String, String> getType10() {
		return Type10;
	}
}
