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
 *
 */

package com.zfoo.protocol.util;

import com.zfoo.protocol.xml.XmlProtocols;
import org.junit.Assert;
import org.junit.Test;

public class DomUtilsTest {

    private static final String XML_WITH_HEAD = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n" +
            "\n" +
            "<protocols>\n" +
            "    <module id=\"1\" name=\"common\">\n" +
            "        <protocol id=\"1000\" location=\"com.zfoo.test.CM_Int\"/>\n" +
            "        <protocol id=\"2000\" location=\"com.zfoo.test.SM_Int\"/>\n" +
            "    </module>\n" +
            "\n" +
            "    <module id=\"2\" name=\"common\">\n" +
            "        <protocol id=\"3000\" location=\"com.zfoo.test.CM_Float\"/>\n" +
            "    </module>\n" +
            "</protocols>";

    private static final String XML_OF_STANDARD_TEXT = "<protocols>\n" +
            "    <module id=\"1\" name=\"common\">\n" +
            "        <protocol id=\"1000\" location=\"com.zfoo.test.CM_Int\"/>\n" +
            "        <protocol id=\"2000\" location=\"com.zfoo.test.SM_Int\"/>\n" +
            "    </module>\n" +
            "\n" +
            "    <module id=\"2\" name=\"common\">\n" +
            "        <protocol id=\"3000\" location=\"com.zfoo.test.CM_Float\"/>\n" +
            "    </module>\n" +
            "</protocols>";

    @Test
    public void testXmlWithHead() {
        var protos = DomUtils.string2Object(XML_WITH_HEAD, XmlProtocols.class);
        for (var module : protos.getModules()) {
            System.out.println(module.getId());
        }
    }

    @Test
    public void testXmlOfStandardText() {
        var protos = DomUtils.string2Object(XML_OF_STANDARD_TEXT, XmlProtocols.class);
        for (var module : protos.getModules()) {
            System.out.println(module.getId());
        }
    }

}
