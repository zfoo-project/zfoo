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

package com.zfoo.storage.model.vo;

import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.Objects;

/**
 * @author godotg
 * @version 4.0
 */
public class ResourceDef {

    private final Class<?> clazz;
    private final Resource resource;

    public ResourceDef(Class<?> clazz, Resource resource) {
        this.clazz = clazz;
        this.resource = resource;
    }


    public Class<?> getClazz() {
        return clazz;
    }

    public Resource getResource() {
        return resource;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceDef that = (ResourceDef) o;
        boolean equal= false;
        try {
            equal = this.resource.getFile().getAbsolutePath().equals(that.resource.getFile().getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return equal;
    }

    @Override
    public int hashCode() {
        int hashCode=0;
        try {
            hashCode=this.resource.getFile().getAbsolutePath().hashCode();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return hashCode;
    }
}
