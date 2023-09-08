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

package com.zfoo.protocol.xml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"id", "location", "enhance"})
public class XmlProtocolDefinition {

    @JacksonXmlProperty(isAttribute = true, localName = "id")
    private short id = -1;

    @JacksonXmlProperty(isAttribute = true, localName = "location")
    private String location;

    @JacksonXmlProperty(isAttribute = true, localName = "enhance")
    private boolean enhance = true;


    public short getId() {
        return id;
    }

    public String getLocation() {
        return location;
    }

    public boolean isEnhance() {
        return enhance;
    }

}
