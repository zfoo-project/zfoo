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

package com.zfoo.protocol.serializer.protobuf;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "protocols")
public class XmlProtobuf {

    @JacksonXmlProperty(isAttribute = true, localName = "syntax")
    private String syntax;

    @JacksonXmlProperty(isAttribute = true, localName = "option")
    private String option;

    @JacksonXmlProperty(localName = "proto")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<XmlProtobufProto> protos;

    public String getSyntax() {
        return syntax;
    }

    public String getOption() {
        return option;
    }

    public List<XmlProtobufProto> getProtos() {
        return protos;
    }
}
