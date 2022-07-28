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

package com.zfoo.util.captcha;

import com.zfoo.util.captcha.model.CaptchaFontEnum;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;

/**
 * @author godotg
 * @version 3.0
 */
@Ignore
public class CaptchaTest {

    @Test
    public void pngTest() throws Exception {

        for (var font : CaptchaFontEnum.values()) {
            PngCaptcha pngCaptcha = new PngCaptcha();
            pngCaptcha.setLength(4);
            pngCaptcha.setCaptchaFont(font);
            pngCaptcha.buildCaptcha();
            System.out.println(pngCaptcha.captcha());
            pngCaptcha.drawImage(new FileOutputStream(new File(font.name() + ".png")));
        }
    }

    @Test
    public void gifTest() throws Exception {
        for (var font : CaptchaFontEnum.values()) {
            GifCaptcha gifCaptcha = new GifCaptcha();
            gifCaptcha.setLength(5);
            gifCaptcha.setCaptchaFont(font);
            gifCaptcha.buildCaptcha();
            System.out.println(gifCaptcha.captcha());
            gifCaptcha.drawImage(new FileOutputStream(new File(font.name() + ".gif")));
        }
    }

    @Test
    public void arithmeticTest() throws Exception {
        for (var font : CaptchaFontEnum.values()) {
            ArithmeticCaptcha specCaptcha = new ArithmeticCaptcha();
            specCaptcha.setLength(3);
            specCaptcha.setCaptchaFont(font);
            specCaptcha.buildCaptcha();
            System.out.println(specCaptcha.getArithmeticString() + " " + specCaptcha.captcha());
            specCaptcha.drawImage(new FileOutputStream(new File(font.name() + ".png")));
        }
    }

    @Test
    public void base64Test() throws Exception {
        for (var i = 0; i < Long.MAX_VALUE; i++) {
            GifCaptcha gifCaptcha = new GifCaptcha();
            gifCaptcha.buildCaptcha();
            gifCaptcha.toBase64();

            PngCaptcha pngCaptcha = new PngCaptcha();
            pngCaptcha.buildCaptcha();
            pngCaptcha.toBase64();

            ArithmeticCaptcha arithmeticCaptcha = new ArithmeticCaptcha();
            arithmeticCaptcha.buildCaptcha();
            arithmeticCaptcha.toBase64();
        }
    }

}
