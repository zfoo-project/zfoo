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

package com.zfoo.util.captcha.gif;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author godotg
 * @version 3.0
 */
public class Encoder {
    static final int BITS = 12;
    static final int HSIZE = 5003; // 80% 占用率
    private static final int EOF = -1;
    int n_bits; // number of bits/code
    int maxbits = BITS; // user settable max # bits/code
    int maxcode; // maximum code, given n_bits
    int maxmaxcode = 1 << BITS; // should NEVER generate this code
    int[] htab = new int[HSIZE];
    int[] codetab = new int[HSIZE];
    int hsize = HSIZE; // for dynamic table sizing
    int free_ent = 0; // first unused entry
    // block compression parameters -- after all codes are used up,
    // and compression rate changes, start over.
    boolean clear_flg = false;
    int g_init_bits;
    int ClearCode;
    int EOFCode;
    int cur_accum = 0;
    int cur_bits = 0;

    // Algorithm:  use open addressing double hashing (no chaining) on the
    // prefix code / next character combination.  We do a variant of Knuth's
    // algorithm D (vol. 3, sec. 6.4) along with G. Knott's relatively-prime
    // secondary probe.  Here, the modular division first probe is gives way
    // to a faster exclusive-or manipulation.  Also do block compression with
    // an adaptive reset, whereby the code table is cleared when the compression
    // ratio decreases, but after the table fills.  The variable-length output
    // codes are re-sized at this point, and a special CLEAR code is generated
    // for the decompressor.  Late addition:  construct the table according to
    // file size for noticeable speed improvement on small files.  Please direct
    // questions about this implementation to ames!jaw.
    int[] masks =
            {
                    0x0000,
                    0x0001,
                    0x0003,
                    0x0007,
                    0x000F,
                    0x001F,
                    0x003F,
                    0x007F,
                    0x00FF,
                    0x01FF,
                    0x03FF,
                    0x07FF,
                    0x0FFF,
                    0x1FFF,
                    0x3FFF,
                    0x7FFF,
                    0xFFFF};
    // Number of characters so far in this 'packet'
    int a_count;
    // Define the storage for the packet accumulator
    byte[] accum = new byte[256];

    // output
    //
    // Output the given code.
    // Inputs:
    //      code:   A n_bits-bit integer.  If == -1, then EOF.  This assumes
    //              that n_bits =< wordsize - 1.
    // Outputs:
    //      Outputs code to the file.
    // Assumptions:
    //      Chars are 8 bits long.
    // Algorithm:
    //      Maintain a BITS character long buffer (so that 8 codes will
    // fit in it exactly).  Use the VAX insv instruction to insert each
    // code in turn.  When the buffer fills up empty it and start over.
    // 图片的宽高
    private final int imgW;
    private final int imgH;
    private final byte[] pixAry;
    private final int initCodeSize;  // 验证码位数
    private int remaining;  // 剩余数量
    private int curPixel;  // 像素

    //----------------------------------------------------------------------------

    /**
     * @param width       宽度
     * @param height      高度
     * @param pixels      像素
     * @param color_depth 颜色
     */
    Encoder(int width, int height, byte[] pixels, int color_depth) {
        imgW = width;
        imgH = height;
        pixAry = pixels;
        initCodeSize = Math.max(2, color_depth);
    }

    // Add a character to the end of the current packet, and if it is 254
    // characters, flush the packet to disk.

    /**
     * @param c    字节
     * @param outs 输出流
     * @throws IOException IO异常
     */
    void char_out(byte c, OutputStream outs) throws IOException {
        accum[a_count++] = c;
        if (a_count >= 254) {
            flush_char(outs);
        }
    }

    // Clear out the hash table

    // table clear for block compress

    /**
     * @param outs 输出流
     * @throws IOException IO异常
     */
    void cl_block(OutputStream outs) throws IOException {
        cl_hash(hsize);
        free_ent = ClearCode + 2;
        clear_flg = true;

        output(ClearCode, outs);
    }

    // reset code table

    /**
     * @param hsize int
     */
    void cl_hash(int hsize) {
        for (int i = 0; i < hsize; ++i) {
            htab[i] = -1;
        }
    }

    /**
     * @param init_bits int
     * @param outs      输出流
     * @throws IOException IO异常
     */
    void compress(int init_bits, OutputStream outs) throws IOException {
        int fcode;
        int i /* = 0 */;
        int c;
        int ent;
        int disp;
        int hsize_reg;
        int hshift;

        // Set up the globals:  g_init_bits - initial number of bits
        g_init_bits = init_bits;

        // Set up the necessary values
        clear_flg = false;
        n_bits = g_init_bits;
        maxcode = MAXCODE(n_bits);

        ClearCode = 1 << (init_bits - 1);
        EOFCode = ClearCode + 1;
        free_ent = ClearCode + 2;

        a_count = 0; // clear packet

        ent = nextPixel();

        hshift = 0;
        for (fcode = hsize; fcode < 65536; fcode *= 2) {
            ++hshift;
        }
        hshift = 8 - hshift; // set hash code range bound

        hsize_reg = hsize;
        cl_hash(hsize_reg); // clear hash table

        output(ClearCode, outs);

        outer_loop:
        while ((c = nextPixel()) != EOF) {
            fcode = (c << maxbits) + ent;
            i = (c << hshift) ^ ent; // xor hashing

            if (htab[i] == fcode) {
                ent = codetab[i];
                continue;
            } else if (htab[i] >= 0) // non-empty slot
            {
                disp = hsize_reg - i; // secondary hash (after G. Knott)
                if (i == 0) {
                    disp = 1;
                }
                do {
                    if ((i -= disp) < 0) {
                        i += hsize_reg;
                    }

                    if (htab[i] == fcode) {
                        ent = codetab[i];
                        continue outer_loop;
                    }
                } while (htab[i] >= 0);
            }
            output(ent, outs);
            ent = c;
            if (free_ent < maxmaxcode) {
                codetab[i] = free_ent++; // code -> hashtable
                htab[i] = fcode;
            } else {
                cl_block(outs);
            }
        }
        // Put out the final code.
        output(ent, outs);
        output(EOFCode, outs);
    }

    //----------------------------------------------------------------------------

    /**
     * @param os 输出流
     * @throws IOException IO异常
     */
    void encode(OutputStream os) throws IOException {
        os.write(initCodeSize); // write "initial code size" byte

        remaining = imgW * imgH; // reset navigation variables
        curPixel = 0;

        compress(initCodeSize + 1, os); // compress and write the pixel data

        os.write(0); // write block terminator
    }

    // Flush the packet to disk, and reset the accumulator

    /**
     * @param outs 输出流
     * @throws IOException IO异常
     */
    void flush_char(OutputStream outs) throws IOException {
        if (a_count > 0) {
            outs.write(a_count);
            outs.write(accum, 0, a_count);
            a_count = 0;
        }
    }

    /**
     * @param n_bits int
     * @return int
     */
    final int MAXCODE(int n_bits) {
        return (1 << n_bits) - 1;
    }

    //----------------------------------------------------------------------------
    // Return the next pixel from the image
    //----------------------------------------------------------------------------

    /**
     * @return int
     */
    private int nextPixel() {
        if (remaining == 0) {
            return EOF;
        }

        --remaining;

        byte pix = pixAry[curPixel++];

        return pix & 0xff;
    }

    /**
     * @param code int
     * @param outs 输出流
     * @throws IOException IO异常
     */
    void output(int code, OutputStream outs) throws IOException {
        cur_accum &= masks[cur_bits];

        if (cur_bits > 0) {
            cur_accum |= (code << cur_bits);
        } else {
            cur_accum = code;
        }

        cur_bits += n_bits;

        while (cur_bits >= 8) {
            char_out((byte) (cur_accum & 0xff), outs);
            cur_accum >>= 8;
            cur_bits -= 8;
        }

        // If the next entry is going to be too big for the code size,
        // then increase it, if possible.
        if (free_ent > maxcode || clear_flg) {
            if (clear_flg) {
                maxcode = MAXCODE(n_bits = g_init_bits);
                clear_flg = false;
            } else {
                ++n_bits;
                if (n_bits == maxbits) {
                    maxcode = maxmaxcode;
                } else {
                    maxcode = MAXCODE(n_bits);
                }
            }
        }

        if (code == EOFCode) {
            // At EOF, write the rest of the buffer.
            while (cur_bits > 0) {
                char_out((byte) (cur_accum & 0xff), outs);
                cur_accum >>= 8;
                cur_bits -= 8;
            }

            flush_char(outs);
        }
    }
}
