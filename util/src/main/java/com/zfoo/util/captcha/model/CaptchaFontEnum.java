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

package com.zfoo.util.captcha.model;

import com.zfoo.protocol.util.ClassUtils;
import com.zfoo.protocol.util.IOUtils;
import com.zfoo.protocol.util.StringUtils;

import java.awt.*;
import java.io.InputStream;

/**
 * @author godotg
 * @version 3.0
 */
public enum CaptchaFontEnum {

    /**
     * 默认字体
     */
    actionj,

    epilog,

    fresnel,

    headache,

    lexo,

    prefix,

    progbot,

    ransom,

    robot,

    scandal,

    ;

    private static final Font DEFAULT_FONT = new Font("Arial", Font.BOLD, 32);

    private final Font font;

    CaptchaFontEnum() {
        InputStream inputStream = null;
        try {
            inputStream = ClassUtils.getFileFromClassPath(StringUtils.format("captcha/{}.ttf", this.name()));
            font = Font.createFont(Font.TRUETYPE_FONT, inputStream).deriveFont(Font.BOLD, 32f);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeIO(inputStream);
        }
    }

    public Font getFont() {
        return font;
    }
}
