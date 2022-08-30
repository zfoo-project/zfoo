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

package com.zfoo.storage;

import com.zfoo.storage.model.anno.ResInjection;
import com.zfoo.storage.model.vo.Storage;
import com.zfoo.storage.resource.StudentCsvResource;
import com.zfoo.storage.resource.StudentResource;
import org.springframework.stereotype.Component;

/**
 * @author godotg
 * @version 4.0
 */
@Component
public class StudentManager {

    @ResInjection
    public Storage<Integer, StudentResource> studentResources;
    @ResInjection
    public Storage<Integer, StudentCsvResource> studentCsvResources;

}
