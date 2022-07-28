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

import com.zfoo.protocol.util.IOUtils;
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.util.math.RandomUtils;

import java.awt.*;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.QuadCurve2D;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Base64;

/**
 * 验证码抽象类
 *
 * @author godotg
 * @version 3.0
 */
public abstract class AbstractCaptcha {

    /**
     * 常用颜色
     */
    private static final int[][] COLOR = new int[][]
            {
                    {0, 135, 255},
                    {51, 153, 51},
                    {255, 102, 102},
                    {255, 153, 0},
                    {153, 102, 0},
                    {153, 102, 153},
                    {51, 153, 153},
                    {102, 102, 255},
                    {0, 102, 204},
                    {204, 51, 51},
                    {0, 153, 204},
                    {0, 51, 102}
            };
    /**
     * 验证码随机字符长度
     */
    protected int length = 5;
    /**
     * 验证码显示宽度
     */
    protected int width = 130;
    /**
     * 验证码显示高度
     */
    protected int height = 48;
    /**
     * 当前验证码
     */
    protected String captcha;
    private CaptchaCharEnum captchaChar = CaptchaCharEnum.ARAB_ENGLISH;
    private CaptchaFontEnum captchaFont = CaptchaFontEnum.actionj;

    public void buildCaptcha() {
        switch (captchaChar) {
            case ARAB_NUMBER:
                captcha = RandomUtils.randomString(StringUtils.ARAB_NUMBER, length);
                break;
            case ENGLISH_CHAR:
                captcha = RandomUtils.randomString(StringUtils.ENGLISH_CHAR, length);
                break;
            case ARAB_ENGLISH:
                captcha = RandomUtils.randomString(length);
                break;
            default:
        }
    }


    /**
     * 给定范围获得随机颜色
     *
     * @param fc 0-255
     * @param bc 0-255
     * @return 随机颜色
     */
    protected Color color(int fc, int bc) {
        if (fc > 255) {
            fc = 255;
        }
        if (bc > 255) {
            bc = 255;
        }
        int r = fc + RandomUtils.randomInt(bc - fc);
        int g = fc + RandomUtils.randomInt(bc - fc);
        int b = fc + RandomUtils.randomInt(bc - fc);
        return new Color(r, g, b);
    }

    /**
     * 获取随机常用颜色
     *
     * @return 随机颜色
     */
    protected Color color() {
        int[] color = COLOR[RandomUtils.randomInt(COLOR.length)];
        return new Color(color[0], color[1], color[2]);
    }

    /**
     * 验证码输出,抽象方法，由子类实现
     */
    public abstract void drawImage(OutputStream os);

    /**
     * 输出base64编码
     *
     * @return base64编码字符串
     */
    public abstract String toBase64();

    /**
     * 输出base64编码
     *
     * @param type 编码头
     * @return base64编码字符串
     */
    public String toBase64(String type) {
        var outputStream = new ByteArrayOutputStream();
        drawImage(outputStream);
        var base64 = type + Base64.getEncoder().encodeToString(outputStream.toByteArray());
        IOUtils.closeIO(outputStream);
        return base64;
    }

    /**
     * 获取当前的验证码
     */
    public String captcha() {
        return captcha;
    }


    /**
     * 随机画干扰线
     *
     * @param num 数量
     * @param g   Graphics2D
     */
    public void drawLine(int num, Graphics2D g) {
        drawLine(num, null, g);
    }

    /**
     * 随机画干扰线
     *
     * @param num   数量
     * @param color 颜色
     * @param g     Graphics2D
     */
    public void drawLine(int num, Color color, Graphics2D g) {
        for (int i = 0; i < num; i++) {
            g.setColor(color == null ? color() : color);
            int x1 = RandomUtils.randomInt(-10, width - 10);
            int y1 = RandomUtils.randomInt(5, height - 5);
            int x2 = RandomUtils.randomInt(10, width + 10);
            int y2 = RandomUtils.randomInt(2, height - 2);
            g.drawLine(x1, y1, x2, y2);
        }
    }

    /**
     * 随机画干扰圆
     *
     * @param num 数量
     * @param g   Graphics2D
     */
    public void drawOval(int num, Graphics2D g) {
        drawOval(num, null, g);
    }

    /**
     * 随机画干扰圆
     *
     * @param num   数量
     * @param color 颜色
     * @param g     Graphics2D
     */
    public void drawOval(int num, Color color, Graphics2D g) {
        for (int i = 0; i < num; i++) {
            g.setColor(color == null ? color() : color);
            int w = 5 + RandomUtils.randomInt(10);
            g.drawOval(RandomUtils.randomInt(width - 25), RandomUtils.randomInt(height - 15), w, w);
        }
    }

    /**
     * 随机画贝塞尔曲线
     *
     * @param num 数量
     * @param g   Graphics2D
     */
    public void drawBesselLine(int num, Graphics2D g) {
        drawBesselLine(num, null, g);
    }

    /**
     * 随机画贝塞尔曲线
     *
     * @param num   数量
     * @param color 颜色
     * @param g     Graphics2D
     */
    public void drawBesselLine(int num, Color color, Graphics2D g) {
        for (var i = 0; i < num; i++) {
            g.setColor(color == null ? color() : color);
            int x1 = 5, y1 = RandomUtils.randomInt(5, height / 2);
            int x2 = width - 5, y2 = RandomUtils.randomInt(height / 2, height - 5);
            int ctrlx = RandomUtils.randomInt(width / 4, width / 4 * 3), ctrly = RandomUtils.randomInt(5, height - 5);
            if (RandomUtils.randomInt(2) == 0) {
                int ty = y1;
                y1 = y2;
                y2 = ty;
            }
            if (RandomUtils.randomInt(2) == 0) {  // 二阶贝塞尔曲线
                QuadCurve2D shape = new QuadCurve2D.Double();
                shape.setCurve(x1, y1, ctrlx, ctrly, x2, y2);
                g.draw(shape);
            } else {  // 三阶贝塞尔曲线
                int ctrlx1 = RandomUtils.randomInt(width / 4, width / 4 * 3), ctrly1 = RandomUtils.randomInt(5, height - 5);
                CubicCurve2D shape = new CubicCurve2D.Double(x1, y1, ctrlx, ctrly, ctrlx1, ctrly1, x2, y2);
                g.draw(shape);
            }
        }
    }


    public CaptchaCharEnum getCaptchaChar() {
        return captchaChar;
    }

    public void setCaptchaChar(CaptchaCharEnum captchaChar) {
        this.captchaChar = captchaChar;
    }

    public CaptchaFontEnum getCaptchaFont() {
        return captchaFont;
    }

    public void setCaptchaFont(CaptchaFontEnum captchaFont) {
        this.captchaFont = captchaFont;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
