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

package com.zfoo.storage.export;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zfoo.storage.util.ExportUtils;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author godotg
 * @version 3.0
 */
@Ignore
public class ExportJsonTest {

    @Test
    public void test() throws Exception {
        var inputDir = "E:\\workspace\\zfoo\\storage\\src\\test\\resources\\excel";
        var outputDir = "E:\\workspace\\zfoo\\storage\\src\\test\\resources\\excel";
        ExportUtils.excel2json(inputDir, outputDir);
    }

}
