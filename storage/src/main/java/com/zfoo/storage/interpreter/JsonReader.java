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

package com.zfoo.storage.interpreter;

import com.zfoo.protocol.util.IOUtils;
import com.zfoo.protocol.util.JsonUtils;
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.storage.interpreter.data.StorageData;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author godotg
 * @version 3.0
 */
public abstract class JsonReader {

    public static StorageData readResourceDataFromJson(InputStream input) {
        try {
            var resourceData= JsonUtils.string2Object(StringUtils.bytesToString(IOUtils.toByteArray(input)), StorageData.class);
            for(int i=0;i<resourceData.getHeaders().size();i++) {
                resourceData.getHeaders().get(i).setIndex(i);
            }
            return resourceData;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
