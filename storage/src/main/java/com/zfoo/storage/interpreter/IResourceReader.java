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

package com.zfoo.storage.interpreter;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * 解释器模式设计的文本解析器
 * <p>
 * interpreter  [in'ter·pret·er || ɪn'tɜrprɪtə(r) /-'tɜːp-]
 * n.  直译程序, 翻译员, 解释者
 *
 * @author godotg
 * @version 4.0
 */
public interface IResourceReader {

    <T> List<T> read(InputStream inputStream, Class<T> clazz, String suffix) throws IOException;

}
