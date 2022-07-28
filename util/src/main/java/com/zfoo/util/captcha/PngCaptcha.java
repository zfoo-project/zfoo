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

import com.zfoo.protocol.util.IOUtils;
import com.zfoo.util.captcha.model.AbstractCaptcha;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

/**
 * png格式验证码
 *
 * @author godotg
 * @version 3.0
 */
public class PngCaptcha extends AbstractCaptcha {

    @Override
    public void drawImage(OutputStream out) {
        var captchaChars = captcha.toCharArray();
        BufferedImage bi = null;
        Graphics2D g2d = null;
        try {
            bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            g2d = (Graphics2D) bi.getGraphics();

            // 填充背景
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, width, height);
            // 抗锯齿
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // 画干扰圆
            drawOval(2, g2d);
            // 画干扰线
            g2d.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
            drawBesselLine(1, g2d);
            // 画字符串
            g2d.setFont(getCaptchaFont().getFont());
            FontMetrics fontMetrics = g2d.getFontMetrics();
            int fW = width / captchaChars.length;  // 每一个字符所占的宽度
            int fSp = (fW - (int) fontMetrics.getStringBounds("W", g2d).getWidth()) / 2;  // 字符的左右边距
            for (int i = 0; i < captchaChars.length; i++) {
                g2d.setColor(color());
                int fY = height - ((height - (int) fontMetrics.getStringBounds(String.valueOf(captchaChars[i]), g2d).getHeight()) >> 1);  // 文字的纵坐标
                g2d.drawString(String.valueOf(captchaChars[i]), i * fW + fSp + 3, fY - 3);
            }
            ImageIO.write(bi, "png", out);
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeIO(out);
            if (g2d != null) {
                g2d.dispose();
            }
            if (bi != null) {
                bi.getGraphics().dispose();
            }
        }
    }

    @Override
    public String toBase64() {
        return toBase64("data:image/png;base64,");
    }

}
